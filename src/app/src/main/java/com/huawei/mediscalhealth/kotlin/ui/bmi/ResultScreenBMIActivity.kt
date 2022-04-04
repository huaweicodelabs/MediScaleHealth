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


package com.huawei.mediscalhealth.kotlin.ui.bmi

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants

class ResultScreenBMIActivity : AppCompatActivity() {
    //Layout images
    var skipResultBTN: ImageView? = null
    var bmiFlagImgView: ImageView? = null
    var advice1IMG: ImageView? = null
    var advice2IMG: ImageView? = null
    var advice3IMG: ImageView? = null

    //Layouts containers
    var containerL: LinearLayout? = null

    //TextView
    var bmiValueTV: TextView? = null
    var commentTV: TextView? = null
    var advice1TV: TextView? = null
    var advice2TV: TextView? = null
    var advice3TV: TextView? = null
    var bmiLabelTV: TextView? = null

    //Internal variables
    var BMI_RESULT: String? = null
    var bmi = 0.0
    var bmi_val_low = -1.0
    var bmi_underweight_threshold = 18.5
    var bmi_overweight_threshold = 25.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result_screen_b_m_i)
        initViews()
        if (savedInstanceState == null) {
            val b = intent.extras
            bmi = b!!.getDouble(Constants.BMI_RESULT)
        }
        if (bmi == bmi_val_low) {
            containerL!!.visibility = View.GONE
        } else {
            bmiValueTV!!.text = bmi.toString()
            if (bmi < bmi_underweight_threshold) {
                containerL!!.setBackgroundResource(R.color.colorYellow)
                bmiFlagImgView!!.setImageResource(R.drawable.exclamationark)
                bmiLabelTV!!.setText(R.string.you_are_underweight)
                commentTV!!.setText(R.string.advice_to_increase_weight)
                advice1IMG!!.setImageResource(R.drawable.nowater)
                advice1TV!!.setText(R.string.dont_drink_water_before_meal)
                advice2IMG!!.setImageResource(R.drawable.bigmeal)
                advice2TV!!.setText(R.string.use_bigger_plates)
                advice3TV!!.setText(R.string.get_quality_sleep)
            } else {
                if (bmi > bmi_overweight_threshold) {
                    containerL!!.setBackgroundResource(R.color.colorRed)
                    bmiFlagImgView!!.setImageResource(R.drawable.warning)
                    bmiLabelTV!!.setText(R.string.overweight)
                    commentTV!!.setText(R.string.advice_to_decrease_weight)
                    advice1IMG!!.setImageResource(R.drawable.water)
                    advice1TV!!.setText(R.string.drink_water_half_hour_before)
                    advice2IMG!!.setImageResource(R.drawable.twoeggs)
                    advice2TV!!.setText(R.string.eat_only_two_meals)
                    advice3IMG!!.setImageResource(R.drawable.nosugar)
                    advice3TV!!.setText(R.string.drink_coffee_or_tea)
                }
            }
        }
    }

    private fun initViews() {
        containerL = findViewById(R.id.containerL)
        bmiValueTV = findViewById(R.id.bmiValueTV)
        commentTV = findViewById(R.id.commentTV)
        advice1TV = findViewById(R.id.advice1TV)
        advice2TV = findViewById(R.id.advice2TV)
        advice3TV = findViewById(R.id.advice3TV)
        bmiLabelTV = findViewById(R.id.bmiLabelTV)
        skipResultBTN = findViewById(R.id.skipResultBTN)
        bmiFlagImgView = findViewById(R.id.bmiFlagImgView)
        advice1IMG = findViewById(R.id.advice1IMG)
        advice2IMG = findViewById(R.id.advice2IMG)
        advice3IMG = findViewById(R.id.advice3IMG)
    }
}