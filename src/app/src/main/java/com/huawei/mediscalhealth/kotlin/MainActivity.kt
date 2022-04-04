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


package com.huawei.mediscalhealth.kotlin

import android.Manifest.permission
import android.Manifest.permission_group
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.ui.FallDetectionActivity
import com.huawei.mediscalhealth.kotlin.ui.FoodDetectionActivity
import com.huawei.mediscalhealth.kotlin.ui.RegisterPhoneActivity
import com.huawei.mediscalhealth.kotlin.ui.bmi.BMIActivity
import com.huawei.mediscalhealth.kotlin.utils.UserContactPrefManager

class MainActivity : AppCompatActivity() {
    //User session
    var mUserContactMngr: UserContactPrefManager? = null
    private val PERMISSIONS_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mUserContactMngr = UserContactPrefManager(this)

        if (!checkingPermission()) {
            requestingPermission()
        } else {
            Toast.makeText(this, "Permission already granted.", Toast.LENGTH_LONG).show()
            if (!mUserContactMngr!!.isUserContactExists) {
                startActivity(Intent(this, com.huawei.mediscalhealth.java.ui.RegisterPhoneActivity::class.java))
            }
        }
    }

    private fun checkingPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestingPermission() {
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission.ACCESS_FINE_LOCATION, permission.CAMERA,
                permission.READ_CONTACTS, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE, permission.CALL_PHONE,
                permission.ACTIVITY_RECOGNITION, permission.BODY_SENSORS), PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> if (grantResults.size > 0) {
                val mLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val mCameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                val mReadContactsAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED
                val mReadExternalStorageAccepted = grantResults[3] == PackageManager.PERMISSION_GRANTED
                val mWriteExternalStorageAccepted = grantResults[4] == PackageManager.PERMISSION_GRANTED
                val mCallPhoneAccepted = grantResults[5] == PackageManager.PERMISSION_GRANTED
                val mActivityRecognitionAccepted = grantResults[6] == PackageManager.PERMISSION_GRANTED
                val mBodySensorsAccepted = grantResults[7] == PackageManager.PERMISSION_GRANTED
                if (mLocationAccepted && mCameraAccepted && mReadContactsAccepted && mReadExternalStorageAccepted
                        && mWriteExternalStorageAccepted && mCallPhoneAccepted && mActivityRecognitionAccepted && mBodySensorsAccepted) {
                    Toast.makeText(this@MainActivity, "Permission Granted Successfully", Toast.LENGTH_LONG).show()
                    if (!mUserContactMngr!!.isUserContactExists) {
                        startActivity(Intent(this@MainActivity, com.huawei.mediscalhealth.java.ui.RegisterPhoneActivity::class.java))
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Permission Denied.", Toast.LENGTH_LONG).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                            showPromptDialog("You need to allow access to these permissions",
                                    DialogInterface.OnClickListener { dia, w ->
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION, permission.CAMERA,
                                                    permission.READ_CONTACTS, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE, permission.CALL_PHONE, permission_group.ACTIVITY_RECOGNITION, permission.BODY_SENSORS),
                                                    PERMISSIONS_REQUEST_CODE)
                                        }
                                    })
                            return
                        }
                    }
                }
            }
        }
    }

    private fun showPromptDialog(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    fun clickEventFallDetection(view: View?) {
        startActivity(Intent(this, FallDetectionActivity::class.java))
    }

    fun clickEventFoodDetection(view: View?) {
        startActivity(Intent(this, FoodDetectionActivity::class.java))
    }

    fun clickEventBMI(view: View?) {
        startActivity(Intent(this, BMIActivity::class.java))
    }
}