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

package com.huawei.mediscalhealth.java.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserContactPrefManager {
    private static final String PREFER_NAME = "ContactsPref";
    private static final String USER_CONTACT_EXISTS = "UserExists";
    private static final String USER_LOCATION_EXISTS = "LocationExists";
    private static final String USER_CALIBRATION_EXISTS = "UserCalibrationExists";

    // User name (make variable public to access from outside)
    public static final String KEY_PRIMARY_NAME = "primary_name";
    public static final String KEY_SECONDARY_NAME = "secondary_name";
    int PRIVATE_MODE = 0;

    // Email address (make variable public to access from outside)
    public static final String KEY_PRIMARY_NO = "primary_email";
    public static final String KEY_SECONDARY_NO = "secondary_email";

    // LOCATION DATA
    public static final String KEY_LATITUDE = "loc_latitude";
    public static final String KEY_LONGITUDE = "loc_longitude";

    // Sensor calibrated intensity
    public static final String KEY_INTENSITY = "sensor_intensity";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Constructor
    public UserContactPrefManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // Create login session
    public void createEmergencyContactInfo(String primary_name, String secondary_name, String primary_email, String secondary_email){
        // Storing login value as TRUE
        editor.putBoolean(USER_CONTACT_EXISTS, true);

        // Storing name in pref
        editor.putString(KEY_PRIMARY_NAME, primary_name);
        editor.putString(KEY_SECONDARY_NAME, secondary_name);
        editor.putString(KEY_PRIMARY_NO, primary_email);
        editor.putString(KEY_SECONDARY_NO, secondary_email);

        // commit changes
        editor.commit();
    }

    // Create login session
    public void createLocationData(String latitude,String longitude){
        editor.putBoolean(USER_LOCATION_EXISTS, true);
        editor.putString(KEY_LATITUDE, latitude);
        editor.putString(KEY_LONGITUDE, longitude);

        // commit changes
        editor.commit();
    }

    public void createSensorIntensity(int intensity){
        editor.putBoolean(USER_CALIBRATION_EXISTS, true);
        editor.putInt(KEY_INTENSITY, intensity);

        // commit changes
        editor.commit();
    }

    // Check for login
    public boolean isUserContactExists(){
        return pref.getBoolean(USER_CONTACT_EXISTS, false);
    }

    // Check for login
    public boolean isUserLocationExists(){
        return pref.getBoolean(USER_LOCATION_EXISTS, false);
    }

    // Check for login
    public boolean isUserCalibrationExists(){
        return pref.getBoolean(USER_CALIBRATION_EXISTS, false);
    }

    // Check for login
    public String getPrimaryContactName(){
        return pref.getString(KEY_PRIMARY_NAME,null);
    }

    public String getSecondaryContactName() {
        return pref.getString(KEY_SECONDARY_NAME,null);
    }
    public String getPrimaryContactNo() {
        return pref.getString(KEY_PRIMARY_NO,null);
    }
    public String getSecondaryContactNo() {
        return pref.getString(KEY_SECONDARY_NO,null);
    }
    public String getKeyLatitude() {
        return pref.getString(KEY_LATITUDE,null);
    }
    public String getKeyLongitude() {
        return pref.getString(KEY_LONGITUDE,null);
    }
    public int getKeyIntensity() {
        return pref.getInt(KEY_INTENSITY,0);
    }
}

