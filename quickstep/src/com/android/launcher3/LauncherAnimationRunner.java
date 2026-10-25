/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.launcher3;

import static com.android.launcher3.Utilities.postAsyncCallback;
import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;
import static com.android.launcher3.util.window.RefreshRateTracker.getSingleFrameMs;
import static com.android.systemui.shared.recents.utilities.Utilities.postAtFrontOfQueueAsynchronously;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import android.window.TransitionInfo;

import androidx.annotation.BinderThread;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.android.systemui.animation.RemoteAnimationDelegate;
import com.android.systemui.animation.RemoteAnimationRunnerCompat;

import java.lang.ref.WeakReference;

/**
 * This class is needed to wrap any animation runner that is a part of the
 * RemoteAnimationDefinition:
 * - Launcher creates a new instance of the LauncherAppTransitionManagerImpl whenever it is
 *   created, which in turn registers a new definition
 * - When the definition is registered, window manager retains a strong binder reference to the
 *   runner passed in
 * - If the Launcher activity is recreated, the new definition registered will replace the old
 *   reference in the system's activity record, but until the system server is GC'd, the binder
 *   reference will still exist, which references the runner in the Launcher process, which
 *   references the (old) Launcher activity through this class
 *
 * Instead we make the runner provided to the definition static only holding a weak reference to
 * the runner implementation.  When this animation manager is destroyed, we remove the Launcher
 * reference to the runner, leaving only the weak ref from the runner.
 */
