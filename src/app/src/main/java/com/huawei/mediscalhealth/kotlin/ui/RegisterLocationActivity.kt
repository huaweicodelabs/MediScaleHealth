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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.FusedLocationProviderClient
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling
import com.huawei.mediscalhealth.kotlin.utils.UserContactPrefManager

class RegisterLocationActivity : AppCompatActivity() {
    private val mFusedLocationProviderClient: FusedLocationProviderClient? = null
    var tv_Location_coordinates: TextView? = null
    var Bttn_location_coordinates: Button? = null
    var mUserContactManager: UserContactPrefManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_location)
        init()
    }

    private fun init() {
        tv_Location_coordinates = findViewById(R.id.activity_register_tv_location_coordinates)
        Bttn_location_coordinates = findViewById(R.id.activity_register_bttn_location_coordinates)
        Bttn_location_coordinates?.setText(R.string.get_location_coordinates)
        Bttn_location_coordinates?.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@RegisterLocationActivity, RegisterSuccessPromptActivity::class.java))
            locationCoordinates
        })
        tv_Location_coordinates?.setVisibility(View.INVISIBLE)
        checkLocationPermissions()
    }

    private val locationCoordinates: Unit
        private get() {
            try {
                val lastLocation = mFusedLocationProviderClient!!.lastLocation
                lastLocation.addOnSuccessListener(OnSuccessListener { location ->
                    if (location == null) {
                        return@OnSuccessListener
                    }
                    tv_Location_coordinates!!.visibility = View.VISIBLE
                    tv_Location_coordinates!!.text = Constants.LATITUDE + location.latitude + Constants.QRY_COMMA + Constants.LONGITUDE + location.longitude
                    Bttn_location_coordinates!!.setText(R.string.next)
                    mUserContactManager = UserContactPrefManager(this@RegisterLocationActivity)
                    mUserContactManager!!.createLocationData(location.latitude.toString(), location.longitude.toString())
                }).addOnFailureListener { }
            } catch (e: Exception) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_exception), e)
            }
        }

    private fun checkLocationPermissions() {
        //You must have the ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission. Otherwise, the location service is unavailable.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                            Constants.ACCESS_BACKGROUND_LOCATION_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf<String?>(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Constants.ACCESS_BACKGROUND_LOCATION_PERMISSION)
                ActivityCompat.requestPermissions(this, strings, Constants.REQUEST_CODE_2)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_1) {
            if (grantResults.size > Constants.REQUEST_CODE_1 && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
        if (requestCode == Constants.REQUEST_CODE_2) {
            if (grantResults.size > Constants.INIT_2 && grantResults[Constants.INIT_2] == PackageManager.PERMISSION_GRANTED && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }
}