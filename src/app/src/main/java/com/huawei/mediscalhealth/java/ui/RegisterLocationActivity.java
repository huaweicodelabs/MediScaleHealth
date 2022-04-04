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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;
import com.huawei.mediscalhealth.java.utils.UserContactPrefManager;

import static com.huawei.mediscalhealth.java.utils.Constants.ACCESS_BACKGROUND_LOCATION_PERMISSION;

public class RegisterLocationActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationProviderClient;
    TextView tv_Location_coordinates;
    Button Bttn_location_coordinates;
    UserContactPrefManager mUserContactManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_location);
        init();
    }

    private void init() {
        tv_Location_coordinates = findViewById(R.id.activity_register_tv_location_coordinates);
        Bttn_location_coordinates = findViewById(R.id.activity_register_bttn_location_coordinates);
        Bttn_location_coordinates.setText(R.string.get_location_coordinates);
        Bttn_location_coordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterLocationActivity.this,RegisterSuccessPromptActivity.class));
                getLocationCoordinates();
            }
        });
        tv_Location_coordinates.setVisibility(View.INVISIBLE);
        checkLocationPermissions();
    }

    private void getLocationCoordinates() {
        try {
            Task<Location> lastLocation = mFusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        return;
                    }
                    tv_Location_coordinates.setVisibility(View.VISIBLE);
                    tv_Location_coordinates.setText(Constants.LATITUDE +location.getLatitude()+Constants.QRY_COMMA+ Constants.LONGITUDE+location.getLongitude());
                    Bttn_location_coordinates.setText(R.string.next);
                    mUserContactManager = new UserContactPrefManager(RegisterLocationActivity.this);
                    mUserContactManager.createLocationData(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                }
            });
        } catch (Exception e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_exception), e);
        } }

    private void checkLocationPermissions() {
        // You must have the ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission. Otherwise, the location service is unavailable.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] str =
                        {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this, str, Constants.REQUEST_CODE_1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    ACCESS_BACKGROUND_LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                String[] str = {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        ACCESS_BACKGROUND_LOCATION_PERMISSION};
                ActivityCompat.requestPermissions(this, str, Constants.REQUEST_CODE_2);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_1) {
            if (grantResults.length >   Constants.REQUEST_CODE_1 && grantResults[ Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED
                    && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
        if (requestCode == Constants.REQUEST_CODE_2) {
            if (grantResults.length > Constants.INIT_2 && grantResults[Constants.INIT_2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED
                    && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
            } else {
            } } }}