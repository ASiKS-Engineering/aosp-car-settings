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

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.provider.Settings;

import androidx.preference.SwitchPreference;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;

/**
 * Developer option that shows or hides the "Power" entry in the system settings.
 */
public class PowerSettingsEnabledPreferenceController
        extends PreferenceController<SwitchPreference> {

    /** Settings.Global key. 1 = the "Power" settings entry is shown. */
    public static final String SETTING_POWER_SETTINGS_ENABLED = "power_settings_enabled";

    public PowerSettingsEnabledPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
    }

    /** Returns whether the "Power" settings entry should be shown. */
    public static boolean isPowerSettingsEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                SETTING_POWER_SETTINGS_ENABLED, 0) == 1;
    }

    @Override
    protected Class<SwitchPreference> getPreferenceType() {
        return SwitchPreference.class;
    }

    @Override
    protected void updateState(SwitchPreference preference) {
        preference.setChecked(isPowerSettingsEnabled(getContext()));
    }

    @Override
    protected boolean handlePreferenceChanged(SwitchPreference preference, Object newValue) {
        return Settings.Global.putInt(getContext().getContentResolver(),
                SETTING_POWER_SETTINGS_ENABLED, ((Boolean) newValue) ? 1 : 0);
    }
}
