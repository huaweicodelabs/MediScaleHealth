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
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling
import com.huawei.mediscalhealth.kotlin.utils.UserContactPrefManager

class SOSActivity : AppCompatActivity() {
    //Layout texts
    var counterText: TextView? = null

    //Internal variables
    var phoneNumber: String? = ""
    var timer: CountDownTimer? = null

    //User session
    var mUserContactMgr: UserContactPrefManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_s_o_s)
        initViews()
    }

    private fun initViews() {
        counterText = findViewById(R.id.counterText)
        mUserContactMgr = UserContactPrefManager(this)
        phoneNumber = mUserContactMgr?.primaryContactNo
        setTimerForEmergencyTrigger()
    }

    /**
     * The user gets 15 seconds to respond before the device triggers an alarm and a
     * phone call/SMS to the primary and secondary contact
     */
    fun setTimerForEmergencyTrigger() {
        timer = object : CountDownTimer(Constants.MILLI_IN_FUTURE_15000.toLong(), Constants.COUNT_DOWN_INTERVAL.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                counterText!!.text = getString(R.string.left_time) + millisUntilFinished / Constants.COUNT_DOWN_INTERVAL
            }

            override fun onFinish() {
                counterText!!.setText(R.string.fall_detection)
                triggerEmergencyAlarms()
            }
        }
        timer?.start()
    }

    private fun triggerEmergencyAlarms() {
        makeEmergencyCall()
    }

    private fun makeEmergencyCall() {
        startActivity(Intent(Intent.ACTION_CALL, Uri.parse(Constants.TELEPHONE + phoneNumber)).putExtra(Constants.SMS_BODY, getString(R.string.default_content)))
        sendSMS()
    }

    private fun sendSMS() {
        try {
            val sendIntent = Intent(Intent.ACTION_VIEW)
            sendIntent.putExtra(Constants.ADDRESS, phoneNumber)
            sendIntent.putExtra(Constants.SMS_BODY, getString(R.string.emergency_message))
            sendIntent.type = Constants.SMS_TYPE
            startActivity(sendIntent)
        } catch (e: Exception) {
            ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_exception), e)
        }
    }

    fun clickEventStopAlarm(view: View?) {
        if (timer != null) {
            timer!!.cancel()
        }
        startActivity(Intent(this, FallDetectionActivity::class.java))
        finish()
    }

    override fun onBackPressed() {}
}