public class LauncherAnimationRunner extends RemoteAnimationRunnerCompat
        implements RemoteAnimationRunnerCompat.InterruptibleRunner {

    private static final RemoteAnimationFactory DEFAULT_FACTORY =
            (transit, appTargets, wallpaperTargets, nonAppTargets, result) ->
                    result.setAnimation(null, null);

    private final Handler mHandler;
    private final boolean mStartAtFrontOfQueue;
    private final WeakReference<RemoteAnimationFactory> mFactory;

    private AnimationResult mAnimationResult;

    /**
     * @param startAtFrontOfQueue If true, the animation start will be posted at the front of the
     *                            queue to minimize latency.
     */
    public LauncherAnimationRunner(Handler handler, RemoteAnimationFactory factory,
            boolean startAtFrontOfQueue) {
        mHandler = handler;
        mFactory = new WeakReference<>(factory);
        mStartAtFrontOfQueue = startAtFrontOfQueue;
    }

    // Called only in S+ platform
    @BinderThread
    public void onAnimationStart(
            int transit,
            RemoteAnimationTarget[] appTargets,
            RemoteAnimationTarget[] wallpaperTargets,
            RemoteAnimationTarget[] nonAppTargets,
            Runnable runnable) {
        Log.d("LauncherAnimationRunner", "onAnimationStart transit=" + transit + " apps="
                + (appTargets == null ? 0 : appTargets.length) + " factory="
                + getFactory().getClass().getSimpleName());
        Runnable r = () -> {
            finishExistingAnimation();
            mAnimationResult = new AnimationResult(() -> mAnimationResult = null, runnable);
            getFactory().onAnimationStart(transit, appTargets, wallpaperTargets, nonAppTargets,
                    mAnimationResult);
        };
        if (mStartAtFrontOfQueue) {
            postAtFrontOfQueueAsynchronously(mHandler, r);
        } else {
            postAsyncCallback(mHandler, r);
        }
    }

    // Introduced in NothingOS 2.5.5, needed in 2.6
    @BinderThread
    public void onAnimationStartWithSurfaceTransaction(
            int transit,
            TransitionInfo transitionInfo,
            SurfaceControl.Transaction transaction,
            RemoteAnimationTarget[] appTargets,
            RemoteAnimationTarget[] wallpaperTargets,
            RemoteAnimationTarget[] nonAppTargets,
            Runnable runnable) {
        if (transaction != null) {
            transaction.apply();
        }
        onAnimationStart(transit, appTargets, wallpaperTargets, nonAppTargets, runnable);
    }

    private RemoteAnimationFactory getFactory() {
        RemoteAnimationFactory factory = mFactory.get();
        return factory != null ? factory : DEFAULT_FACTORY;
    }

    @Override
    public boolean canMerge() {
        return getFactory() instanceof MergeableFactory mergeable && mergeable.canMergeClosing();
    }

    @BinderThread
    @Override
    public void onMergeTransition(
            RemoteAnimationTarget[] appTargets,
            RemoteAnimationTarget[] wallpaperTargets,
            RemoteAnimationTarget[] nonAppTargets,
            Runnable onAccepted,
            Runnable onRejected) {
        postAsyncCallback(mHandler, () -> {
            if (mAnimationResult != null
                    && getFactory() instanceof MergeableFactory mergeable
                    && mergeable.onMergeClosing(appTargets, wallpaperTargets, nonAppTargets,
                            mAnimationResult, onAccepted)) {
                return;
            }
            Log.d("LauncherAnimationRunner", "Merge rejected, cancelling the playing animation");
            onRejected.run();
        });
    }

    @BinderThread
    @Override
    public void cancelThenRun(Runnable afterCancel) {
        Log.d("LauncherAnimationRunner", "cancelThenRun");
        postAsyncCallback(mHandler, () -> {
            if (mAnimationResult != null) {
                mAnimationResult.endAnimation();
            }
            finishExistingAnimation();
            getFactory().onAnimationCancelled();
            afterCancel.run();
        });
    }

    /** The animation result that is playing now, if any. */
    private static volatile java.lang.ref.WeakReference<AnimationResult> sPlaying;

    /**
     * Takes the playing animation, such as an app closing into its icon, to its end state and
     * finishes its transition. Called when a new launch is starting, so that the system has
     * committed the end state of the playing transition before it collects the new one.
     */
    @UiThread
    public static void endPlayingAnimation() {
        java.lang.ref.WeakReference<AnimationResult> ref = sPlaying;
        AnimationResult playing = ref != null ? ref.get() : null;
        if (playing != null) {
            Log.d("LauncherAnimationRunner", "endPlayingAnimation");
            playing.endAnimation();
        }
    }

    @UiThread
    private void finishExistingAnimation() {
        if (mAnimationResult != null) {
            mAnimationResult.finish();
            mAnimationResult = null;
        }
    }

    /**
     * Called by the system
     */
    @BinderThread
    @Override
    public void onAnimationCancelled() {
        Log.d("LauncherAnimationRunner", "onAnimationCancelled");
        postAsyncCallback(mHandler, () -> {
            // The transition is superseded, for example by an icon opened while the app is still
            // closing. Jump the playing animation to its end state, otherwise it keeps writing to
            // the leashes while the next animation starts and the two fight over them.
            if (mAnimationResult != null) {
                mAnimationResult.endAnimation();
            }
            finishExistingAnimation();
            getFactory().onAnimationCancelled();
        });
    }

    public void onAnimationCancelled(boolean isKeyguardOccluded) {
        onAnimationCancelled();
    }

    /**
     * Used by RemoteAnimationFactory implementations to run the actual animation and its lifecycle
     * callbacks.
     */
    public static final class AnimationResult extends IRemoteAnimationFinishedCallback.Stub {

        private final Runnable mSyncFinishRunnable;
        private final Runnable mASyncFinishRunnable;

        private AnimatorSet mAnimator;
        private AnimatorListenerAdapter mEndListener;
        private Runnable mOnCompleteCallback;
        private Runnable mCarriedOverCompleteCallback;
        private boolean mFinished = false;
        private boolean mInitialized = false;

        private AnimationResult(Runnable syncFinishRunnable, Runnable asyncFinishRunnable) {
            mSyncFinishRunnable = syncFinishRunnable;
            mASyncFinishRunnable = asyncFinishRunnable;
        }

        @UiThread
        private void finish() {
            Log.d("LauncherAnimationRunner", "AnimationResult.finish finished=" + mFinished);
            if (!mFinished) {
                mSyncFinishRunnable.run();
                UI_HELPER_EXECUTOR.execute(() -> {
                    mASyncFinishRunnable.run();
                    if (mOnCompleteCallback != null) {
                        MAIN_EXECUTOR.execute(mOnCompleteCallback);
                    }
                });
                mFinished = true;
            }
        }

        /** Ends the playing animation at its final state, which also finishes the transition. */
        @UiThread
        private void endAnimation() {
            AnimatorSet animator = mAnimator;
            if (animator != null && !mFinished && animator.isStarted()) {
                animator.end();
            }
        }

        @UiThread
        public void setAnimation(AnimatorSet animation, Context context) {
            setAnimation(animation, context, null, true);
        }

        /**
         * Sets the animation to play for this app launch
         * @param skipFirstFrame Iff true, we skip the first frame of the animation.
         *                       We set to false when skipping first frame causes jank.
         */
        @UiThread
        public void setAnimation(AnimatorSet animation, Context context,
                @Nullable Runnable onCompleteCallback, boolean skipFirstFrame) {
            if (mInitialized) {
                throw new IllegalStateException("Animation already initialized");
            }
            mInitialized = true;
            sPlaying = new java.lang.ref.WeakReference<>(this);
            mAnimator = animation;
            mOnCompleteCallback = onCompleteCallback != null
                    ? onCompleteCallback : mCarriedOverCompleteCallback;
            mCarriedOverCompleteCallback = null;
            if (mAnimator == null) {
                finish();
            } else if (mFinished) {
                // Animation callback was already finished, skip the animation.
                mAnimator.start();
                mAnimator.end();
                if (mOnCompleteCallback != null) {
                    mOnCompleteCallback.run();
                }
            } else {
                // Start the animation
                mEndListener = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finish();
                    }
                };
                mAnimator.addListener(mEndListener);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                    mAnimator.start();
                }

                if (skipFirstFrame) {
                    // Because t=0 has the app icon in its original spot, we can skip the
                    // first frame and have the same movement one frame earlier.
                    Log.d("b/311077782", "LauncherAnimationRunner.setAnimation");
                    mAnimator.setCurrentPlayTime(
                            Math.min(getSingleFrameMs(context), mAnimator.getTotalDuration()));
                }
                if (Utilities.ATLEAST_U) {
                    mAnimator.start();
                }
            }
        }

        /**
         * Stops the playing animation without finishing the transition, so that
         * {@link #setAnimation} can be called again with the animation that takes over. The
         * transition only finishes once that animation ends. The on complete callback of the
         * stopped animation carries over, unless the new animation sets its own.
         */
        @UiThread
        public void beginReplace() {
            if (mFinished) {
                throw new IllegalStateException("Animation already finished");
            }
            AnimatorSet previous = mAnimator;
            mAnimator = null;
            mInitialized = false;
            mCarriedOverCompleteCallback = mOnCompleteCallback;
            if (previous != null) {
                previous.removeListener(mEndListener);
                previous.cancel();
            }
            mEndListener = null;
        }

        /**
         * When used as a simple IRemoteAnimationFinishedCallback, this method is used to run the
         * animation finished runnable.
         */

        @Override
        public void onAnimationFinished() {
            mASyncFinishRunnable.run();
        }
    }

    /**
     * A {@link RemoteAnimationFactory} whose playing animation can hand over to an interrupting
     * transition that returns to Home.
     */
    public interface MergeableFactory {

        /** Whether the playing animation can currently be interrupted. Called on a binder thread. */
        boolean canMergeClosing();

        /**
         * Takes over {@code result} for the interrupting transition, calling {@code onAccepted}
         * once the interrupting transition's windows may be shown. Returns false, without calling
         * it, if the animation can't be taken over.
         */
        @UiThread
        boolean onMergeClosing(RemoteAnimationTarget[] appTargets,
                RemoteAnimationTarget[] wallpaperTargets, RemoteAnimationTarget[] nonAppTargets,
                AnimationResult result, Runnable onAccepted);
    }

    /**
     * Used with LauncherAnimationRunner as an interface for the runner to call back to the
     * implementation.
     */
    public interface RemoteAnimationFactory extends RemoteAnimationDelegate<AnimationResult> {

        /**
         * Called on the UI thread when the animation targets are received. The implementation must
         * call {@link AnimationResult#setAnimation} with the target animation to be run.
         */
        @Override
        @UiThread
        void onAnimationStart(int transit,
                RemoteAnimationTarget[] appTargets,
                RemoteAnimationTarget[] wallpaperTargets,
                RemoteAnimationTarget[] nonAppTargets,
                LauncherAnimationRunner.AnimationResult result);

        @UiThread
        default void onAnimationCancelled(boolean isKeyguardOccluded) {}

        /**
         * Called when the animation is cancelled. This can happen with or without
         * the create being called.
         */
        @Override
        @UiThread
        default void onAnimationCancelled() {}
    }
}
