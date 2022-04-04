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
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.app.ActivityCompat
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.MapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling
import com.huawei.mediscalhealth.kotlin.utils.WebURLConstants
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

class NearestHospitalActivity : AppCompatActivity(), OnMapReadyCallback {
    //Constants
    private var mRadius = 10

    //Internal variables
    private var mRadiusText: TextView? = null
    private var isCalledHospitalApi = false
    private var mSearchService: SearchService? = null
    private var mRadiusBar: AppCompatSeekBar? = null
    private var mSites: ArrayList<Site>? = ArrayList()

    //Map/Location component variables
    private var hMap: HuaweiMap? = null
    private var mapView: MapView? = null
    private var mLocationRequest: LocationRequest? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var settingsClient: SettingsClient? = null
    private var mLocationCallback: LocationCallback? = null
    private val mCurrentLocation = Location(Constants.STR_EMPTY)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearest_hospital)
        //get mapview instance
        mapView = findViewById(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        MapsInitializer.setApiKey(WebURLConstants.MAP_API_KEY)
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
        mRadiusText = findViewById(R.id.tv_radius)
        mRadiusBar = findViewById(R.id.sb_radius)
        mRadiusBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mRadius = progress
                mRadiusText?.setText(mRadius.toString() + Constants.STR_KM)
                callNearByHospitalApi(mRadius)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        //Ask location permissions
        checkLocationPermission()
        //To initialize views
        initViews()
        mSearchService = SearchServiceFactory.create(this, getApiKey(WebURLConstants.MAP_API_KEY))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView!!.onSaveInstanceState(mapViewBundle)
    }

    /**
     * Call nearest hospital for contact and details
     * @param mRadius Radius which can be set by the user to check the nearest hospitals
     */
    private fun callNearByHospitalApi(mRadius: Int) {
        val mHospitalRequest = NearbySearchRequest()
        mHospitalRequest.location = Coordinate(mCurrentLocation.latitude, mCurrentLocation.longitude)
        mHospitalRequest.radius = mRadius * Constants.INIT_1000
        mHospitalRequest.poiType = LocationType.HOSPITAL
        mHospitalRequest.language = Constants.LANGUAGE_EN
        val mListener: SearchResultListener<NearbySearchResponse> = object : SearchResultListener<NearbySearchResponse> {
            override fun onSearchResult(nearbySearchResponse: NearbySearchResponse) {
                mSites = ArrayList()
                mSites = nearbySearchResponse.sites as ArrayList<Site>
                if (mSites != null) {
                    for (i in Constants.INIT_ZERO until mSites!!.size) {
                        addHospitalMarkerToMap(mSites!![i])
                    }
                } else {
                    Log.d(TAG, getString(R.string.no_near_hospital))
                }
            }

            override fun onSearchError(searchStatus: SearchStatus) {}
        }
        mSearchService!!.nearbySearch(mHospitalRequest, mListener)
    }

    /**
     * Add markers to the map
     * @param site Add markers to the coordinates explicitly specified
     */
    private fun addHospitalMarkerToMap(site: Site) {
        val hospitalCoordinate = site.location
        val hospitalLatLng = LatLng(hospitalCoordinate.lat, hospitalCoordinate.lng)
        if (hMap != null) {
            hMap!!.addMarker(MarkerOptions().position(hospitalLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_marker)).snippet(site.name).clusterable(true)).showInfoWindow()
        }
    }

    private fun initViews() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = Constants.INTERVAL_10000.toLong()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult != null) {
                    val locations = locationResult.locations
                    if (!locations.isEmpty()) {
                        for (location in locations) {
                            mCurrentLocation.latitude = location.latitude
                            mCurrentLocation.longitude = location.longitude
                            if (!isCalledHospitalApi && location.accuracy < Constants.MAX_ACCURACY) {
                                isCalledHospitalApi = true
                                callNearByHospitalApi(mRadius)
                            }
                            Log.d(TAG, getString(R.string.on_location_result) + location.longitude + Constants.STR_COMMA + location.latitude + Constants.STR_COMMA + location.accuracy)
                        }
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (locationAvailability != null) {
                    val flag = locationAvailability.isLocationAvailable
                    Log.d(TAG, getString(R.string.on_location_availability_is_location_available) + flag)
                }
            }
        }
    }

    //To check location permission
    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.d(TAG, Constants.SDK_VERSION)
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(this, strings, Constants.REQUEST_CODE_1)
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

    override fun onResume() {
        super.onResume()
        requestLocationUpdatesWithCallback()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        removeLocationUpdatesWithCallback()
        mapView!!.onPause()
    }

    private fun requestLocationUpdatesWithCallback() {
        try {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()
            // check devices settings before request location updates.
            settingsClient!!.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener {
                        Log.d(TAG, getString(R.string.check_location_setting_success))
                        //request location updates
                        fusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper()).addOnSuccessListener { Log.d(TAG, getString(R.string.request_loaction_update_with_success_callback)) }.addOnFailureListener { e -> ExceptionHandling.printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e) }
                    }.addOnFailureListener { e ->
                        ExceptionHandling.printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e)
                        val statusCode = (e as ApiException).statusCode
                        if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            val rae = e as ResolvableApiException
                        }
                    }
        } catch (e: Exception) {
            ExceptionHandling.printExceptionInfo(getString(R.string.request_loaction_update_exception_callback), e)
        }
    }

    private fun removeLocationUpdatesWithCallback() {
        try {
            fusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
                    .addOnSuccessListener { Log.d(TAG, getString(R.string.request_loaction_update_with_success_callback)) }
                    .addOnFailureListener { e -> ExceptionHandling.printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e) }
        } catch (e: Exception) {
            ExceptionHandling.printExceptionInfo(getString(R.string.request_loaction_update_exception_callback), e)
        }
    }

    private fun getApiKey(apiKey: String?): String? {
        var encodeKey: String? = Constants.STR_EMPTY
        try {
            encodeKey = URLEncoder.encode(apiKey, Constants.LANGUAGE_EN)
        } catch (e: UnsupportedEncodingException) {
            ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_unsupported_encoding), e)
        }
        return encodeKey
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_1) {
            if (grantResults.size > Constants.INIT_1 && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, getString(R.string.on_request_permission_result_location_successful))
            } else {
                Log.d(TAG, getString(R.string.on_request_permission_result_location_failed))
            }
        }
        if (requestCode == Constants.INIT_2) {
            if (grantResults.size > Constants.INIT_2 && grantResults[Constants.INIT_2] == PackageManager.PERMISSION_GRANTED && grantResults[Constants.INIT_ZERO] == PackageManager.PERMISSION_GRANTED && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, getString(R.string.on_request_permission_result_background_location_successful))
            } else {
                Log.d(TAG, getString(R.string.on_request_permission_result_background_location_failed))
            }
        }
    }

    override fun onMapReady(huaweiMap: HuaweiMap) {
        hMap = huaweiMap
        hMap!!.isMyLocationEnabled = true
        hMap!!.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    companion object {
        private const val TAG = "NearHospitalsActivity"
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    }
}