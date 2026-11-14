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

import static com.android.car.settings.common.PreferenceController.AVAILABLE;
import static com.android.car.settings.common.PreferenceController.AVAILABLE_FOR_VIEWING;
import static com.android.car.settings.common.PreferenceController.UNSUPPORTED_ON_DEVICE;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import android.car.drivingstate.CarUxRestrictions;
import android.content.Context;

import androidx.preference.Preference;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.PreferenceControllerTestUtil;
import com.android.car.settings.testutils.TestLifecycleOwner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RaspberryPiPreferenceControllersTest {
    private Context mContext;
    private CarUxRestrictions mRestrictions;
    private FragmentController mFragmentController;

    @Before
    public void setUp() {
        mContext = ApplicationProvider.getApplicationContext();
        mRestrictions = new CarUxRestrictions.Builder(/* reqOpt= */ true,
                CarUxRestrictions.UX_RESTRICTIONS_BASELINE, /* timestamp= */ 0).build();
        mFragmentController = mock(FragmentController.class);
    }

    @Test
    public void entry_isAvailableOnlyOnPi5() {
        RaspberryPiSettingsEntryPreferenceController controller = spy(
                new RaspberryPiSettingsEntryPreferenceController(mContext, "entry",
                        mFragmentController, mRestrictions));
        doReturn(true).when(controller).isRaspberryPi5();
        assertThat(controller.getAvailabilityStatus()).isEqualTo(AVAILABLE);
        doReturn(false).when(controller).isRaspberryPi5();
        assertThat(controller.getAvailabilityStatus()).isEqualTo(UNSUPPORTED_ON_DEVICE);
    }

    @Test
    public void uptime_isReadOnlyAndShowsElapsedTime() {
        RaspberryPiUptimePreferenceController controller = spy(
                new RaspberryPiUptimePreferenceController(mContext, "uptime",
                        mFragmentController, mRestrictions));
        doReturn(3_661_000L).when(controller).getElapsedRealtimeMillis();
        Preference preference = new Preference(mContext);
        PreferenceControllerTestUtil.assignPreference(controller, preference);
        controller.onCreate(new TestLifecycleOwner());

        assertThat(controller.getAvailabilityStatus()).isEqualTo(AVAILABLE_FOR_VIEWING);
        assertThat(preference.getSummary().toString()).isEqualTo("1:01:01");
    }
}
