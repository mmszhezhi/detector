/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.xuanyutech.vision.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.gms.common.images.Size;

import net.xuanyutech.vision.R;
import net.xuanyutech.vision.camera.CameraSizePair;

/** Utility class to retrieve shared preferences. */
public class PreferenceUtils {

  public static void saveStringPreference(
      Context context, @StringRes int prefKeyId, @Nullable String value) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putString(context.getString(prefKeyId), value)
        .apply();
  }

  public static int getConfirmationTimeMs(Context context) {
    return getIntPref(context, R.string.pref_key_confirmation_time, 1500);
  }

  public static String getConnectionAddress(Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(R.string.pref_key_connection_address);
    return sharedPreferences.getString(prefKey, context.getString(R.string.pref_default_connection_address));
  }

  public static String getUsingModel(Context context) {
    if (getBooleanPref(context, R.string.pref_key_use_custom_model, false)) {
      return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_pref_model), "");
    }
    return "";
  }

  private static int getIntPref(Context context, @StringRes int prefKeyId, int defaultValue) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getInt(prefKey, defaultValue);
  }

  @Nullable
  public static CameraSizePair getUserSpecifiedPreviewSize(Context context) {
    try {
      String previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size);
      String pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size);
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      return new CameraSizePair(
          Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
          Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null)));
    } catch (Exception e) {
      return null;
    }
  }

  private static boolean getBooleanPref(
      Context context, @StringRes int prefKeyId, boolean defaultValue) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getBoolean(prefKey, defaultValue);
  }
}
