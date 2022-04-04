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

object Constants {
    const val CAMERA_PERMISSION = "android.permission.CAMERA"
    const val WRITE_EXTERNAL_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE"
    const val ACCESS_BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION"
    const val MESSAGE = "Message"
    const val ERROR = "Error"
    const val OK = "OK"
    const val WEATHER = "weather"
    const val DESCRIPTION = "description"
    const val MAIN = "main"
    const val TEMP = "temp"
    const val SUCCESS = "Success"
    const val STATUS = "Status"
    const val TYPE = "type"
    const val STATIONS = "stations"
    const val AQI = "AQI"
    const val ADDRESS = "address"
    const val SMS_BODY = "sms_body"
    const val SMS_TYPE = "vnd.android-dir/mms-sms"
    const val TELEPHONE = "tel:"
    const val LATITUDE = "Latitude: "
    const val LONGITUDE = "Longitude: "
    const val IMAGE_TYPE_JPG = ".jpg"
    const val DATE_FORMAT = "yyyyMMddHHmmss"
    const val CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"
    const val X_API_KEY = "x-api-key"
    const val VEHICLE = "VEHICLE"
    const val BIKE = "BIKE"
    const val FOOT = "FOOT"
    const val STILL = "STILL"
    const val OTHERS = "OTHERS"
    const val TILTING = "TILTING"
    const val WALKING = "WALKING"
    const val RUNNING = "RUNNING"
    const val VEHICLE_VALUE = 100
    const val BIKE_VALUE = 101
    const val FOOT_VALUE = 102
    const val STILL_VALUE = 103
    const val OTHERS_VALUE = 104
    const val TILTING_VALUE = 105
    const val WALKING_VALUE = 107
    const val RUNNING_VALUE = 108
    const val LOW = "LOW"
    const val MEDIUM = "MEDIUM"
    const val HIGH = "HIGH"
    const val LOW_INTENSITY = 20
    const val MEDIUM_INTENSITY = 30
    const val HIGH_INTENSITY = 40
    const val THRESHOLD_BOUNDARY_LOW = 138
    const val THRESHOLD_BOUNDARY_MEDIUM = 153
    const val THRESHOLD_BOUNDARY_HIGH = 167
    const val MILLI_IN_FUTURE_15000 = 15000
    const val COUNT_DOWN_INTERVAL = 1000
    const val INITIAL_TIME_OUT = 30000
    const val INIT_1000 = 1000
    const val INIT_ZERO = 0
    const val INTERVAL_10000 = 10000
    const val INIT_1 = 1
    const val INIT_2 = 2
    const val REQUEST_CODE_1 = 1
    const val REQUEST_CODE_2 = 2
    const val EXCEPTION_MSG = "Exception Message"
    const val SDK_VERSION = "sdk < 28 Q"
    const val STR_EMPTY = ""
    const val QRY_AND = " = ? AND "
    const val QRY_EQUAL = " = "
    const val QRY_COMMA = ", "
    const val STR_COMMA = ","
    const val MAX_ACCURACY = 35
    const val STR_LAT = "?lat="
    const val STR_LONG = "&lon="
    const val STR_APP_ID = "&cnt=10&appid="
    const val STR_LNG = "&lng="
    const val STR_LATI = "lat="
    const val FLOAT_8F = 0.8f
    const val STR_TEMP_F = " F"
    const val AQ_POINT_50 = 50
    const val AQ_POINT_100 = 100
    const val AQ_POINT_150 = 150
    const val STR_KM = " Km's"
    const val RANGE_MAX: Float = 150F
    const val DYNAMIC_DATA = "Dynamic Data"
    const val BMI_RESULT = "BMI_RESULT"
    const val UTF_8 = "UTF-8"
    const val BIKE_JSON = "bike.json"
    const val CYCLING_JSON = "cycling.json"
    const val WALKING_JSON = "walking.json"
    const val RUNNING_JSON = "running.json"
    const val STILL_JSON = "still.json"
    const val LANGUAGE_EN = "en"
    const val LABEL_A = "a"
    const val UNIT_KM = "Km's"
}