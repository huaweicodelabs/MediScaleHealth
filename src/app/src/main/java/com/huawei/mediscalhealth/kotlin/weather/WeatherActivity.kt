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


package com.huawei.mediscalhealth.kotlin.weather

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.model.MediScalSingleton
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling
import com.huawei.mediscalhealth.kotlin.utils.WebURLConstants
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*

class WeatherActivity : AppCompatActivity() {
    private var mAirQuality: TextView? = null
    var mWalkingTitle: TextView? = null
    var mWeatherTemp: TextView? = null
    var mWeatherReport: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        //To initialize views
        initViews()
    }

    private fun initViews() {
        mAirQuality = findViewById(R.id.tv_airQualityIndex)
        mWalkingTitle = findViewById(R.id.walkingTitle)
        mWeatherTemp = findViewById(R.id.tv_weather)
        mWeatherReport = findViewById(R.id.weatherReport)
        //To get air quality details from ambee server
        val latitude = 12.426183
        val longitude = 76.390479
        getAirQualityDetails(latitude, longitude)
        //To get weather report
        getWeatherReport(latitude, longitude)
    }

    /**
     * Pass the coordinates to get the weather report
     * @param lat Latitude of the location to obtain weather data
     * @param lng Longitude of the location to obtain weather data
     */
    private fun getWeatherReport(lat: Double, lng: Double) {
        val weatherKey = R.string.WEATHER_API_KEY.toString()
        val weatherRequest: StringRequest = object : StringRequest(Method.GET, WebURLConstants.WEATHER + Constants.STR_LAT + lat + Constants.STR_LONG + lng + Constants.STR_APP_ID + weatherKey, Response.Listener { response ->
            try {
                val responseFromServer = JSONObject(response)
                val weatherArray = responseFromServer.getJSONArray(Constants.WEATHER)
                val weather = weatherArray.getJSONObject(Constants.INIT_ZERO)
                var mDescription = weather.getString(Constants.DESCRIPTION)
                mDescription = getString(R.string.weather_is) + mDescription
                mWeatherReport!!.text = mDescription
                val main = responseFromServer.getJSONObject(Constants.MAIN)
                var mTemp = main.getString(Constants.TEMP)
                mTemp = mTemp + Constants.STR_TEMP_F
                mWeatherTemp!!.text = mTemp
            } catch (e: JSONException) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_json), e)
            }
        }, Response.ErrorListener { error ->
            if (error.networkResponse != null) {
                parseVolleyError(error)
            }
            if (error is ServerError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_server), error)
            } else if (error is AuthFailureError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_authfail), error)
            } else if (error is ParseError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_parse), error)
            } else if (error is NoConnectionError) {
                Toast.makeText(this@WeatherActivity, getString(R.string.server_maintenance), Toast.LENGTH_LONG).show()
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_no_connection), error)
            } else if (error is NetworkError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_network), error)
            } else if (error is TimeoutError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_timeout), error)
            } else {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_exception), error)
            }
        }) {
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params[Constants.CONTENT_TYPE] = Constants.CONTENT_TYPE_JSON
                return params
            }
        }
        weatherRequest.retryPolicy = DefaultRetryPolicy(Constants.INITIAL_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        MediScalSingleton.Companion.getInstance(applicationContext)!!.addtorequestqueue(weatherRequest)
    }

    /**
     * Pass coordinates to obtain air quality index values of the area
     * @param lat Latitude of the Location
     * @param lng Longitude of the location
     */
    private fun getAirQualityDetails(lat: Double, lng: Double) {
        val airQualityRequest: StringRequest = object : StringRequest(Method.GET, WebURLConstants.AIR_QUALITY_INDEX + Constants.STR_LATI + lat + Constants.STR_LNG + lng, Response.Listener { response ->
            try {
                val responseFromServer = JSONObject(response)
                val message = responseFromServer.getString(Constants.MESSAGE)
                if (message == Constants.SUCCESS) {
                    val stations = responseFromServer.getJSONArray(Constants.STATIONS)
                    val particularStation = stations.getJSONObject(Constants.INIT_ZERO)
                    val mAQIPoint = particularStation.getDouble(Constants.AQI)
                    mAirQuality!!.text = mAQIPoint.toString()
                    if (mAQIPoint > Constants.AQ_POINT_50) {
                        mWalkingTitle!!.text = getText(R.string.air_quality_good)
                    }
                    if (mAQIPoint > Constants.AQ_POINT_100) {
                        mWalkingTitle!!.text = getString(R.string.air_lightly_polluted)
                    }
                    if (mAQIPoint > Constants.AQ_POINT_150) {
                        mWalkingTitle!!.text = getString(R.string.air_quality_bad)
                    }
                } else {
                    Toast.makeText(this@WeatherActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: JSONException) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_json), e)
            }
        }, Response.ErrorListener { error ->
            if (error.networkResponse != null) {
                parseVolleyError(error)
            }
            if (error is ServerError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_server), error)
            } else if (error is AuthFailureError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_authfail), error)
            } else if (error is ParseError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_parse), error)
            } else if (error is NoConnectionError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_no_connection), error)
            } else if (error is NetworkError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_network), error)
            } else if (error is TimeoutError) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_timeout), error)
            } else {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_exception), error)
            }
        }) {
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params[Constants.X_API_KEY] = R.string.AQI_VALUES_API_KEY.toString()
                params[Constants.CONTENT_TYPE] = Constants.CONTENT_TYPE_JSON
                return params
            }
        }
        airQualityRequest.retryPolicy = DefaultRetryPolicy(Constants.INITIAL_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        MediScalSingleton.Companion.getInstance(applicationContext)!!.addtorequestqueue(airQualityRequest)
    }

    //To Handle volley response error
    fun parseVolleyError(error: VolleyError) {
        try {
            val responseBody = String(error.networkResponse.data, Charset.forName(Constants.UTF_8))
            val data = JSONObject(responseBody)
            val message = data.getString(Constants.MESSAGE)
            Toast.makeText(this@WeatherActivity, message, Toast.LENGTH_LONG).show()
            val loginErrorBuilder = AlertDialog.Builder(this@WeatherActivity)
            loginErrorBuilder.setTitle(Constants.ERROR)
            loginErrorBuilder.setMessage(message)
            loginErrorBuilder.setPositiveButton(Constants.OK) { dialogInterface, i -> dialogInterface.dismiss() }
            loginErrorBuilder.show()
        } catch (e: JSONException) {
            ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_json), e)
        }
    }

    fun clickEventObtainAQI(view: View?) {
        startActivity(Intent(this, AQIValuesActivity::class.java))
    }
}