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

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.airbnb.lottie.LottieAnimationView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.UserContactPrefManager
import com.huawei.mediscalhealth.kotlin.weather.WeatherActivity
import java.util.*

class FallDetectionActivity : AppCompatActivity(), SensorEventListener {
    //Sensor
    private var sensor: Sensor? = null
    private var sensorManager: SensorManager? = null

    //internal variables
    private var firstChange = true
    private var thresholdBoundary = 0

    //User session
    var mUserContactManager: UserContactPrefManager? = null

    //Broadcasts
    private var mRecognitionBroadcastReceiver: BroadcastReceiver? = null
    private var mChart: LineChart? = null
    private var lottieAnimationView: LottieAnimationView? = null
    var ARRAY_INDEX_FIRST = 0
    var ARRAY_INDEX_SECOND = 1
    var ARRAY_INDEX_THIRD = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fall_detection)
        initialUI()
        initialSensor()
        mRecognitionBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Constants.STATUS) {
                    val type = intent.getStringExtra(Constants.TYPE)
                    changeLottieAnimation(type)
                }
            }
        }
    }

    /**
     * Change animation based on users activity
     *
     * @param type Get the type of action identified to be rendered to Lottie animations
     *
     */
    //fall detection service change in animation
    @SuppressLint("ResourceType")
    private fun changeLottieAnimation(type: String) {
        when (type) {
            Constants.VEHICLE -> lottieAnimationView!!.setAnimation(Constants.BIKE_JSON)
            Constants.BIKE -> lottieAnimationView!!.setAnimation(Constants.CYCLING_JSON)
            Constants.FOOT -> lottieAnimationView!!.setAnimation(Constants.WALKING_JSON)
            Constants.RUNNING -> lottieAnimationView!!.setAnimation(Constants.RUNNING_JSON)
            else -> lottieAnimationView!!.setAnimation(Constants.STILL_JSON)
        }
    }

    fun initialUI() {
        mUserContactManager = UserContactPrefManager(this)
        if (mUserContactManager?.keyIntensity != 0) {
            val SelectedIntensity = mUserContactManager?.keyIntensity
            if (SelectedIntensity == Constants.LOW_INTENSITY) {
                thresholdBoundary = Constants.THRESHOLD_BOUNDARY_LOW
            }
            if (SelectedIntensity == Constants.MEDIUM_INTENSITY) {
                thresholdBoundary = Constants.THRESHOLD_BOUNDARY_MEDIUM
            }
            if (SelectedIntensity == Constants.HIGH_INTENSITY) {
                thresholdBoundary = Constants.THRESHOLD_BOUNDARY_HIGH
            }
        } else {
            startActivity(Intent(this, FallDetectionCalibrationActivity::class.java))
        }
        if (mUserContactManager?.primaryContactNo != null) {
            //Stat values
            val phoneNumber = mUserContactManager?.primaryContactNo
        } else {
            startActivity(Intent(this, RegisterPhoneActivity::class.java))
        }
        lottieAnimationView = findViewById(R.id.lav_recognition)
        val legendEntryA = LegendEntry()
        legendEntryA.label = Constants.LABEL_A
        legendEntryA.formColor = Color.GREEN
        mChart = findViewById(R.id.chart)
        mChart?.setTouchEnabled(false)
        mChart?.setPinchZoom(false)
        //To hide grid lines of the chart
        mChart?.getXAxis()?.setDrawGridLines(true)
        mChart?.getAxisLeft()?.setDrawGridLines(true)
        mChart?.getAxisRight()?.setDrawGridLines(true)
        mChart?.setDrawGridBackground(true)
        mChart?.getXAxis()?.setDrawAxisLine(false)
        mChart?.getAxisLeft()?.setDrawAxisLine(true)
        mChart?.getAxisLeft()?.setDrawGridLines(true)
        mChart?.getAxisRight()?.setDrawAxisLine(true)
        mChart?.getAxisRight()?.setDrawGridLines(true)
        val mDescription = Description()
        mDescription.text = Constants.STR_EMPTY
        mChart?.setDescription(mDescription)
        mChart?.getAxisLeft()?.setDrawLabels(false)
        mChart?.getAxisRight()?.setDrawLabels(false)
        mChart?.getXAxis()?.setDrawLabels(false)
        mChart?.setData(baseLineData)
    }

    //To create base straight line for graph
    private val baseLineData: LineData
        private get() {
            val arrayList = ArrayList<Entry>()
            for (i in 0..198) {
                arrayList.add(Entry(i.toFloat(), 0.0f))
            }
            val customLineDataSet = LineDataSet(arrayList, null)
            customLineDataSet.setDrawCircles(false)
            customLineDataSet.setDrawValues(true)
            customLineDataSet.color = resources.getColor(android.R.color.holo_red_dark)
            customLineDataSet.lineWidth = 4.5f
            customLineDataSet.axisDependency = YAxis.AxisDependency.RIGHT
            return LineData(customLineDataSet)
        }

    fun initialSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        val gravity_updated_values = DoubleArray(3)
        val gravity = DoubleArray(3)
        gravity_updated_values[ARRAY_INDEX_FIRST] = gravity[ARRAY_INDEX_FIRST]
        gravity_updated_values[ARRAY_INDEX_SECOND] = gravity[ARRAY_INDEX_SECOND]
        gravity_updated_values[ARRAY_INDEX_THIRD] = gravity[ARRAY_INDEX_THIRD]
        gravity[ARRAY_INDEX_FIRST] = event.values[ARRAY_INDEX_FIRST].toDouble()
        gravity[ARRAY_INDEX_SECOND] = event.values[ARRAY_INDEX_SECOND].toDouble()
        gravity[ARRAY_INDEX_THIRD] = event.values[ARRAY_INDEX_THIRD].toDouble()
        val updatedAmount = Math.pow(gravity[ARRAY_INDEX_FIRST] - gravity_updated_values[ARRAY_INDEX_FIRST], Constants.INIT_2.toDouble()) +
                Math.pow(gravity[ARRAY_INDEX_SECOND] - gravity_updated_values[ARRAY_INDEX_SECOND], Constants.INIT_2.toDouble()) +
                Math.pow(gravity[ARRAY_INDEX_THIRD] - gravity_updated_values[ARRAY_INDEX_THIRD], Constants.INIT_2.toDouble())
        if (!firstChange && updatedAmount >= thresholdBoundary) {
            startActivity(Intent(this, SOSActivity::class.java))
            finish()
        }
        firstChange = false
        addEntry(event)
    }

    private fun addEntry(event: SensorEvent) {
        val data = mChart!!.data
        if (data != null) {
            var set = data.getDataSetByIndex(Constants.INIT_ZERO)
            if (set == null) {
                set = createSet()
                data.addDataSet(set)
            }
            data.addEntry(Entry(set.entryCount.toFloat(), event.values[ARRAY_INDEX_FIRST] + 5), 0)
            data.notifyDataChanged()
            mChart!!.notifyDataSetChanged()
            mChart!!.setVisibleXRangeMaximum(Constants.RANGE_MAX)
            mChart!!.moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun createSet(): LineDataSet {
        val set = LineDataSet(null, Constants.DYNAMIC_DATA)
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 3f
        set.color = Color.MAGENTA
        set.isHighlightEnabled = false
        set.setDrawValues(false)
        set.setDrawCircles(false)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.cubicIntensity = 0.2f
        return set
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mRecognitionBroadcastReceiver!!,
                IntentFilter(Constants.STATUS))
        sensorManager!!.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onStop() {
        super.onStop()
        sensorManager!!.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager!!.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecognitionBroadcastReceiver!!)
        sensorManager!!.unregisterListener(this)
    }

    fun clickEventFindNearestHospital(view: View?) {
        startActivity(Intent(this, NearestHospitalActivity::class.java))
    }

    fun clickEventWeatherActivity(view: View?) {
        startActivity(Intent(this, WeatherActivity::class.java))
    }

    fun clickEventSOS(view: View?) {
        startActivity(Intent(this, SOSActivity::class.java))
    }
}