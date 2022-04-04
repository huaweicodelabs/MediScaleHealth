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

package com.huawei.mediscalhealth.java.utils.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huawei.hms.location.ActivityIdentificationData;
import com.huawei.hms.location.ActivityIdentificationResponse;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;

import java.util.List;

public class ActivityIdentityBroadcastReceiver extends BroadcastReceiver {

    // Constants
    public static final String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";
    private static final String TAG = "LocationReceiver";
    public static boolean isListenActivityIdentification = false;

    /**
     * BroadcastReceiver onReceive method
     * @param context as Context
     * @param intent Triggered when a users activity is identified
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_LOCATION.equals(action)) {
                ActivityIdentificationResponse activityIdentificationResponse = ActivityIdentificationResponse.getDataFromIntent(intent);
                if (activityIdentificationResponse != null && isListenActivityIdentification == true) {
                    Log.d(TAG, context.getString(R.string.activity_recognition_result) + activityIdentificationResponse);
                    List<ActivityIdentificationData> list = activityIdentificationResponse.getActivityIdentificationDatas();
                    for (int i = 0; i < list.size(); i++) {
                        int type = list.get(i).getIdentificationActivity();
                        String activityStatus;
                        Intent statusIntent = new Intent(Constants.STATUS);
                        switch (type) {
                            case Constants.VEHICLE_VALUE:
                                activityStatus = Constants.VEHICLE;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.BIKE_VALUE:
                                activityStatus = Constants.BIKE;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.FOOT_VALUE:
                                activityStatus = Constants.FOOT;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.STILL_VALUE:
                                activityStatus = Constants.STILL;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.OTHERS_VALUE:
                                activityStatus = Constants.OTHERS;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.TILTING_VALUE:
                                activityStatus = Constants.TILTING;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.WALKING_VALUE:
                                activityStatus = Constants.WALKING;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.RUNNING_VALUE:
                                activityStatus = Constants.RUNNING;
                                statusIntent.putExtra(Constants.TYPE,activityStatus);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, R.string.identified_activity + activityStatus, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    public static void addIdentificationListener() {
        isListenActivityIdentification = true;
    }

    public static void removeIdentificationListener() {
        isListenActivityIdentification = false;
    }
}
