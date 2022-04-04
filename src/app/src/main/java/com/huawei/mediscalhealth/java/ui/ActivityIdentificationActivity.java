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

package com.huawei.mediscalhealth.java.ui;

import android.app.PendingIntent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.ActivityIdentificationService;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;
import com.huawei.mediscalhealth.java.utils.broadcasts.ActivityIdentityBroadcastReceiver;

public class ActivityIdentificationActivity extends AppCompatActivity {
    public String TAG = "ActivityTransitionUpdate";

    /**
     * Users activity is recognised  and broadcast event is sent to fall detectionActivity
     * @see FallDetectionActivity
     * @param detectionIntervalMillis as long
     * @param activityIdentificationService as ActivityIdentificationService
     * @param pendingIntent as PendingIntent
     */
    public void requestActivityUpdates(long detectionIntervalMillis, ActivityIdentificationService activityIdentificationService, PendingIntent pendingIntent) {
        try {
            ActivityIdentityBroadcastReceiver.addIdentificationListener();
            activityIdentificationService.createActivityIdentificationUpdates(detectionIntervalMillis, pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, getString(R.string.createActivityIdentificationUpdates_onSuccess));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.createActivityIdentificationUpdates_onFailure), e);
                        }
                    });
        } catch (Exception e) {
            Log.d(getString(R.string.createActivityIdentificationUpdates_exception), Constants.EXCEPTION_MSG,e);
        }
    }

    public void removeActivityUpdates(ActivityIdentificationService activityIdentificationService, PendingIntent pendingIntent) {

        try {
            ActivityIdentityBroadcastReceiver.removeIdentificationListener();
            Log.d(TAG, getString(R.string.start_to_removeActivityUpdates));
            activityIdentificationService.deleteActivityIdentificationUpdates(pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, getString(R.string.deleteActivityIdentificationUpdates_onSuccess));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.deleteActivityIdentificationUpdates_onSuccess), e);
                        }
                    });
        } catch (Exception e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.removeActivityUpdates_exception), e);
        }
    }
}