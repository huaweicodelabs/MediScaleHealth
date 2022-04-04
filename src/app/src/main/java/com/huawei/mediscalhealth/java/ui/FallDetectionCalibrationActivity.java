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
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.UserContactPrefManager;

import static com.huawei.mediscalhealth.java.utils.Constants.HIGH;
import static com.huawei.mediscalhealth.java.utils.Constants.HIGH_INTENSITY;
import static com.huawei.mediscalhealth.java.utils.Constants.LOW;
import static com.huawei.mediscalhealth.java.utils.Constants.LOW_INTENSITY;
import static com.huawei.mediscalhealth.java.utils.Constants.MEDIUM;
import static com.huawei.mediscalhealth.java.utils.Constants.MEDIUM_INTENSITY;

public class FallDetectionCalibrationActivity extends AppCompatActivity {
    // Variables
    String SelectedIntensity ="";

    // Layout Texts
    TextView txtIntensityInfo;

    // Session
    UserContactPrefManager mUserContactManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detection_calibration);
        txtIntensityInfo = findViewById(R.id.txtIntensityInfo);
        mUserContactManager = new UserContactPrefManager(this);
        if(!mUserContactManager.isUserContactExists()) {
            startActivity(new Intent(this,RegisterPhoneActivity.class));
        }
    }

    // Handle the event to control Fall detection sensor intensity value
    public void clickEventLowIntensity(View view) {
        SelectedIntensity = LOW;
        txtIntensityInfo.setText(R.string.low_intesity);
    }
    public void clickEventMediumIntensity(View view) {
        SelectedIntensity = MEDIUM;
        txtIntensityInfo.setText(R.string.medium_intensity);
    }
    public void clickEventHighIntensity(View view) {
        SelectedIntensity = HIGH;
        txtIntensityInfo.setText(R.string.high_intensity);
    }

    /**
     * Button click event during users confirmation on sensor calibration for fall detection
     * @param view as View
     */
    public void clickEventCalibrationConfirmation(View view) {
        if(SelectedIntensity.equals(Constants.STR_EMPTY)) {
            Toast.makeText(this, getString(R.string.select_intensity_level), Toast.LENGTH_SHORT).show();
        }
        else {
            if(SelectedIntensity.equals(HIGH)) {
                mUserContactManager.createSensorIntensity(LOW_INTENSITY);

            }
            if(SelectedIntensity.equals(MEDIUM)) {
                mUserContactManager.createSensorIntensity(MEDIUM_INTENSITY);
            }
            if(SelectedIntensity.equals(LOW)) {
                mUserContactManager.createSensorIntensity(HIGH_INTENSITY);
            }
            startActivity(new Intent(FallDetectionCalibrationActivity.this,RegisterLocationActivity.class));
            finish();
        }
    }
}