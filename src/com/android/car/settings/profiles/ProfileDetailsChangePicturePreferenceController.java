/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.car.settings.profiles;

import android.app.Activity;
import android.car.drivingstate.CarUxRestrictions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.car.settings.R;
import com.android.car.settings.common.FragmentController;
import com.android.car.settings.common.Logger;
import com.android.car.ui.preference.CarUiPreference;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lets the current profile pick an image from the device and use it as profile picture.
 *
 * <p>Only available when the selected profile is the profile of the current process.
 */
public class ProfileDetailsChangePicturePreferenceController
        extends ProfileDetailsBasePreferenceController<CarUiPreference> {

    private static final Logger LOG = new Logger(
            ProfileDetailsChangePicturePreferenceController.class);

    // Only the lower 8 bits of the request code are usable.
    private static final int PICK_IMAGE_REQUEST_CODE = 0x2A;
    private static final int ICON_SIZE_PX = 256;

    private final ProfileHelper mProfileHelper;
    private final UserManager mUserManager;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public ProfileDetailsChangePicturePreferenceController(Context context, String preferenceKey,
            FragmentController fragmentController, CarUxRestrictions uxRestrictions) {
        super(context, preferenceKey, fragmentController, uxRestrictions);
        mProfileHelper = ProfileHelper.getInstance(context);
        mUserManager = UserManager.get(context);
    }

    @Override
    protected Class<CarUiPreference> getPreferenceType() {
        return CarUiPreference.class;
    }

    @Override
    protected int getDefaultAvailabilityStatus() {
        return mProfileHelper.isCurrentProcessUser(getUserInfo())
                ? AVAILABLE
                : DISABLED_FOR_PROFILE;
    }

    @Override
    protected boolean handlePreferenceClicked(CarUiPreference preference) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")
                .addCategory(Intent.CATEGORY_OPENABLE);
        try {
            getFragmentController().startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE,
                    this::processActivityResult);
        } catch (ActivityNotFoundException e) {
            LOG.w("No app available to pick an image", e);
            showToast(R.string.profile_picture_no_picker);
        }
        return true;
    }

    private void processActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != PICK_IMAGE_REQUEST_CODE || resultCode != Activity.RESULT_OK
                || data == null || data.getData() == null) {
            return;
        }
        Uri uri = data.getData();
        int userId = getUserInfo().id;
        Context context = getContext();
        mExecutor.execute(() -> {
            Bitmap icon = loadCircularIcon(context, uri);
            if (icon == null) {
                mMainHandler.post(() -> showToast(R.string.profile_picture_error));
                return;
            }
            mUserManager.setUserIcon(userId, icon);
        });
    }

    @Nullable
    private static Bitmap loadCircularIcon(Context context, Uri uri) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        } catch (IOException | SecurityException e) {
            LOG.w("Could not read picture bounds", e);
            return null;
        }
        int shortSide = Math.min(bounds.outWidth, bounds.outHeight);
        if (shortSide <= 0) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Math.max(1, shortSide / ICON_SIZE_PX);
        Bitmap source;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            source = BitmapFactory.decodeStream(in, null, options);
        } catch (IOException | SecurityException e) {
            LOG.w("Could not decode picture", e);
            return null;
        }
        if (source == null) {
            return null;
        }

        // Center-crop to a square and scale to the icon size.
        int side = Math.min(source.getWidth(), source.getHeight());
        Bitmap square = Bitmap.createBitmap(source, (source.getWidth() - side) / 2,
                (source.getHeight() - side) / 2, side, side);
        Bitmap scaled = Bitmap.createScaledBitmap(square, ICON_SIZE_PX, ICON_SIZE_PX, true);

        // User icons are shown as they are, so apply the circular mask here.
        Bitmap circle = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        new Canvas(circle).drawCircle(ICON_SIZE_PX / 2f, ICON_SIZE_PX / 2f, ICON_SIZE_PX / 2f,
                paint);
        return circle;
    }

    private void showToast(int messageResId) {
        Toast.makeText(getContext(), messageResId, Toast.LENGTH_LONG).show();
    }
}
