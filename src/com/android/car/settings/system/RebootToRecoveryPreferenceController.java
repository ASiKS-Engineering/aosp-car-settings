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

package com.android.car.settings.system;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.os.PowerManager;

import com.android.car.settings.R;
import com.android.car.settings.common.FragmentController;

/**
 * Reboots once with the boot order SD card first, NVMe second.
 *
 * <p>The reboot reason is picked up by the vendor init scripts, which arm the one-shot boot order
 * before the reboot (see set_reboot_order in the device tree).
 */
public class RebootToRecoveryPreferenceController extends PowerActionPreferenceController {

    /** Must match the reason in set_reboot_order.rc (sys.powerctl=reboot,rpi_recovery). */
    private static final String REBOOT_REASON_RPI_RECOVERY = "rpi_recovery";

    private static final String DIALOG_TAG =
            "com.android.car.settings.system.RebootToRecoveryConfirmDialog";

    public RebootToRecoveryPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions,
                R.string.power_reboot_to_recovery_dialog_text, DIALOG_TAG);
    }

    @Override
    protected void performAction(PowerManager powerManager) {
        powerManager.reboot(REBOOT_REASON_RPI_RECOVERY);
    }
}
