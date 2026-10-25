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
package com.android.wm.shell.shared.compat;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Stands in for {@code android.window.DesktopModeFlags}. Releases after the one this was compiled
 * against drop the constants of flags that launched, so each constant is looked up on the
 * framework class when it is read, and reads as true when the framework no longer has it.
 */
public final class DesktopModeFlags {

    private static final String TAG = "DesktopModeFlags";
    private static final String FRAMEWORK_CLASS = "android.window.DesktopModeFlags";

    public static final DesktopModeFlags DISABLE_NON_RESIZABLE_APP_SNAP_RESIZE = new DesktopModeFlags("DISABLE_NON_RESIZABLE_APP_SNAP_RESIZE");
    public static final DesktopModeFlags ENABLE_ACCESSIBLE_CUSTOM_HEADERS = new DesktopModeFlags("ENABLE_ACCESSIBLE_CUSTOM_HEADERS");
    public static final DesktopModeFlags ENABLE_APP_HEADER_WITH_TASK_DENSITY = new DesktopModeFlags("ENABLE_APP_HEADER_WITH_TASK_DENSITY");
    public static final DesktopModeFlags ENABLE_CAPTION_COMPAT_INSET_FORCE_CONSUMPTION = new DesktopModeFlags("ENABLE_CAPTION_COMPAT_INSET_FORCE_CONSUMPTION");
    public static final DesktopModeFlags ENABLE_CAPTION_COMPAT_INSET_FORCE_CONSUMPTION_ALWAYS = new DesktopModeFlags("ENABLE_CAPTION_COMPAT_INSET_FORCE_CONSUMPTION_ALWAYS");
    public static final DesktopModeFlags ENABLE_CASCADING_WINDOWS = new DesktopModeFlags("ENABLE_CASCADING_WINDOWS");
    public static final DesktopModeFlags ENABLE_DESKTOP_APP_HANDLE_ANIMATION = new DesktopModeFlags("ENABLE_DESKTOP_APP_HANDLE_ANIMATION");
    public static final DesktopModeFlags ENABLE_DESKTOP_APP_LAUNCH_ALTTAB_TRANSITIONS_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_APP_LAUNCH_ALTTAB_TRANSITIONS_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_APP_LAUNCH_TRANSITIONS_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_APP_LAUNCH_TRANSITIONS_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_COMPAT_UI_VISIBILITY_STATUS = new DesktopModeFlags("ENABLE_DESKTOP_COMPAT_UI_VISIBILITY_STATUS");
    public static final DesktopModeFlags ENABLE_DESKTOP_IMMERSIVE_DRAG_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_IMMERSIVE_DRAG_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_INDICATOR_IN_SEPARATE_THREAD_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_INDICATOR_IN_SEPARATE_THREAD_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_OPENING_DEEPLINK_MINIMIZE_ANIMATION_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_OPENING_DEEPLINK_MINIMIZE_ANIMATION_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_RECENTS_TRANSITIONS_CORNERS_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_RECENTS_TRANSITIONS_CORNERS_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_SKIP_COMPAT_UI_EDUCATION_IN_DESKTOP_MODE_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_SKIP_COMPAT_UI_EDUCATION_IN_DESKTOP_MODE_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_SYSTEM_DIALOGS_TRANSITIONS = new DesktopModeFlags("ENABLE_DESKTOP_SYSTEM_DIALOGS_TRANSITIONS");
    public static final DesktopModeFlags ENABLE_DESKTOP_TAB_TEARING_MINIMIZE_ANIMATION_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_TAB_TEARING_MINIMIZE_ANIMATION_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_TRAMPOLINE_CLOSE_ANIMATION_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_TRAMPOLINE_CLOSE_ANIMATION_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_WALLPAPER_ACTIVITY_FOR_SYSTEM_USER = new DesktopModeFlags("ENABLE_DESKTOP_WALLPAPER_ACTIVITY_FOR_SYSTEM_USER");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_APP_TO_WEB = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_APP_TO_WEB");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_BACK_NAVIGATION = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_BACK_NAVIGATION");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_ENTER_TRANSITIONS_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_ENTER_TRANSITIONS_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_EXIT_BY_MINIMIZE_TRANSITION_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_EXIT_BY_MINIMIZE_TRANSITION_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_EXIT_TRANSITIONS_BUGFIX = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_EXIT_TRANSITIONS_BUGFIX");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_HSUM = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_HSUM");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_MODALS_POLICY = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_MODALS_POLICY");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_MODE = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_MODE");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_MULTI_INSTANCE_FEATURES = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_MULTI_INSTANCE_FEATURES");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_PERSISTENCE = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_PERSISTENCE");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_QUICK_SWITCH = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_QUICK_SWITCH");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_SCVH_CACHE = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_SCVH_CACHE");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_SIZE_CONSTRAINTS = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_SIZE_CONSTRAINTS");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_TASKBAR_RUNNING_APPS = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_TASKBAR_RUNNING_APPS");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_TASK_LIMIT = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_TASK_LIMIT");
    public static final DesktopModeFlags ENABLE_DESKTOP_WINDOWING_WALLPAPER_ACTIVITY = new DesktopModeFlags("ENABLE_DESKTOP_WINDOWING_WALLPAPER_ACTIVITY");
    public static final DesktopModeFlags ENABLE_DRAG_RESIZE_SET_UP_IN_BG_THREAD = new DesktopModeFlags("ENABLE_DRAG_RESIZE_SET_UP_IN_BG_THREAD");
    public static final DesktopModeFlags ENABLE_DRAG_TO_DESKTOP_INCOMING_TRANSITIONS_BUGFIX = new DesktopModeFlags("ENABLE_DRAG_TO_DESKTOP_INCOMING_TRANSITIONS_BUGFIX");
    public static final DesktopModeFlags ENABLE_FULLY_IMMERSIVE_IN_DESKTOP = new DesktopModeFlags("ENABLE_FULLY_IMMERSIVE_IN_DESKTOP");
    public static final DesktopModeFlags ENABLE_HANDLE_INPUT_FIX = new DesktopModeFlags("ENABLE_HANDLE_INPUT_FIX");
    public static final DesktopModeFlags ENABLE_HOLD_TO_DRAG_APP_HANDLE = new DesktopModeFlags("ENABLE_HOLD_TO_DRAG_APP_HANDLE");
    public static final DesktopModeFlags ENABLE_INPUT_LAYER_TRANSITION_FIX = new DesktopModeFlags("ENABLE_INPUT_LAYER_TRANSITION_FIX");
    public static final DesktopModeFlags ENABLE_MINIMIZE_BUTTON = new DesktopModeFlags("ENABLE_MINIMIZE_BUTTON");
    public static final DesktopModeFlags ENABLE_MODALS_FULLSCREEN_WITH_PERMISSIONS = new DesktopModeFlags("ENABLE_MODALS_FULLSCREEN_WITH_PERMISSIONS");
    public static final DesktopModeFlags ENABLE_OPAQUE_BACKGROUND_FOR_TRANSPARENT_WINDOWS = new DesktopModeFlags("ENABLE_OPAQUE_BACKGROUND_FOR_TRANSPARENT_WINDOWS");
    public static final DesktopModeFlags ENABLE_PROJECTED_DISPLAY_DESKTOP_MODE = new DesktopModeFlags("ENABLE_PROJECTED_DISPLAY_DESKTOP_MODE");
    public static final DesktopModeFlags ENABLE_QUICKSWITCH_DESKTOP_SPLIT_BUGFIX = new DesktopModeFlags("ENABLE_QUICKSWITCH_DESKTOP_SPLIT_BUGFIX");
    public static final DesktopModeFlags ENABLE_RESIZING_METRICS = new DesktopModeFlags("ENABLE_RESIZING_METRICS");
    public static final DesktopModeFlags ENABLE_RESTORE_TO_PREVIOUS_SIZE_FROM_DESKTOP_IMMERSIVE = new DesktopModeFlags("ENABLE_RESTORE_TO_PREVIOUS_SIZE_FROM_DESKTOP_IMMERSIVE");
    public static final DesktopModeFlags ENABLE_SHELL_INITIAL_BOUNDS_REGRESSION_BUG_FIX = new DesktopModeFlags("ENABLE_SHELL_INITIAL_BOUNDS_REGRESSION_BUG_FIX");
    public static final DesktopModeFlags ENABLE_START_LAUNCH_TRANSITION_FROM_TASKBAR_BUGFIX = new DesktopModeFlags("ENABLE_START_LAUNCH_TRANSITION_FROM_TASKBAR_BUGFIX");
    public static final DesktopModeFlags ENABLE_TASKBAR_OVERFLOW = new DesktopModeFlags("ENABLE_TASKBAR_OVERFLOW");
    public static final DesktopModeFlags ENABLE_TASKBAR_RECENTS_LAYOUT_TRANSITION = new DesktopModeFlags("ENABLE_TASKBAR_RECENTS_LAYOUT_TRANSITION");
    public static final DesktopModeFlags ENABLE_TASK_RESIZING_KEYBOARD_SHORTCUTS = new DesktopModeFlags("ENABLE_TASK_RESIZING_KEYBOARD_SHORTCUTS");
    public static final DesktopModeFlags ENABLE_TASK_STACK_OBSERVER_IN_SHELL = new DesktopModeFlags("ENABLE_TASK_STACK_OBSERVER_IN_SHELL");
    public static final DesktopModeFlags ENABLE_THEMED_APP_HEADERS = new DesktopModeFlags("ENABLE_THEMED_APP_HEADERS");
    public static final DesktopModeFlags ENABLE_VISUAL_INDICATOR_IN_TRANSITION_BUGFIX = new DesktopModeFlags("ENABLE_VISUAL_INDICATOR_IN_TRANSITION_BUGFIX");
    public static final DesktopModeFlags ENABLE_WINDOWING_DYNAMIC_INITIAL_BOUNDS = new DesktopModeFlags("ENABLE_WINDOWING_DYNAMIC_INITIAL_BOUNDS");
    public static final DesktopModeFlags ENABLE_WINDOWING_EDGE_DRAG_RESIZE = new DesktopModeFlags("ENABLE_WINDOWING_EDGE_DRAG_RESIZE");
    public static final DesktopModeFlags ENABLE_WINDOWING_SCALED_RESIZING = new DesktopModeFlags("ENABLE_WINDOWING_SCALED_RESIZING");
    public static final DesktopModeFlags INCLUDE_TOP_TRANSPARENT_FULLSCREEN_TASK_IN_DESKTOP_HEURISTIC = new DesktopModeFlags("INCLUDE_TOP_TRANSPARENT_FULLSCREEN_TASK_IN_DESKTOP_HEURISTIC");
    public static final DesktopModeFlags INHERIT_TASK_BOUNDS_FOR_TRAMPOLINE_TASK_LAUNCHES = new DesktopModeFlags("INHERIT_TASK_BOUNDS_FOR_TRAMPOLINE_TASK_LAUNCHES");
    public static final DesktopModeFlags SKIP_DECOR_VIEW_RELAYOUT_WHEN_CLOSING_BUGFIX = new DesktopModeFlags("SKIP_DECOR_VIEW_RELAYOUT_WHEN_CLOSING_BUGFIX");
    /** Whether desktop mode is forced on. */
    public static boolean isDesktopModeForcedEnabled() {
        try {
            Method m = Class.forName(FRAMEWORK_CLASS).getMethod("isDesktopModeForcedEnabled");
            return (Boolean) m.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }


    private final String mName;
    private Boolean mCached;

    private DesktopModeFlags(String name) {
        mName = name;
    }

    /** Whether the flag is on. */
    public boolean isTrue() {
        Boolean cached = mCached;
        if (cached == null) {
            cached = resolve();
            mCached = cached;
        }
        return cached;
    }

    private boolean resolve() {
        try {
            Class<?> flags = Class.forName(FRAMEWORK_CLASS);
            Field field = flags.getField(mName);
            Object flag = field.get(null);
            Method isTrue = flag.getClass().getMethod("isTrue");
            return (Boolean) isTrue.invoke(flag);
        } catch (NoSuchFieldException e) {
            // The flag launched and the framework dropped it, so it is always on.
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Log.w(TAG, "Could not read " + mName + ", assuming it is on", e);
            return true;
        }
    }

    @Override
    public String toString() {
        return mName;
    }
}
