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


package com.huawei.mediscalhealth.kotlin.ui

import android.app.PendingIntent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.location.ActivityIdentificationService
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling
import com.huawei.mediscalhealth.kotlin.utils.broadcasts.ActivityIdentityBroadcastReceiver

class ActivityIdentificationActivity : AppCompatActivity() {
    var TAG = "ActivityTransitionUpdate"

    /**
     * Users activity is recognised  and broadcast event is sent to fall detectionActivity
     * @see FallDetectionActivity
     *
     * @param detectionIntervalMillis
     * @param activityIdentificationService
     * @param pendingIntent
     */
    fun requestActivityUpdates(detectionIntervalMillis: Long, activityIdentificationService: ActivityIdentificationService, pendingIntent: PendingIntent?) {
        try {
            ActivityIdentityBroadcastReceiver.Companion.addIdentificationListener()
            activityIdentificationService.createActivityIdentificationUpdates(detectionIntervalMillis, pendingIntent)
                    .addOnSuccessListener { Log.d(TAG, getString(R.string.createActivityIdentificationUpdates_onSuccess)) }
                    .addOnFailureListener { e -> ExceptionHandling.printExceptionInfo(getString(R.string.createActivityIdentificationUpdates_onFailure), e) }
        } catch (e: Exception) {
            ExceptionHandling.printExceptionInfo(getString(R.string.createActivityIdentificationUpdates_exception), e)
        }
    }

    fun removeActivityUpdates(activityIdentificationService: ActivityIdentificationService, pendingIntent: PendingIntent?) {
        try {
            ActivityIdentityBroadcastReceiver.Companion.removeIdentificationListener()
            Log.d(TAG, getString(R.string.start_to_removeActivityUpdates))
            activityIdentificationService.deleteActivityIdentificationUpdates(pendingIntent)
                    .addOnSuccessListener { Log.d(TAG, getString(R.string.deleteActivityIdentificationUpdates_onSuccess)) }
                    .addOnFailureListener { e -> ExceptionHandling.printExceptionInfo(getString(R.string.deleteActivityIdentificationUpdates_onSuccess), e) }
        } catch (e: Exception) {
            ExceptionHandling.printExceptionInfo(getString(R.string.removeActivityUpdates_exception), e)
        }
    }
}