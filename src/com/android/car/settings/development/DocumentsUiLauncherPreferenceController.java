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

package com.android.car.settings.development;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

import android.car.drivingstate.CarUxRestrictions;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.preference.SwitchPreference;

import com.android.car.settings.R;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;

/**
 * Preference controller for enabling the DocumentsUI launcher activity.
 */
public class DocumentsUiLauncherPreferenceController
        extends PreferenceController<SwitchPreference> {

    private static final String DOCUMENTS_UI_PACKAGE =
            "com.android.documentsui";

    private static final String DOCUMENTS_UI_LAUNCHER =
            "com.android.documentsui.LauncherActivity";

    private final ComponentName mLauncherComponent;

    public DocumentsUiLauncherPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);

        mLauncherComponent = new ComponentName(
                DOCUMENTS_UI_PACKAGE,
                DOCUMENTS_UI_LAUNCHER);
    }

    @Override
    protected Class<SwitchPreference> getPreferenceType() {
        return SwitchPreference.class;
    }

    @Override
    protected void updateState(SwitchPreference preference) {
        preference.setChecked(isLauncherEnabled());
    }

    @Override
    protected boolean handlePreferenceChanged(SwitchPreference preference, Object newValue) {
        boolean enabled = (Boolean) newValue;

        int state = enabled
                ? COMPONENT_ENABLED_STATE_ENABLED
                : COMPONENT_ENABLED_STATE_DISABLED;

        getContext().getPackageManager().setComponentEnabledSetting(
                mLauncherComponent,
                state,
                PackageManager.DONT_KILL_APP);

        return true;
    }

    private boolean isLauncherEnabled() {
        int state = getContext().getPackageManager()
                .getComponentEnabledSetting(mLauncherComponent);

        if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            try {
                return getContext().getPackageManager()
                        .getActivityInfo(mLauncherComponent, 0)
                        .enabled;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }

        return state == COMPONENT_ENABLED_STATE_ENABLED;
    }
}
