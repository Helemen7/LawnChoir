/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.animation;

import static android.app.WindowConfiguration.ACTIVITY_TYPE_HOME;
import static android.view.WindowManager.TRANSIT_CLOSE;
import static android.view.WindowManager.TRANSIT_OLD_NONE;
import static android.view.WindowManager.TRANSIT_OPEN;
import static android.view.WindowManager.TRANSIT_TO_BACK;
import static android.view.WindowManager.TRANSIT_TO_FRONT;
import static com.android.wm.shell.shared.compat.DesktopModeFlags.ENABLE_DESKTOP_WINDOWING_EXIT_BY_MINIMIZE_TRANSITION_BUGFIX;
import static com.android.wm.shell.shared.compat.DesktopModeFlags.ENABLE_DESKTOP_WINDOWING_EXIT_TRANSITIONS_BUGFIX;
import static android.window.TransitionInfo.FLAG_IS_WALLPAPER;

import static com.android.internal.util.Preconditions.checkArgument;
import static com.android.wm.shell.shared.TransitionUtil.FLAG_IS_DESKTOP_WALLPAPER_ACTIVITY;
import static com.android.wm.shell.shared.TransitionUtil.isClosingMode;
import static com.android.wm.shell.shared.TransitionUtil.isClosingType;
import static com.android.wm.shell.shared.TransitionUtil.isOpeningMode;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManager.TransitionOldType;
import android.window.IRemoteTransition;
import android.window.IRemoteTransitionFinishedCallback;
import android.window.RemoteTransitionStub;
import android.window.TransitionInfo;

import com.android.wm.shell.shared.CounterRotator;

import java.util.ArrayList;

public abstract class RemoteAnimationRunnerCompat extends IRemoteAnimationRunner.Stub {
    private static final String TAG = "RemoteAnimRunnerCompat";

    /**
     * Implemented by runners that can hand a playing animation over to a transition that
     * interrupts it, instead of having the playing animation cancelled (and so snapped to its end
     * state) as soon as the interrupting transition is ready.
     */
    public interface InterruptibleRunner {
        /**
         * Cheap, thread-safe check of whether the playing animation may accept a merge now.
         */
        boolean canMerge();

        /**
         * Offers an interrupting "return to home" transition to the playing animation.
         *
         * <p>Called on a binder thread. The runner must call exactly one of the callbacks, on any
         * thread: {@code onAccepted} once it took over the playing animation (it applies the
         * start state of the interrupting transition and marks it merged), or {@code onRejected}
         * to fall back to cancelling the playing animation.
         */
        void onMergeTransition(RemoteAnimationTarget[] apps, RemoteAnimationTarget[] wallpapers,
                RemoteAnimationTarget[] nonApps, Runnable onAccepted, Runnable onRejected);

        /**
         * Cancels the playing animation, taking it to its end state first, and only then runs
         * {@code afterCancel}, which finishes the transition. Finishing before the animation is at
         * its end state hands the windows back to the system mid animation.
         */
        void cancelThenRun(Runnable afterCancel);
    }

    public abstract void onAnimationStart(@WindowManager.TransitionOldType int transit,
            RemoteAnimationTarget[] apps, RemoteAnimationTarget[] wallpapers,
            RemoteAnimationTarget[] nonApps, Runnable finishedCallback);

    @Override
    public final void onAnimationStart(@TransitionOldType int transit,
            RemoteAnimationTarget[] apps,
            RemoteAnimationTarget[] wallpapers,
            RemoteAnimationTarget[] nonApps,
            final IRemoteAnimationFinishedCallback finishedCallback) {

        onAnimationStart(transit, apps, wallpapers,
                nonApps, () -> {
                    try {
                        finishedCallback.onAnimationFinished();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to call app controlled animation finished callback", e);
                    }
                });
    }

    public IRemoteTransition toRemoteTransition() {
        return wrap(this);
    }

