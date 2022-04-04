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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.UserContactPrefManager

class FallDetectionCalibrationActivity : AppCompatActivity() {
    //Variables
    var SelectedIntensity: String? = ""

    //Layout Texts
    var txtIntensityInfo: TextView? = null

    //Session
    var mUserContactManager: UserContactPrefManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_detection_calibration)
        txtIntensityInfo = findViewById(R.id.txtIntensityInfo)
        mUserContactManager = UserContactPrefManager(this)
        if (!mUserContactManager!!.isUserContactExists) {
            startActivity(Intent(this, RegisterPhoneActivity::class.java))
        }
    }

    //Handle the event to control Fall detection sensor intensity value
    fun clickEventLowIntensity(view: View?) {
        SelectedIntensity = Constants.LOW
        txtIntensityInfo!!.setText(R.string.low_intesity)
    }

    fun clickEventMediumIntensity(view: View?) {
        SelectedIntensity = Constants.MEDIUM
        txtIntensityInfo!!.setText(R.string.medium_intensity)
    }

    fun clickEventHighIntensity(view: View?) {
        SelectedIntensity = Constants.HIGH
        txtIntensityInfo!!.setText(R.string.high_intensity)
    }

    /**
     * Button click event during users confirmation on sensor calibration for fall detection
     * @param view
     */
    fun clickEventCalibrationConfirmation(view: View?) {
        if (SelectedIntensity == Constants.STR_EMPTY) {
            Toast.makeText(this, getString(R.string.select_intensity_level), Toast.LENGTH_SHORT).show()
        } else {
            if (SelectedIntensity == Constants.HIGH) {
                mUserContactManager!!.createSensorIntensity(Constants.LOW_INTENSITY)
            }
            if (SelectedIntensity == Constants.MEDIUM) {
                mUserContactManager!!.createSensorIntensity(Constants.MEDIUM_INTENSITY)
            }
            if (SelectedIntensity == Constants.LOW) {
                mUserContactManager!!.createSensorIntensity(Constants.HIGH_INTENSITY)
            }
            startActivity(Intent(this@FallDetectionCalibrationActivity, RegisterLocationActivity::class.java))
            finish()
        }
    }
}