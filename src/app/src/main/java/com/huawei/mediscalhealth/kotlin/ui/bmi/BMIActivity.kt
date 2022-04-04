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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling

class BMIActivity : AppCompatActivity() {
    //Layout texts
    var txt_result_bmi: TextView? = null
    var txt_result_cat: TextView? = null
    var txt_height: AutoCompleteTextView? = null
    var txt_weight: AutoCompleteTextView? = null
    var HEIGHT_ = 39.37008
    var WEIGHT_ = 2.204623
    var sharedPreferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_b_m_i)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        txt_height = findViewById(R.id.txt_height)
        txt_weight = findViewById(R.id.txt_weight)
        txt_result_bmi = findViewById(R.id.txt_result_bmi)
        txt_result_cat = findViewById(R.id.txt_result_cat)
        val btn_more_info = findViewById<Button>(R.id.btn_more_info)
        btn_more_info.setOnClickListener {
            if (isValidInput(txt_height) && isValidInput(txt_weight)) {
                val bmi = calculateBmiAndCastIfNeeded(getTextAsDouble(txt_height), getTextAsDouble(txt_weight))
                val i = Intent(this@BMIActivity, ResultScreenBMIActivity::class.java)
                i.putExtra(Constants.BMI_RESULT, bmi)
                startActivity(i)
            } else {
                txt_result_bmi?.setText(Constants.STR_EMPTY)
                txt_result_cat?.setText(Constants.STR_EMPTY)
            }
        }
        setSystemOfUnits()
    }

    private fun isValidInput(editText: EditText?): Boolean {
        return getTextAsDouble(editText) > 0
    }

    private fun getTextAsDouble(editText: EditText?): Double {
        val input = editText!!.text.toString().replace(',', '.')
        try {
            return java.lang.Double.valueOf(input)
        } catch (e: NumberFormatException) {
            ExceptionHandling.printExceptionInfo(TAG, e)
        }
        return 0 as Double
    }

    private fun calculateBmiAndCastIfNeeded(height: Double, weight: Double): Double {
        var height = height
        var weight = weight
        height = if (isMetric) height else height / HEIGHT_
        weight = if (isMetric) weight else weight / WEIGHT_
        return calculateBmi(height, weight)
    }

    private fun setSystemOfUnits() {
        val btn_metric = findViewById<RadioButton>(R.id.btn_metric)
        val btn_imperial = findViewById<RadioButton>(R.id.btn_imperial)
        btn_metric.isChecked = isMetric
        btn_imperial.isChecked = !isMetric
        val txt_weight_outer = findViewById<TextInputLayout>(R.id.txt_weight_outer)
        val txt_height_outer = findViewById<TextInputLayout>(R.id.txt_height_outer)
        txt_weight_outer.hint = if (isMetric) getString(R.string.weight_metric) else getString(R.string.weight_imperial)
        txt_height_outer.hint = if (isMetric) getString(R.string.height_metric) else getString(R.string.height_imperial)
    }

    private val isMetric: Boolean
        private get() {
            val defaultToMetric = getString(R.string.default_unit) == getString(R.string.metric)
            return sharedPreferences!!.getBoolean(PREF_IS_METRIC, defaultToMetric)
        }

    fun setSystemOfUnits(v: View) {
        sharedPreferences!!.edit().putBoolean(PREF_IS_METRIC, v.id == R.id.btn_metric).apply()
        setSystemOfUnits()
    }

    companion object {
        //Constants
        private const val PREF_IS_METRIC = "system_of_unit"
        private const val TAG = "BMIActivity"
        fun calculateBmi(height: Double, weight: Double): Double {
            return Math.round(weight / Math.pow(height, 2.0) * 10.0) / 10.0
        }
    }
}