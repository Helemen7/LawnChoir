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

package com.android.wm.shell.shared.compat.android.service.dreams;

import java.lang.reflect.Method;

/** Stands in for {@code android.service.dreams.Flags}, which moved in Android 17. */
public final class Flags {

    private static final String[] CLASSES = {
        "com.android.internal.hidden_from_bootclasspath.android.service.dreams.Flags",
        "android.service.dreams.Flags",
    };

    private Flags() {}

    public static boolean dismissDreamOnKeyguardDismiss() {
        return read("dismissDreamOnKeyguardDismiss");
    }

    private static boolean read(String method) {
        for (String name : CLASSES) {
            try {
                Method m = Class.forName(name).getMethod(method);
                return (Boolean) m.invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // Try the next location.
            } catch (ReflectiveOperationException | RuntimeException e) {
                return true;
            }
        }
        // A flag that launched has no method any more, so it is always on.
        return true;
    }
}
