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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.UserContactPrefManager;
import com.huawei.mediscalhealth.java.weather.WeatherActivity;

import java.util.ArrayList;

public class FallDetectionActivity extends AppCompatActivity implements SensorEventListener {
    // Sensor
    private Sensor sensor;
    private SensorManager sensorManager;

    // Internal variables
    private boolean firstChange = true;
    private int thresholdBoundary = 0;

    // User session
    UserContactPrefManager mUserContactManager;

    // Broadcasts
    private BroadcastReceiver mRecognitionBroadcastReceiver;

    private LineChart mChart;
    private LottieAnimationView lottieAnimationView;

    int ARRAY_INDEX_FIRST = 0;
    int ARRAY_INDEX_SECOND = 1;
    int ARRAY_INDEX_THIRD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detection);
        initialUI();
        initialSensor();
        mRecognitionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.STATUS)) {
                    String type = intent.getStringExtra(Constants.TYPE);
                    changeLottieAnimation(type);
                }
            }
        };
    }
    /**
     * Change animation based on users activity
     *
     * @param type Get the type of action identified to be rendered to Lottie animations
     *
     */
    @SuppressLint("ResourceType")
    private void changeLottieAnimation(String type) {
        // fall detection service change in animation
        switch (type) {
            case Constants.VEHICLE:
                lottieAnimationView.setAnimation(Constants.BIKE_JSON);
                break;
            case Constants.BIKE:
                lottieAnimationView.setAnimation(Constants.CYCLING_JSON);
                break;
            case Constants.FOOT:
                lottieAnimationView.setAnimation(Constants.WALKING_JSON);
                break;
            case Constants.RUNNING:
                lottieAnimationView.setAnimation(Constants.RUNNING_JSON);
                break;
            default:
                lottieAnimationView.setAnimation(Constants.STILL_JSON);
                break;
        }
    }
    public void initialUI() {
        mUserContactManager = new UserContactPrefManager(this);
        if(mUserContactManager.getKeyIntensity() != 0) {
            int selectedIntensity = mUserContactManager.getKeyIntensity();
            if(selectedIntensity == Constants.LOW_INTENSITY) {
                thresholdBoundary = Constants.THRESHOLD_BOUNDARY_LOW;
            }
            if(selectedIntensity == Constants.MEDIUM_INTENSITY) {
                thresholdBoundary = Constants.THRESHOLD_BOUNDARY_MEDIUM;
            }
            if(selectedIntensity == Constants.HIGH_INTENSITY) {
                thresholdBoundary = Constants.THRESHOLD_BOUNDARY_HIGH;
            }
        }
        else {
            startActivity(new Intent(this, FallDetectionCalibrationActivity.class));
        }

        if(mUserContactManager.getPrimaryContactNo()!=null) {
            // Stat values
            String phoneNumber = mUserContactManager.getPrimaryContactNo();
        }
        else {
            startActivity(new Intent(this, RegisterPhoneActivity.class));
        }
        lottieAnimationView = findViewById(R.id.lav_recognition);
        LegendEntry legendEntryA = new LegendEntry();
        legendEntryA.label = Constants.LABEL_A;
        legendEntryA.formColor = Color.GREEN;
        mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(false);
        mChart.setPinchZoom(false);
        // To hide grid lines of the chart
        mChart.getXAxis().setDrawGridLines(true);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getAxisRight().setDrawGridLines(true);
        mChart.setDrawGridBackground(true);
        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawAxisLine(true);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getAxisRight().setDrawAxisLine(true);
        mChart.getAxisRight().setDrawGridLines(true);
        Description mDescription = new Description();
        mDescription.setText(Constants.STR_EMPTY);
        mChart.setDescription(mDescription);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getAxisRight().setDrawLabels(false);
        mChart.getXAxis().setDrawLabels(false);
        mChart.setData(getBaseLineData());
    }

    // To create base straight line for graph
    private LineData getBaseLineData() {
        ArrayList<Entry> arrayList = new ArrayList<>();
        for (int i = 0; i <199 ; i++) {
            arrayList.add(new Entry((float) i,0.0f));
        }
        LineDataSet customLineDataSet = new LineDataSet(arrayList, null);
        customLineDataSet.setDrawCircles(false);
        customLineDataSet.setDrawValues(true);
        customLineDataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
        customLineDataSet.setLineWidth(4.5f);
        customLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        return new LineData(customLineDataSet);
    }
    public void initialSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double[] gravityUpdatedValues = new double[3];
        double[] gravity = new double[3];

        gravityUpdatedValues[ARRAY_INDEX_FIRST] = gravity[ARRAY_INDEX_FIRST];
        gravityUpdatedValues[ARRAY_INDEX_SECOND] = gravity[ARRAY_INDEX_SECOND];
        gravityUpdatedValues[ARRAY_INDEX_THIRD] = gravity[ARRAY_INDEX_THIRD];
        gravity[ARRAY_INDEX_FIRST] = event.values[ARRAY_INDEX_FIRST];
        gravity[ARRAY_INDEX_SECOND] = event.values[ARRAY_INDEX_SECOND];
        gravity[ARRAY_INDEX_THIRD] = event.values[ARRAY_INDEX_THIRD];
        double updatedAmount = Math.pow((gravity[ARRAY_INDEX_FIRST] - gravityUpdatedValues[ARRAY_INDEX_FIRST]), Constants.INIT_2) +
                Math.pow((gravity[ARRAY_INDEX_SECOND] - gravityUpdatedValues[ARRAY_INDEX_SECOND]), Constants.INIT_2) +
                Math.pow((gravity[ARRAY_INDEX_THIRD] - gravityUpdatedValues[ARRAY_INDEX_THIRD]), Constants.INIT_2);
        if (!firstChange && updatedAmount >= thresholdBoundary) {
            startActivity(new Intent(this,SOSActivity.class));
            finish();
        }
        firstChange = false;
        addEntry(event);
    }
    private void addEntry(SensorEvent event) {
        LineData data = mChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(Constants.INIT_ZERO);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), event.values[ARRAY_INDEX_FIRST] + 5), 0);
            data.notifyDataChanged();
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(Constants.RANGE_MAX);
            mChart.moveViewToX(data.getEntryCount());
        }
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, Constants.DYNAMIC_DATA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRecognitionBroadcastReceiver,
                new IntentFilter(Constants.STATUS));
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecognitionBroadcastReceiver);
        sensorManager.unregisterListener(this);
    }
    public void clickEventFindNearestHospital(View view) {
        startActivity(new Intent(this, NearestHospitalActivity.class));
    }
    public void clickEventWeatherActivity(View view) {
        startActivity(new Intent(this, WeatherActivity.class));
    }
    public void clickEventSOS(View view) {
        startActivity(new Intent(this,SOSActivity.class));
    }
}