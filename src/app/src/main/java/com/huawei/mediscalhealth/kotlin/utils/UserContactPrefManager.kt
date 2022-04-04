/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.huawei.mediscalhealth.kotlin.utils

import android.content.Context
import android.content.SharedPreferences

class UserContactPrefManager(var _context: Context) {
    var PRIVATE_MODE = 0
    var pref: SharedPreferences
    var editor: SharedPreferences.Editor

    //Create login session
    fun createEmergencyContactInfo(primary_name: String?, secondary_name: String?, primary_email: String?, secondary_email: String?) {
        // Storing login value as TRUE
        editor.putBoolean(USER_CONTACT_EXISTS, true)
        // Storing name in pref
        editor.putString(KEY_PRIMARY_NAME, primary_name)
        editor.putString(KEY_SECONDARY_NAME, secondary_name)
        editor.putString(KEY_PRIMARY_NO, primary_email)
        editor.putString(KEY_SECONDARY_NO, secondary_email)
        // commit changes
        editor.commit()
    }

    //Create login session
    fun createLocationData(latitude: String?, longitude: String?) {
        editor.putBoolean(USER_LOCATION_EXISTS, true)
        editor.putString(KEY_LATITUDE, latitude)
        editor.putString(KEY_LONGITUDE, longitude)
        // commit changes
        editor.commit()
    }

    fun createSensorIntensity(intensity: Int) {
        editor.putBoolean(USER_CALIBRATION_EXISTS, true)
        editor.putInt(KEY_INTENSITY, intensity)
        // commit changes
        editor.commit()
    }

    // Check for login
    val isUserContactExists: Boolean
        get() = pref.getBoolean(USER_CONTACT_EXISTS, false)

    // Check for login
    val isUserLocationExists: Boolean
        get() = pref.getBoolean(USER_LOCATION_EXISTS, false)

    // Check for login
    val isUserCalibrationExists: Boolean
        get() = pref.getBoolean(USER_CALIBRATION_EXISTS, false)

    // Check for login
    val primaryContactName: String?
        get() = pref.getString(KEY_PRIMARY_NAME, null)

    val secondaryContactName: String?
        get() = pref.getString(KEY_SECONDARY_NAME, null)

    val primaryContactNo: String?
        get() = pref.getString(KEY_PRIMARY_NO, null)

    val secondaryContactNo: String?
        get() = pref.getString(KEY_SECONDARY_NO, null)

    val keyLatitude: String?
        get() = pref.getString(KEY_LATITUDE, null)

    val keyLongitude: String?
        get() = pref.getString(KEY_LONGITUDE, null)

    val keyIntensity: Int
        get() = pref.getInt(KEY_INTENSITY, 0)

    companion object {
        private const val PREFER_NAME = "ContactsPref"
        private const val USER_CONTACT_EXISTS = "UserExists"
        private const val USER_LOCATION_EXISTS = "LocationExists"
        private const val USER_CALIBRATION_EXISTS = "UserCalibrationExists"

        // User name (make variable public to access from outside)
        const val KEY_PRIMARY_NAME = "primary_name"
        const val KEY_SECONDARY_NAME = "secondary_name"

        // Email address (make variable public to access from outside)
        const val KEY_PRIMARY_NO = "primary_email"
        const val KEY_SECONDARY_NO = "secondary_email"

        //LOCATION DATA
        const val KEY_LATITUDE = "loc_latitude"
        const val KEY_LONGITUDE = "loc_longitude"

        //Sensor calibrated intensity
        const val KEY_INTENSITY = "sensor_intensity"
    }

    // Constructor
    init {
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}