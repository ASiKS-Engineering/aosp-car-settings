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
import android.os.Build;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceController;

/** Shows the Raspberry Pi settings only on the Raspberry Pi 5 device. */
public class RaspberryPiSettingsEntryPreferenceController extends PreferenceController<Preference> {
    public RaspberryPiSettingsEntryPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
    }

    @Override
    protected Class<Preference> getPreferenceType() {
        return Preference.class;
    }

    @Override
    protected int getDefaultAvailabilityStatus() {
        return isRaspberryPi5() ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @VisibleForTesting
    boolean isRaspberryPi5() {
        return "rpi5".equals(Build.DEVICE);
    }
}
