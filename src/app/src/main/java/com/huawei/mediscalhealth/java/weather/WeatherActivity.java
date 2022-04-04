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

package com.huawei.mediscalhealth.java.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.model.MediScalSingleton;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;
import com.huawei.mediscalhealth.java.utils.WebURLConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


public class WeatherActivity extends AppCompatActivity {
    private TextView mAirQuality;
    TextView mWalkingTitle;
    TextView mWeatherTemp;
    TextView mWeatherReport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        // To initialize views
        initViews();
    }
    private void initViews() {
        mAirQuality = findViewById(R.id.tv_airQualityIndex);
        mWalkingTitle = findViewById(R.id.walkingTitle);
        mWeatherTemp = findViewById(R.id.tv_weather);
        mWeatherReport = findViewById(R.id.weatherReport);
        // To get air quality details from ambee server
        double latitude = 12.426183;
        double longitude = 76.390479;
        getAirQualityDetails(latitude, longitude);
        // To get weather report
        getWeatherReport(latitude, longitude);
    }
    /**
     * Pass the coordinates to get the weather report
     * @param lat Latitude of the location to obtain weather data
     * @param lng Longitude of the location to obtain weather data
     */
    private void getWeatherReport(double lat, double lng) {
        String weatherKey = String.valueOf(R.string.WEATHER_API_KEY);
        StringRequest weatherRequest = new StringRequest(Request.Method.GET, WebURLConstants.WEATHER + Constants.STR_LAT + lat + Constants.STR_LONG + lng + Constants.STR_APP_ID+weatherKey, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseFromServer = new JSONObject(response);
                    JSONArray weatherArray = responseFromServer.getJSONArray(Constants.WEATHER);
                    JSONObject weather = weatherArray.getJSONObject(Constants.INIT_ZERO);
                    String mDescription = weather.getString(Constants.DESCRIPTION);
                    mDescription = getString(R.string.weather_is) + mDescription;
                    mWeatherReport.setText(mDescription);
                    JSONObject main = responseFromServer.getJSONObject(Constants.MAIN);
                    String mTemp = main.getString(Constants.TEMP);
                    mTemp = mTemp + Constants.STR_TEMP_F;
                    mWeatherTemp.setText(mTemp);
                } catch (JSONException e) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_json), e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_server), error);
                } else if (error instanceof AuthFailureError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_authfail), error);
                } else if (error instanceof ParseError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_parse), error);
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(WeatherActivity.this, getString(R.string.server_maintenance), Toast.LENGTH_LONG).show();
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_no_connection), error);
                } else if (error instanceof NetworkError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_network), error);
                } else if (error instanceof TimeoutError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_timeout), error);
                } else {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_exception), error);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_JSON);
                return params;
            }
        };
        weatherRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.INITIAL_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MediScalSingleton.getInstance(getApplicationContext()).addtorequestqueue(weatherRequest);
    }

    /**
     * Pass coordinates to obtain air quality index values of the area
     * @param lat Latitude of the Location
     * @param lng Longitude of the location
     */
    private void getAirQualityDetails(double lat, double lng) {
        StringRequest airQualityRequest = new StringRequest(Request.Method.GET, WebURLConstants.AIR_QUALITY_INDEX + Constants.STR_LATI + lat + Constants.STR_LNG + lng, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseFromServer = new JSONObject(response);
                    String message = responseFromServer.getString(Constants.MESSAGE);
                    if (message.equals(Constants.SUCCESS)) {
                        JSONArray stations = responseFromServer.getJSONArray(Constants.STATIONS);
                        JSONObject particularStation = stations.getJSONObject(Constants.INIT_ZERO);
                        double mAQIPoint = particularStation.getDouble(Constants.AQI);
                        mAirQuality.setText(String.valueOf(mAQIPoint));
                        if (mAQIPoint > Constants.AQ_POINT_50) {
                            mWalkingTitle.setText(getText(R.string.air_quality_good));
                        }
                        if (mAQIPoint > Constants.AQ_POINT_100) {
                            mWalkingTitle.setText(getString(R.string.air_lightly_polluted));
                        }
                        if (mAQIPoint > Constants.AQ_POINT_150) {
                            mWalkingTitle.setText(getString(R.string.air_quality_bad));
                        }
                    } else {
                        Toast.makeText(WeatherActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_json), e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_server), error);
                } else if (error instanceof AuthFailureError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_authfail), error);
                } else if (error instanceof ParseError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_parse), error);
                } else if (error instanceof NoConnectionError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_no_connection), error);
                } else if (error instanceof NetworkError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_network), error);
                } else if (error instanceof TimeoutError) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_timeout), error);
                } else {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_exception), error);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(Constants.X_API_KEY,String.valueOf(R.string.AQI_VALUES_API_KEY));
                params.put(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_JSON);
                return params;
            }
        };
        airQualityRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.INITIAL_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MediScalSingleton.getInstance(getApplicationContext()).addtorequestqueue(airQualityRequest);
    }

    // To Handle volley response error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, Charset.forName(Constants.UTF_8));
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString(Constants.MESSAGE);
            Toast.makeText(WeatherActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(WeatherActivity.this);
            loginErrorBuilder.setTitle(Constants.ERROR);
            loginErrorBuilder.setMessage(message);
            loginErrorBuilder.setPositiveButton(Constants.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            loginErrorBuilder.show();
        } catch (JSONException e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_json), e);
        }
    }
    public void clickEventObtainAQI(View view) {
        startActivity(new Intent(this,AQIValuesActivity.class));
    }
}