    /** Wraps a remote animation runner in a remote-transition. */
    public static RemoteTransitionStub wrap(IRemoteAnimationRunner runner) {
        return new RemoteTransitionStub() {
            final ArrayMap<IBinder, Runnable> mFinishRunnables = new ArrayMap<>();
            // Interrupting transitions merged into a playing one, released when it finishes.
            final ArrayMap<IBinder, ArrayList<TransitionInfo>> mMergedInfos = new ArrayMap<>();

            @Override
            public void startAnimation(IBinder token, TransitionInfo info,
                    SurfaceControl.Transaction t,
                    IRemoteTransitionFinishedCallback finishCallback) throws RemoteException {
                final ArrayMap<SurfaceControl, SurfaceControl> leashMap = new ArrayMap<>();
                final RemoteAnimationTarget[] apps =
                        RemoteAnimationTargetCompat.wrapApps(info, t, leashMap);
                final RemoteAnimationTarget[] wallpapers =
                        RemoteAnimationTargetCompat.wrapNonApps(
                                info, true /* wallpapers */, t, leashMap);
                final RemoteAnimationTarget[] nonApps =
                        RemoteAnimationTargetCompat.wrapNonApps(
                                info, false /* wallpapers */, t, leashMap);

                // TODO(b/177438007): Move this set-up logic into launcher's animation impl.
                boolean isReturnToHome = false;
                TransitionInfo.Change launcherTask = null;
                TransitionInfo.Change wallpaper = null;
                int launcherLayer = 0;
                int rotateDelta = 0;
                float displayW = 0;
                float displayH = 0;
                for (int i = info.getChanges().size() - 1; i >= 0; --i) {
                    final TransitionInfo.Change change = info.getChanges().get(i);
                    // skip changes that we didn't wrap
                    if (!leashMap.containsKey(change.getLeash())) continue;
                    if (change.getTaskInfo() != null
                            && change.getTaskInfo().getActivityType() == ACTIVITY_TYPE_HOME) {
                        isReturnToHome = change.getMode() == TRANSIT_OPEN
                                || change.getMode() == TRANSIT_TO_FRONT;
                        launcherTask = change;
                        launcherLayer = info.getChanges().size() - i;
                    } else if ((change.getFlags() & FLAG_IS_WALLPAPER) != 0) {
                        wallpaper = change;
                    }
                    if (change.getParent() == null && change.getEndRotation() >= 0
                            && change.getEndRotation() != change.getStartRotation()) {
                        rotateDelta = change.getEndRotation() - change.getStartRotation();
                        displayW = change.getEndAbsBounds().width();
                        displayH = change.getEndAbsBounds().height();
                    }
                }

                // Prepare for rotation if there is one
                final CounterRotator counterLauncher = new CounterRotator();
                final CounterRotator counterWallpaper = new CounterRotator();
                if (launcherTask != null && rotateDelta != 0 && launcherTask.getParent() != null) {
                    final TransitionInfo.Change parent = info.getChange(launcherTask.getParent());
                    if (parent != null) {
                        counterLauncher.setup(t, parent.getLeash(), rotateDelta, displayW,
                                displayH);
                    } else {
                        Log.e(TAG, "Malformed: " + launcherTask + " has parent="
                                + launcherTask.getParent() + " but it's not in info.");
                    }
                    if (counterLauncher.getSurface() != null) {
                        t.setLayer(counterLauncher.getSurface(), launcherLayer);
                    }
                }

                if (isReturnToHome) {
                    if (counterLauncher.getSurface() != null) {
                        t.setLayer(counterLauncher.getSurface(), info.getChanges().size() * 3);
                    }
                    // Need to "boost" the closing things since that's what launcher expects.
                    for (int i = info.getChanges().size() - 1; i >= 0; --i) {
                        final TransitionInfo.Change change = info.getChanges().get(i);
                        final SurfaceControl leash = leashMap.get(change.getLeash());
                        // skip changes that we didn't wrap
                        if (leash == null) continue;
                        final int mode = info.getChanges().get(i).getMode();
                        // Only deal with independent layers
                        if (!TransitionInfo.isIndependent(change, info)) continue;
                        if (mode == TRANSIT_CLOSE || mode == TRANSIT_TO_BACK) {
                            t.setLayer(leash, info.getChanges().size() * 3 - i);
                            counterLauncher.addChild(t, leash);
                        }
                    }
                    // Make wallpaper visible immediately since launcher apparently won't do this.
                    for (int i = wallpapers.length - 1; i >= 0; --i) {
                        t.show(wallpapers[i].leash);
                        t.setAlpha(wallpapers[i].leash, 1.f);
                    }
                    if (ENABLE_DESKTOP_WINDOWING_EXIT_TRANSITIONS_BUGFIX.isTrue()) {
                        resetLauncherAlphaOnDesktopExit(info, launcherTask, leashMap, t);
                    }
                } else {
                    if (launcherTask != null) {
                        counterLauncher.addChild(t, leashMap.get(launcherTask.getLeash()));
                    }
                    if (wallpaper != null && rotateDelta != 0 && wallpaper.getParent() != null) {
                        final TransitionInfo.Change parent = info.getChange(wallpaper.getParent());
                        if (parent != null) {
                            counterWallpaper.setup(t, parent.getLeash(), rotateDelta, displayW,
                                    displayH);
                        } else {
                            Log.e(TAG, "Malformed: " + wallpaper + " has parent="
                                    + wallpaper.getParent() + " but it's not in info.");
                        }
                        if (counterWallpaper.getSurface() != null) {
                            t.setLayer(counterWallpaper.getSurface(), -1);
                            counterWallpaper.addChild(t, leashMap.get(wallpaper.getLeash()));
                        }
                    }
                }
                t.apply();

                final Runnable animationFinishedCallback = () -> {
                    final SurfaceControl.Transaction finishTransaction =
                            new SurfaceControl.Transaction();
                    counterLauncher.cleanUp(finishTransaction);
                    counterWallpaper.cleanUp(finishTransaction);
                    // Release surface references now. This is apparently to free GPU memory
                    // before GC would.
                    info.releaseAllSurfaces();
                    final ArrayList<TransitionInfo> mergedInfos;
                    synchronized (mFinishRunnables) {
                        mergedInfos = mMergedInfos.remove(token);
                    }
                    if (mergedInfos != null) {
                        for (int i = mergedInfos.size() - 1; i >= 0; --i) {
                            mergedInfos.get(i).releaseAllSurfaces();
                        }
                    }
                    // Don't release here since launcher might still be using them. Instead
                    // let launcher release them (eg. via RemoteAnimationTargets)
                    leashMap.clear();
                    try {
                        finishCallback.onTransitionFinished(null /* wct */, finishTransaction);
                        finishTransaction.close();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to call app controlled animation finished callback", e);
                    }
                };
                synchronized (mFinishRunnables) {
                    mFinishRunnables.put(token, animationFinishedCallback);
                }
                // TODO(bc-unlcok): Pass correct transit type.
                runner.onAnimationStart(TRANSIT_OLD_NONE,
                        apps, wallpapers, nonApps, new IRemoteAnimationFinishedCallback() {
                            @Override
                            public void onAnimationFinished() {
                                synchronized (mFinishRunnables) {
                                    if (mFinishRunnables.remove(token) == null) return;
                                }
                                animationFinishedCallback.run();
                            }

                            @Override
                            public IBinder asBinder() {
                                return null;
                            }
                        });
            }

            @Override
            public void mergeAnimation(IBinder token, TransitionInfo info,
                    SurfaceControl.Transaction t, IBinder mergeTarget,
                    IRemoteTransitionFinishedCallback finishCallback) throws RemoteException {
                Log.d(TAG, "mergeAnimation: type=" + info.getType() + " flags=" + info.getFlags()
                        + " changes=" + info.getChanges().size() + " interruptible="
                        + (runner instanceof InterruptibleRunner));
                if (runner instanceof InterruptibleRunner
                        && tryMergeReturnToHome((InterruptibleRunner) runner, info, t,
                                mergeTarget, finishCallback)) {
                    return;
                }
                // TODO: hook up merge to recents onTaskAppeared if applicable. Until then, adapt
                //       to legacy cancel.
                final Runnable finishRunnable;
                synchronized (mFinishRunnables) {
                    finishRunnable = mFinishRunnables.remove(mergeTarget);
                }
                // Since we're not actually animating, release native memory now
                t.close();
                info.releaseAllSurfaces();
                if (finishRunnable == null) return;
                if (runner instanceof InterruptibleRunner) {
                    ((InterruptibleRunner) runner).cancelThenRun(finishRunnable);
                    return;
                }
                runner.onAnimationCancelled();
                finishRunnable.run();
            }

            /**
             * Lets {@code interruptible} take over the playing animation {@code mergeTarget} for
             * an interrupting return to home. Returns false, having done nothing, if the merge
             * isn't supported here, so the caller falls back to cancelling the playing animation.
             */
            private boolean tryMergeReturnToHome(InterruptibleRunner interruptible,
                    TransitionInfo info, SurfaceControl.Transaction t, IBinder mergeTarget,
                    IRemoteTransitionFinishedCallback finishCallback) {
                if (!interruptible.canMerge()) return false;
                if (!isPlainReturnToHome(info)) {
                    Log.d(TAG, "Merge not attempted, not a plain return to home: type="
                            + info.getType() + " flags=" + info.getFlags());
                    return false;
                }
                synchronized (mFinishRunnables) {
                    if (!mFinishRunnables.containsKey(mergeTarget)) {
                        Log.d(TAG, "Merge not attempted, the playing transition is finished");
                        return false;
                    }
                }
                final ArrayMap<SurfaceControl, SurfaceControl> leashMap = new ArrayMap<>();
                final RemoteAnimationTarget[] apps =
                        RemoteAnimationTargetCompat.wrapApps(info, t, leashMap);
                final RemoteAnimationTarget[] wallpapers =
                        RemoteAnimationTargetCompat.wrapNonApps(
                                info, true /* wallpapers */, t, leashMap);
                final RemoteAnimationTarget[] nonApps =
                        RemoteAnimationTargetCompat.wrapNonApps(
                                info, false /* wallpapers */, t, leashMap);

                final Runnable onAccepted = () -> {
                    // Same layering as a regular return to home. The closing windows are not hidden
                    // here: the runner has already placed them where the launch animation left
                    // them, and hiding them would flash the whole home screen for a frame.
                    for (int i = info.getChanges().size() - 1; i >= 0; --i) {
                        final TransitionInfo.Change change = info.getChanges().get(i);
                        final SurfaceControl leash = leashMap.get(change.getLeash());
                        if (leash == null) continue;
                        if (!TransitionInfo.isIndependent(change, info)) continue;
                        final int mode = change.getMode();
                        if (mode == TRANSIT_CLOSE || mode == TRANSIT_TO_BACK) {
                            t.setLayer(leash, info.getChanges().size() * 3 - i);
                        }
                    }
                    for (int i = wallpapers.length - 1; i >= 0; --i) {
                        t.show(wallpapers[i].leash);
                        t.setAlpha(wallpapers[i].leash, 1.f);
                    }
                    // The windows of the merged transition were made invisible by the launch that
                    // it interrupts, so show the roots and the wrapped leashes explicitly.
                    for (int i = info.getRootCount() - 1; i >= 0; --i) {
                        t.show(info.getRoot(i).getLeash());
                    }
                    for (int i = info.getChanges().size() - 1; i >= 0; --i) {
                        final TransitionInfo.Change change = info.getChanges().get(i);
                        final SurfaceControl leash = leashMap.get(change.getLeash());
                        if (leash == null) continue;
                        final int mode = change.getMode();
                        if (mode == TRANSIT_OPEN || mode == TRANSIT_TO_FRONT) {
                            t.show(leash);
                            t.setAlpha(leash, 1.f);
                        }
                    }
                    t.apply();
                    synchronized (mFinishRunnables) {
                        ArrayList<TransitionInfo> merged = mMergedInfos.get(mergeTarget);
                        if (merged == null) {
                            merged = new ArrayList<>();
                            mMergedInfos.put(mergeTarget, merged);
                        }
                        merged.add(info);
                    }
                    // The playing animation now also covers this transition, and finishes both.
                    final SurfaceControl.Transaction finishTransaction =
                            new SurfaceControl.Transaction();
                    try {
                        finishCallback.onTransitionFinished(null /* wct */, finishTransaction);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to finish a merged transition", e);
                    }
                    finishTransaction.close();
                };
                final Runnable onRejected = () -> {
                    final Runnable finishRunnable;
                    synchronized (mFinishRunnables) {
                        finishRunnable = mFinishRunnables.remove(mergeTarget);
                    }
                    t.close();
                    info.releaseAllSurfaces();
                    if (finishRunnable == null) return;
                    interruptible.cancelThenRun(finishRunnable);
                };
                interruptible.onMergeTransition(apps, wallpapers, nonApps, onAccepted, onRejected);
                return true;
            }

            @Override
            public void onTransitionConsumed(IBinder transition, boolean aborted)
                    throws RemoteException {
                Log.d(TAG, "onTransitionConsumed: aborted=" + aborted);
                // Notify the remote runner that the transition has been canceled if the transition
                // was merged into another transition or aborted
                synchronized (mFinishRunnables) {
                    mFinishRunnables.remove(transition);
                }
                runner.onAnimationCancelled();
            }
        };
    }

