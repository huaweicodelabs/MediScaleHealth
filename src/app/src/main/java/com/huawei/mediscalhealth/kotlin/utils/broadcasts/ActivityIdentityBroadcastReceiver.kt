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


package com.huawei.mediscalhealth.kotlin.utils.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.huawei.hms.location.ActivityIdentificationResponse
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants

class ActivityIdentityBroadcastReceiver : BroadcastReceiver() {
    /**
     *
     * @param context
     * @param intent Triggered when a users activity is identified
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_LOCATION == action) {
                val activityIdentificationResponse = ActivityIdentificationResponse.getDataFromIntent(intent)
                if (activityIdentificationResponse != null && isListenActivityIdentification == true) {
                    Log.d(TAG, context.getString(R.string.activity_recognition_result) + activityIdentificationResponse)
                    val list = activityIdentificationResponse.activityIdentificationDatas
                    for (i in list.indices) {
                        val type = list[i].identificationActivity
                        var activity_status: String
                        val statusIntent = Intent(Constants.STATUS)
                        when (type) {
                            Constants.VEHICLE_VALUE -> {
                                activity_status = Constants.VEHICLE
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.BIKE_VALUE -> {
                                activity_status = Constants.BIKE
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.FOOT_VALUE -> {
                                activity_status = Constants.FOOT
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.STILL_VALUE -> {
                                activity_status = Constants.STILL
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.OTHERS_VALUE -> {
                                activity_status = Constants.OTHERS
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.TILTING_VALUE -> {
                                activity_status = Constants.TILTING
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.WALKING_VALUE -> {
                                activity_status = Constants.WALKING
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                            Constants.RUNNING_VALUE -> {
                                activity_status = Constants.RUNNING
                                statusIntent.putExtra(Constants.TYPE, activity_status)
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent)
                                Toast.makeText(context, R.string.identified_activity.toString() + activity_status, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        //Constants
        const val ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION"
        private const val TAG = "LocationReceiver"
        var isListenActivityIdentification = false
        fun addIdentificationListener() {
            isListenActivityIdentification = true
        }

        fun removeIdentificationListener() {
            isListenActivityIdentification = false
        }
    }
}