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

package com.huawei.mediscalhealth.java.ui.bmi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;

public class BMIActivity extends AppCompatActivity {
    // Constants
    private static final String PREF_IS_METRIC = "system_of_unit";

    // Layout texts
    TextView txt_result_bmi;
    TextView txt_result_cat;
    AutoCompleteTextView txt_height;
    AutoCompleteTextView txt_weight;
    int HEIGHT_ = 39;
    int WEIGHT_ = 2;
    private static final String TAG ="BMIActivity";

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_m_i);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        txt_height = findViewById(R.id.txt_height);
        txt_weight = findViewById(R.id.txt_weight);
        txt_result_bmi = findViewById(R.id.txt_result_bmi);
        txt_result_cat = findViewById(R.id.txt_result_cat);
        Button btnMoreInfo = findViewById(R.id.btn_more_info);
        btnMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidInput(txt_height) && isValidInput(txt_weight)) {
                    double bmi = calculateBmiAndCastIfNeeded(Integer.parseInt(txt_height.getText().toString()), Integer.parseInt(txt_weight.getText().toString()));
                    Intent i = new Intent(BMIActivity.this, ResultScreenBMIActivity.class);
                    i.putExtra(Constants.BMI_RESULT, bmi);
                    startActivity(i);
                } else {
                    txt_result_bmi.setText(Constants.STR_EMPTY);
                    txt_result_cat.setText(Constants.STR_EMPTY);
                }

            }
        });
        settingSystemOfUnits();
    }

    private boolean isValidInput(EditText editText) {
        return getTextAsDouble(editText) > 0;
    }

    private double getTextAsDouble(EditText editText) {
        String input = editText.getText().toString().replace(',', '.');
        try {
            return Double.valueOf(input);
        } catch (NumberFormatException e) {
            Log.d(TAG, Constants.EXCEPTION_MSG,e);
        }
        return 0;
    }

    private double calculateBmiAndCastIfNeeded(int height, int weight) {

        height = isMetrics() ? height : height / HEIGHT_;
        weight = isMetrics() ? weight : weight / WEIGHT_;
        return calculateBmi(height, weight);
    }

    public static long calculateBmi(int height, int weight) {
        return Math.round(weight / Math.pow(height, 2) * 10) / 10;
    }

    private void settingSystemOfUnits() {
        RadioButton btnMetrics = findViewById(R.id.btn_metric);
        RadioButton btnImperials = findViewById(R.id.btn_imperial);
        btnMetrics.setChecked(isMetrics());
        btnImperials.setChecked(!isMetrics());
        TextInputLayout txtWeightOuters = findViewById(R.id.txt_weight_outer);
        TextInputLayout txtHeightOuters = findViewById(R.id.txt_height_outer);
        txtWeightOuters.setHint(isMetrics() ? getString(R.string.weight_metric) : getString(R.string.weight_imperial));
        txtHeightOuters.setHint(isMetrics() ? getString(R.string.height_metric) : getString(R.string.height_imperial));
    }

    private boolean isMetrics() {
        boolean defaultToMetrics = getString(R.string.default_unit).equals(getString(R.string.metric));
        return sharedPreferences.getBoolean(PREF_IS_METRIC, defaultToMetrics);
    }

    public void settingSystemOfUnits(View view) {
        sharedPreferences.edit().putBoolean(PREF_IS_METRIC, view.getId() == R.id.btn_metric).apply();
        settingSystemOfUnits();
    }
}