package com.google.android.gms.location.sample.backgroundlocationupdates

/*
 * Copyright (C) 2017 The Android Open Source Project
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


import android.content.Context
import android.preference.PreferenceManager


internal object LocationRequestHelper {

    val KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested"

    fun setRequesting(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply()
    }

    fun getRequesting(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false)
    }
}
