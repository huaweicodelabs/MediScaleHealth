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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;
import com.huawei.mediscalhealth.java.utils.UserContactPrefManager;


public class SOSActivity extends AppCompatActivity {
    // Layout texts
    TextView counterText;

    // Internal variables
    String phoneNumber = "";
    CountDownTimer timer = null;

    // User session
    UserContactPrefManager mUserContactMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_o_s);
        initViews();
    }

    private void initViews() {
        counterText = findViewById(R.id.counterText);
        mUserContactMgr = new UserContactPrefManager(this);
        phoneNumber = mUserContactMgr.getPrimaryContactNo();
        setTimerForEmergencyTrigger();
    }

    /**
     * The user gets 15 seconds to respond before the device triggers an alarm and a
     * phone call/SMS to the primary and secondary contact
     */
    public void setTimerForEmergencyTrigger() {
        timer = new CountDownTimer(Constants.MILLI_IN_FUTURE_15000, Constants.COUNT_DOWN_INTERVAL) {
            public void onTick(long millisUntilFinished) {
                counterText.setText(getString(R.string.left_time) + millisUntilFinished / Constants.COUNT_DOWN_INTERVAL);
            }
            public void onFinish() {
                counterText.setText(R.string.fall_detection);
                triggerEmergencyAlarms();
            }
        };
        timer.start();
    }
    private void triggerEmergencyAlarms() {
        makeEmergencyCall();
    }

    private void makeEmergencyCall() {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(Constants.TELEPHONE + phoneNumber)).putExtra(Constants.SMS_BODY, getString(R.string.default_content)));
        sendSMS();
    }

    private void sendSMS() {
        try {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.putExtra(Constants.ADDRESS, phoneNumber);
            sendIntent.putExtra(Constants.SMS_BODY, getString(R.string.emergency_message));
            sendIntent.setType(Constants.SMS_TYPE);
            startActivity(sendIntent);
        } catch (Exception e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_exception), e);
        }
    }

    public void clickEventStopAlarm(View view) {
        if (timer != null) {
            timer.cancel();
        }
        startActivity(new Intent(this, FallDetectionActivity.class));
        finish();
    }
    @Override
    public void onBackPressed() {
    }
}