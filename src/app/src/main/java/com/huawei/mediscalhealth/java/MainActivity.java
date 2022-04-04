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

package com.huawei.mediscalhealth.java;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.ui.FallDetectionActivity;
import com.huawei.mediscalhealth.java.ui.FoodDetectionActivity;
import com.huawei.mediscalhealth.java.ui.RegisterPhoneActivity;
import com.huawei.mediscalhealth.java.ui.bmi.BMIActivity;
import com.huawei.mediscalhealth.java.utils.UserContactPrefManager;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BODY_SENSORS;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.ACTIVITY_RECOGNITION;

public class MainActivity extends AppCompatActivity {
    // User session
    UserContactPrefManager mUserContactMngr;
    private static final int PERMISSIONS_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUserContactMngr = new UserContactPrefManager(this);

        if (!checkingPermission()) {
            requestingPermission();
        } else {
            Toast.makeText(this, "Permission already granted.", Toast.LENGTH_LONG).show();
            if (!mUserContactMngr.isUserContactExists()) {
                startActivity(new Intent(this, RegisterPhoneActivity.class));
            }
        }
    }

    private boolean checkingPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestingPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CAMERA,
        READ_CONTACTS, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CALL_PHONE,
                Manifest.permission.ACTIVITY_RECOGNITION, BODY_SENSORS}, PERMISSIONS_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean mLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean mCameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean mReadContactsAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean mReadExternalStorageAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean mWriteExternalStorageAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean mCallPhoneAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean mActivityRecognitionAccepted = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    boolean mBodySensorsAccepted = grantResults[7] == PackageManager.PERMISSION_GRANTED;

                    if (mLocationAccepted && mCameraAccepted && mReadContactsAccepted && mReadExternalStorageAccepted
                    && mWriteExternalStorageAccepted && mCallPhoneAccepted && mActivityRecognitionAccepted && mBodySensorsAccepted) {
                        Toast.makeText(this, "Permission Granted Successfully", Toast.LENGTH_LONG).show();
                        if (!mUserContactMngr.isUserContactExists()) {
                            startActivity(new Intent(this, RegisterPhoneActivity.class));
                        }
                    } else {
                        Toast.makeText(this, "Permission Denied.", Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showPromptDialog("You need to allow access to these permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dia, int w) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION, CAMERA,
                                                                    READ_CONTACTS, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CALL_PHONE, ACTIVITY_RECOGNITION, BODY_SENSORS},
                                                            PERMISSIONS_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }
                break;
        }
    }

    private void showPromptDialog(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void clickEventFallDetection(View view) {
        startActivity(new Intent(this, FallDetectionActivity.class));
    }

    public void clickEventFoodDetection(View view) {
        startActivity(new Intent(this, FoodDetectionActivity.class));
    }

    public void clickEventBMI(View view) {
        startActivity(new Intent(this, BMIActivity.class));
    }
}