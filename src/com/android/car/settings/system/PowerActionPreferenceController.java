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

import android.Manifest;
import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;

import com.android.car.settings.R;
import com.android.car.settings.common.ConfirmationDialogFragment;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.Logger;
import com.android.car.settings.common.PreferenceController;

/**
 * Base class for entries on the power screen: asks for confirmation, then runs a
 * {@link PowerManager} action.
 */
public abstract class PowerActionPreferenceController extends PreferenceController<Preference> {

    private static final Logger LOG = new Logger(PowerActionPreferenceController.class);

    private final PowerManager mPowerManager;
    private final boolean mIsRebootPermissionGranted;
    private final int mDialogMessageResId;
    private final String mDialogTag;

    private final ConfirmationDialogFragment.ConfirmListener mConfirmListener =
            new ConfirmationDialogFragment.ConfirmListener() {
                @Override
                public void onConfirm(@Nullable Bundle arguments) {
                    if (isActionAllowed()) {
                        performAction(mPowerManager);
                    } else {
                        LOG.e("PowerManager or REBOOT permission not available");
                    }
                }
            };

    protected PowerActionPreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions,
            @StringRes int dialogMessageResId, String dialogTag) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
        mPowerManager = context.getSystemService(PowerManager.class);
        mIsRebootPermissionGranted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.REBOOT) == PackageManager.PERMISSION_GRANTED;
        mDialogMessageResId = dialogMessageResId;
        mDialogTag = dialogTag;
    }

    /** Runs the action after the user confirmed. */
    protected abstract void performAction(PowerManager powerManager);

    private boolean isActionAllowed() {
        return mPowerManager != null && mIsRebootPermissionGranted;
    }

    @Override
    protected Class<Preference> getPreferenceType() {
        return Preference.class;
    }

    @Override
    protected int getDefaultAvailabilityStatus() {
        int status = super.getDefaultAvailabilityStatus();
        if (status == AVAILABLE && !isActionAllowed()) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return status;
    }

    @Override
    protected void onCreateInternal() {
        ConfirmationDialogFragment.resetListeners(
                (ConfirmationDialogFragment) getFragmentController().findDialogByTag(mDialogTag),
                mConfirmListener,
                /* rejectListener= */ null,
                /* neutralListener= */ null);
    }

    @Override
    protected boolean handlePreferenceClicked(Preference preference) {
        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(getContext())
                .setMessage(mDialogMessageResId)
                .setPositiveButton(R.string.continue_confirmation, mConfirmListener)
                .setNegativeButton(android.R.string.cancel, /* rejectListener= */ null)
                .build();
        getFragmentController().showDialog(dialog, mDialogTag);
        return true;
    }
}