    /**
     * Whether {@code info} only brings Home to the front and hides an app, without rotating the
     * display or being a custom transition such as recents.
     */
    private static boolean isPlainReturnToHome(TransitionInfo info) {
        if (info.getType() >= WindowManager.TRANSIT_FIRST_CUSTOM) return false;
        boolean homeShown = false;
        boolean appHidden = false;
        for (int i = info.getChanges().size() - 1; i >= 0; --i) {
            final TransitionInfo.Change change = info.getChanges().get(i);
            if (change.getParent() == null && change.getEndRotation() >= 0
                    && change.getEndRotation() != change.getStartRotation()) {
                return false;
            }
            final int mode = change.getMode();
            if (change.getTaskInfo() != null
                    && change.getTaskInfo().getActivityType() == ACTIVITY_TYPE_HOME) {
                homeShown |= mode == TRANSIT_OPEN || mode == TRANSIT_TO_FRONT;
            } else if ((mode == TRANSIT_CLOSE || mode == TRANSIT_TO_BACK)
                    && (change.getFlags() & FLAG_IS_WALLPAPER) == 0
                    && TransitionInfo.isIndependent(change, info)) {
                appHidden = true;
            }
        }
        return homeShown && appHidden;
    }

    /**
     * Reset the alpha of the Launcher leash to give the Launcher time to hide its Views before the
     * exit-desktop animation starts.
     *
     * This method should only be called if the current transition is opening Launcher, otherwise we
     * might not be exiting Desktop Mode.
     */
    private static void resetLauncherAlphaOnDesktopExit(
            TransitionInfo info,
            TransitionInfo.Change launcherChange,
            ArrayMap<SurfaceControl, SurfaceControl> leashMap,
            SurfaceControl.Transaction startTransaction
    ) {
        checkArgument(isOpeningMode(launcherChange.getMode()));
        if (!isClosingType(info.getType())
                && !ENABLE_DESKTOP_WINDOWING_EXIT_BY_MINIMIZE_TRANSITION_BUGFIX.isTrue()) {
            return;
        }
        for (int i = info.getChanges().size() - 1; i >= 0; --i) {
            final TransitionInfo.Change change = info.getChanges().get(i);
            // skip changes that we didn't wrap
            if (!leashMap.containsKey(change.getLeash())) continue;
            // Only make the update if we are closing Desktop tasks.
            if (change.getTaskInfo() != null && (change.getTaskInfo().isFreeform()
                    || change.hasFlags(FLAG_IS_DESKTOP_WALLPAPER_ACTIVITY))
                    && isClosingMode(change.getMode())) {
                startTransaction.setAlpha(leashMap.get(launcherChange.getLeash()), 0f);
                return;
            }
        }
    }
}
