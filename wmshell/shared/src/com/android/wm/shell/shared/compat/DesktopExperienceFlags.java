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
 * Stands in for {@code android.window.DesktopExperienceFlags}. Releases after the one this was compiled
 * against drop the constants of flags that launched, so each constant is looked up on the
 * framework class when it is read, and reads as true when the framework no longer has it.
 */
public final class DesktopExperienceFlags {

    private static final String TAG = "DesktopExperienceFlags";
    private static final String FRAMEWORK_CLASS = "android.window.DesktopExperienceFlags";

    public static final DesktopExperienceFlags DesktopExperienceFlag = new DesktopExperienceFlags("DesktopExperienceFlag");
    public static final DesktopExperienceFlags ENABLE_APP_HANDLE_POSITION_REPORTING = new DesktopExperienceFlags("ENABLE_APP_HANDLE_POSITION_REPORTING");
    public static final DesktopExperienceFlags ENABLE_BUG_FIXES_FOR_SECONDARY_DISPLAY = new DesktopExperienceFlags("ENABLE_BUG_FIXES_FOR_SECONDARY_DISPLAY");
    public static final DesktopExperienceFlags ENABLE_CONNECTED_DISPLAYS_PIP = new DesktopExperienceFlags("ENABLE_CONNECTED_DISPLAYS_PIP");
    public static final DesktopExperienceFlags ENABLE_CONNECTED_DISPLAYS_WINDOW_DRAG = new DesktopExperienceFlags("ENABLE_CONNECTED_DISPLAYS_WINDOW_DRAG");
    public static final DesktopExperienceFlags ENABLE_DEFAULT_DESK_WITHOUT_WARMUP_MIGRATION = new DesktopExperienceFlags("ENABLE_DEFAULT_DESK_WITHOUT_WARMUP_MIGRATION");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_APP_LAUNCH_BUGFIX = new DesktopExperienceFlags("ENABLE_DESKTOP_APP_LAUNCH_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_CLOSE_TASK_ANIMATION_IN_DTC_BUGFIX = new DesktopExperienceFlags("ENABLE_DESKTOP_CLOSE_TASK_ANIMATION_IN_DTC_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_FIRST_BASED_DEFAULT_TO_DESKTOP_BUGFIX = new DesktopExperienceFlags("ENABLE_DESKTOP_FIRST_BASED_DEFAULT_TO_DESKTOP_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_FIRST_BASED_DRAG_TO_MAXIMIZE = new DesktopExperienceFlags("ENABLE_DESKTOP_FIRST_BASED_DRAG_TO_MAXIMIZE");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_FIRST_FULLSCREEN_REFOCUS_BUGFIX = new DesktopExperienceFlags("ENABLE_DESKTOP_FIRST_FULLSCREEN_REFOCUS_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_FIRST_LISTENER = new DesktopExperienceFlags("ENABLE_DESKTOP_FIRST_LISTENER");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_IME_BUGFIX = new DesktopExperienceFlags("ENABLE_DESKTOP_IME_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_TAB_TEARING_LAUNCH_ANIMATION = new DesktopExperienceFlags("ENABLE_DESKTOP_TAB_TEARING_LAUNCH_ANIMATION");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_TASKBAR_ON_FREEFORM_DISPLAYS = new DesktopExperienceFlags("ENABLE_DESKTOP_TASKBAR_ON_FREEFORM_DISPLAYS");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_TASK_LIMIT_SEPARATE_TRANSITION = new DesktopExperienceFlags("ENABLE_DESKTOP_TASK_LIMIT_SEPARATE_TRANSITION");
    public static final DesktopExperienceFlags ENABLE_DESKTOP_WINDOWING_PIP = new DesktopExperienceFlags("ENABLE_DESKTOP_WINDOWING_PIP");
    public static final DesktopExperienceFlags ENABLE_DISPLAY_CONTENT_MODE_MANAGEMENT = new DesktopExperienceFlags("ENABLE_DISPLAY_CONTENT_MODE_MANAGEMENT");
    public static final DesktopExperienceFlags ENABLE_DISPLAY_DISCONNECT_INTERACTION = new DesktopExperienceFlags("ENABLE_DISPLAY_DISCONNECT_INTERACTION");
    public static final DesktopExperienceFlags ENABLE_DISPLAY_FOCUS_IN_SHELL_TRANSITIONS = new DesktopExperienceFlags("ENABLE_DISPLAY_FOCUS_IN_SHELL_TRANSITIONS");
    public static final DesktopExperienceFlags ENABLE_DISPLAY_RECONNECT_INTERACTION = new DesktopExperienceFlags("ENABLE_DISPLAY_RECONNECT_INTERACTION");
    public static final DesktopExperienceFlags ENABLE_DISPLAY_WINDOWING_MODE_SWITCHING = new DesktopExperienceFlags("ENABLE_DISPLAY_WINDOWING_MODE_SWITCHING");
    public static final DesktopExperienceFlags ENABLE_DRAGGING_PIP_ACROSS_DISPLAYS = new DesktopExperienceFlags("ENABLE_DRAGGING_PIP_ACROSS_DISPLAYS");
    public static final DesktopExperienceFlags ENABLE_DRAG_TO_MAXIMIZE = new DesktopExperienceFlags("ENABLE_DRAG_TO_MAXIMIZE");
    public static final DesktopExperienceFlags ENABLE_DYNAMIC_RADIUS_COMPUTATION_BUGFIX = new DesktopExperienceFlags("ENABLE_DYNAMIC_RADIUS_COMPUTATION_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_EMPTY_DESK_ON_MINIMIZE = new DesktopExperienceFlags("ENABLE_EMPTY_DESK_ON_MINIMIZE");
    public static final DesktopExperienceFlags ENABLE_FREEFORM_BOX_SHADOWS = new DesktopExperienceFlags("ENABLE_FREEFORM_BOX_SHADOWS");
    public static final DesktopExperienceFlags ENABLE_FREEFORM_DISPLAY_LAUNCH_PARAMS = new DesktopExperienceFlags("ENABLE_FREEFORM_DISPLAY_LAUNCH_PARAMS");
    public static final DesktopExperienceFlags ENABLE_INDEPENDENT_BACK_IN_PROJECTED = new DesktopExperienceFlags("ENABLE_INDEPENDENT_BACK_IN_PROJECTED");
    public static final DesktopExperienceFlags ENABLE_KEYBOARD_SHORTCUTS_TO_SWITCH_DESKS = new DesktopExperienceFlags("ENABLE_KEYBOARD_SHORTCUTS_TO_SWITCH_DESKS");
    public static final DesktopExperienceFlags ENABLE_MODALS_FULLSCREEN_WITH_PLATFORM_SIGNATURE = new DesktopExperienceFlags("ENABLE_MODALS_FULLSCREEN_WITH_PLATFORM_SIGNATURE");
    public static final DesktopExperienceFlags ENABLE_MOVE_TO_NEXT_DISPLAY_SHORTCUT = new DesktopExperienceFlags("ENABLE_MOVE_TO_NEXT_DISPLAY_SHORTCUT");
    public static final DesktopExperienceFlags ENABLE_MULTIDISPLAY_TRACKPAD_BACK_GESTURE = new DesktopExperienceFlags("ENABLE_MULTIDISPLAY_TRACKPAD_BACK_GESTURE");
    public static final DesktopExperienceFlags ENABLE_MULTIPLE_DESKTOPS_ACTIVATION_IN_DESKTOP_FIRST_DISPLAYS = new DesktopExperienceFlags("ENABLE_MULTIPLE_DESKTOPS_ACTIVATION_IN_DESKTOP_FIRST_DISPLAYS");
    public static final DesktopExperienceFlags ENABLE_MULTIPLE_DESKTOPS_BACKEND = new DesktopExperienceFlags("ENABLE_MULTIPLE_DESKTOPS_BACKEND");
    public static final DesktopExperienceFlags ENABLE_MULTIPLE_DESKTOPS_FRONTEND = new DesktopExperienceFlags("ENABLE_MULTIPLE_DESKTOPS_FRONTEND");
    public static final DesktopExperienceFlags ENABLE_NON_DEFAULT_DISPLAY_SPLIT = new DesktopExperienceFlags("ENABLE_NON_DEFAULT_DISPLAY_SPLIT");
    public static final DesktopExperienceFlags ENABLE_NO_WINDOW_DECORATION_FOR_DESKS = new DesktopExperienceFlags("ENABLE_NO_WINDOW_DECORATION_FOR_DESKS");
    public static final DesktopExperienceFlags ENABLE_PER_DISPLAY_DESKTOP_WALLPAPER_ACTIVITY = new DesktopExperienceFlags("ENABLE_PER_DISPLAY_DESKTOP_WALLPAPER_ACTIVITY");
    public static final DesktopExperienceFlags ENABLE_PINNING_APP_WITH_CONTEXT_MENU = new DesktopExperienceFlags("ENABLE_PINNING_APP_WITH_CONTEXT_MENU");
    public static final DesktopExperienceFlags ENABLE_PROJECTED_DISPLAY_DESKTOP_MODE = new DesktopExperienceFlags("ENABLE_PROJECTED_DISPLAY_DESKTOP_MODE");
    public static final DesktopExperienceFlags ENABLE_REJECT_HOME_TRANSITION = new DesktopExperienceFlags("ENABLE_REJECT_HOME_TRANSITION");
    public static final DesktopExperienceFlags ENABLE_SYS_DECORS_CALLBACKS_VIA_WM = new DesktopExperienceFlags("ENABLE_SYS_DECORS_CALLBACKS_VIA_WM");
    public static final DesktopExperienceFlags ENABLE_TASKBAR_CONNECTED_DISPLAYS = new DesktopExperienceFlags("ENABLE_TASKBAR_CONNECTED_DISPLAYS");
    public static final DesktopExperienceFlags ENABLE_TASKBAR_RECENT_TASKS_THROTTLE_BUGFIX = new DesktopExperienceFlags("ENABLE_TASKBAR_RECENT_TASKS_THROTTLE_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_TILE_RESIZING = new DesktopExperienceFlags("ENABLE_TILE_RESIZING");
    public static final DesktopExperienceFlags ENABLE_WINDOWING_TASK_STACK_ORDER_BUGFIX = new DesktopExperienceFlags("ENABLE_WINDOWING_TASK_STACK_ORDER_BUGFIX");
    public static final DesktopExperienceFlags ENABLE_WINDOWING_TRANSITION_HANDLERS_OBSERVERS = new DesktopExperienceFlags("ENABLE_WINDOWING_TRANSITION_HANDLERS_OBSERVERS");
    public static final DesktopExperienceFlags ENABLE_WINDOW_DECORATION_REFACTOR = new DesktopExperienceFlags("ENABLE_WINDOW_DECORATION_REFACTOR");
    public static final DesktopExperienceFlags ENTER_DESKTOP_BY_DEFAULT_ON_FREEFORM_DISPLAYS = new DesktopExperienceFlags("ENTER_DESKTOP_BY_DEFAULT_ON_FREEFORM_DISPLAYS");
    public static final DesktopExperienceFlags EXCLUDE_DESK_ROOTS_FROM_DESKTOP_TASKS = new DesktopExperienceFlags("EXCLUDE_DESK_ROOTS_FROM_DESKTOP_TASKS");
    public static final DesktopExperienceFlags FORCE_CLOSE_TOP_TRANSPARENT_FULLSCREEN_TASK = new DesktopExperienceFlags("FORCE_CLOSE_TOP_TRANSPARENT_FULLSCREEN_TASK");
    public static final DesktopExperienceFlags FORM_FACTOR_BASED_DESKTOP_FIRST_SWITCH = new DesktopExperienceFlags("FORM_FACTOR_BASED_DESKTOP_FIRST_SWITCH");
    public static final DesktopExperienceFlags SKIP_DEACTIVATION_OF_DESK_WITH_NOTHING_IN_FRONT = new DesktopExperienceFlags("SKIP_DEACTIVATION_OF_DESK_WITH_NOTHING_IN_FRONT");
    /** Whether the developer toggle overrides the flags. */
    public static boolean getToggleOverride() {
        try {
            Method m = Class.forName(FRAMEWORK_CLASS).getMethod("getToggleOverride");
            return (Boolean) m.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    /** The flags registered on the framework class. */
    @SuppressWarnings("unchecked")
    public static java.util.List<android.window.DesktopExperienceFlags.DesktopExperienceFlag>
            getRegisteredFlags() {
        try {
            Method m = Class.forName(FRAMEWORK_CLASS).getMethod("getRegisteredFlags");
            return (java.util.List<android.window.DesktopExperienceFlags.DesktopExperienceFlag>) m.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return java.util.Collections.emptyList();
        }
    }

    /** Every constant of this class. */
    public static java.util.List<DesktopExperienceFlags> getEntries() {
        return java.util.Arrays.asList(DesktopExperienceFlag, ENABLE_APP_HANDLE_POSITION_REPORTING, ENABLE_BUG_FIXES_FOR_SECONDARY_DISPLAY, ENABLE_CONNECTED_DISPLAYS_PIP, ENABLE_CONNECTED_DISPLAYS_WINDOW_DRAG, ENABLE_DEFAULT_DESK_WITHOUT_WARMUP_MIGRATION, ENABLE_DESKTOP_APP_LAUNCH_BUGFIX, ENABLE_DESKTOP_CLOSE_TASK_ANIMATION_IN_DTC_BUGFIX, ENABLE_DESKTOP_FIRST_BASED_DEFAULT_TO_DESKTOP_BUGFIX, ENABLE_DESKTOP_FIRST_BASED_DRAG_TO_MAXIMIZE, ENABLE_DESKTOP_FIRST_FULLSCREEN_REFOCUS_BUGFIX, ENABLE_DESKTOP_FIRST_LISTENER, ENABLE_DESKTOP_IME_BUGFIX, ENABLE_DESKTOP_TAB_TEARING_LAUNCH_ANIMATION, ENABLE_DESKTOP_TASKBAR_ON_FREEFORM_DISPLAYS, ENABLE_DESKTOP_TASK_LIMIT_SEPARATE_TRANSITION, ENABLE_DESKTOP_WINDOWING_PIP, ENABLE_DISPLAY_CONTENT_MODE_MANAGEMENT, ENABLE_DISPLAY_DISCONNECT_INTERACTION, ENABLE_DISPLAY_FOCUS_IN_SHELL_TRANSITIONS, ENABLE_DISPLAY_RECONNECT_INTERACTION, ENABLE_DISPLAY_WINDOWING_MODE_SWITCHING, ENABLE_DRAGGING_PIP_ACROSS_DISPLAYS, ENABLE_DRAG_TO_MAXIMIZE, ENABLE_DYNAMIC_RADIUS_COMPUTATION_BUGFIX, ENABLE_EMPTY_DESK_ON_MINIMIZE, ENABLE_FREEFORM_BOX_SHADOWS, ENABLE_FREEFORM_DISPLAY_LAUNCH_PARAMS, ENABLE_INDEPENDENT_BACK_IN_PROJECTED, ENABLE_KEYBOARD_SHORTCUTS_TO_SWITCH_DESKS, ENABLE_MODALS_FULLSCREEN_WITH_PLATFORM_SIGNATURE, ENABLE_MOVE_TO_NEXT_DISPLAY_SHORTCUT, ENABLE_MULTIDISPLAY_TRACKPAD_BACK_GESTURE, ENABLE_MULTIPLE_DESKTOPS_ACTIVATION_IN_DESKTOP_FIRST_DISPLAYS, ENABLE_MULTIPLE_DESKTOPS_BACKEND, ENABLE_MULTIPLE_DESKTOPS_FRONTEND, ENABLE_NON_DEFAULT_DISPLAY_SPLIT, ENABLE_NO_WINDOW_DECORATION_FOR_DESKS, ENABLE_PER_DISPLAY_DESKTOP_WALLPAPER_ACTIVITY, ENABLE_PINNING_APP_WITH_CONTEXT_MENU, ENABLE_PROJECTED_DISPLAY_DESKTOP_MODE, ENABLE_REJECT_HOME_TRANSITION, ENABLE_SYS_DECORS_CALLBACKS_VIA_WM, ENABLE_TASKBAR_CONNECTED_DISPLAYS, ENABLE_TASKBAR_RECENT_TASKS_THROTTLE_BUGFIX, ENABLE_TILE_RESIZING, ENABLE_WINDOWING_TASK_STACK_ORDER_BUGFIX, ENABLE_WINDOWING_TRANSITION_HANDLERS_OBSERVERS, ENABLE_WINDOW_DECORATION_REFACTOR, ENTER_DESKTOP_BY_DEFAULT_ON_FREEFORM_DISPLAYS, EXCLUDE_DESK_ROOTS_FROM_DESKTOP_TASKS, FORCE_CLOSE_TOP_TRANSPARENT_FULLSCREEN_TASK, FORM_FACTOR_BASED_DESKTOP_FIRST_SWITCH, SKIP_DEACTIVATION_OF_DESK_WITH_NOTHING_IN_FRONT);
    }

    public String getFlagName() {
        return mName;
    }


    private final String mName;
    private Boolean mCached;

    private DesktopExperienceFlags(String name) {
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
