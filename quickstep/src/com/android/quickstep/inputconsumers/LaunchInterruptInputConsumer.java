/*
 * Copyright (C) 2026 The Android Open Source Project
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
package com.android.quickstep.inputconsumers;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.android.launcher3.QuickstepTransitionManager;
import com.android.launcher3.uioverrides.QuickstepLauncher;
import com.android.quickstep.InputConsumer;
import com.android.quickstep.LauncherActivityInterface;
import com.android.systemui.shared.system.InputMonitorCompat;

/**
 * Handles a swipe up from the navigation area that starts while an app is still opening from its
 * icon. Starting the usual swipe up handler would make the system finish the opening animation
 * first, so the window would jump to full screen. Going home instead lets the opening animation
 * be taken over by the closing one, from where the window is.
 */
public class LaunchInterruptInputConsumer implements InputConsumer {

    private static final String TAG = "LaunchInterruptConsumer";

    private final Context mContext;
    private final int mDisplayId;
    private final InputMonitorCompat mInputMonitorCompat;
    private final float mTriggerDistance;

    private float mDownY;
    private boolean mTriggered;

    public LaunchInterruptInputConsumer(Context context, int displayId,
            InputMonitorCompat inputMonitorCompat) {
        mContext = context;
        mDisplayId = displayId;
        mInputMonitorCompat = inputMonitorCompat;
        mTriggerDistance = ViewConfiguration.get(context).getScaledTouchSlop() * 2f;
    }

    /** Whether an icon to app launch animation is playing right now. */
    public static boolean isLaunchAnimationRunning() {
        QuickstepLauncher launcher = LauncherActivityInterface.INSTANCE.getCreatedContainer();
        if (launcher == null) {
            return false;
        }
        QuickstepTransitionManager manager = launcher.getAppTransitionManager();
        return manager != null && manager.isAppLaunchInterruptible();
    }

    @Override
    public int getType() {
        return TYPE_LAUNCH_INTERRUPT;
    }

    @Override
    public int getDisplayId() {
        return mDisplayId;
    }

    @Override
    public void onMotionEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownY = ev.getY();
                mTriggered = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mTriggered && mDownY - ev.getY() >= mTriggerDistance) {
                    mTriggered = true;
                    // The app must not get the rest of this gesture.
                    mInputMonitorCompat.pilferPointers();
                    goHome();
                }
                break;
            default:
                break;
        }
    }

    private void goHome() {
        Log.d(TAG, "Swipe up during an app launch, going home");
        QuickstepLauncher launcher = LauncherActivityInterface.INSTANCE.getCreatedContainer();
        QuickstepTransitionManager manager =
                launcher != null ? launcher.getAppTransitionManager() : null;
        if (manager != null) {
            manager.closeLaunchingApp();
        }
    }
}
