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
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.site.api.SearchResultListener;
import com.huawei.hms.site.api.SearchService;
import com.huawei.hms.site.api.SearchServiceFactory;
import com.huawei.hms.site.api.model.Coordinate;
import com.huawei.hms.site.api.model.LocationType;
import com.huawei.hms.site.api.model.NearbySearchRequest;
import com.huawei.hms.site.api.model.NearbySearchResponse;
import com.huawei.hms.site.api.model.SearchStatus;
import com.huawei.hms.site.api.model.Site;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.huawei.mediscalhealth.java.utils.Constants.ACCESS_BACKGROUND_LOCATION_PERMISSION;
import static com.huawei.mediscalhealth.java.utils.Constants.INIT_ZERO;
import static com.huawei.mediscalhealth.java.utils.WebURLConstants.MAP_API_KEY;

public class NearestHospitalActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Constants
    private int mRadius = 10;
    private static final String TAG = "NearHospitalsActivity";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    // Internal variables
    private TextView mRadiusText;
    private boolean isCalledHospitalApi;
    private SearchService mSearchService;
    private AppCompatSeekBar mRadiusBar;
    private ArrayList<Site> mSites = new ArrayList<>();

    // Map/Location component variables
    private HuaweiMap hMap;
    private MapView map;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation = new Location(Constants.STR_EMPTY);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_hospital);

        // Get mapview instance
        map = findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        MapsInitializer.setApiKey(MAP_API_KEY);
        map.onCreate(mapViewBundle);
        map.getMapAsync(this);
        mRadiusText = findViewById(R.id.tv_radius);
        mRadiusBar = findViewById(R.id.sb_radius);
        mRadiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRadius = progress;
                mRadiusText.setText(mRadius +Constants.STR_KM);
                callNearByHospitalApi(mRadius);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Ask location permissions
        checkLocationPermission();

        // To initialize views
        initViews();
        mSearchService = SearchServiceFactory.create(this, getApiKey(MAP_API_KEY));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        map.onSaveInstanceState(mapViewBundle);
    }

    /**
     * Call nearest hospital for contact and details
     * @param mRadius Radius which can be set by the user to check the nearest hospitals
     */
    private void callNearByHospitalApi(int mRadius) {
        NearbySearchRequest mHospitalRequest = new NearbySearchRequest();
        mHospitalRequest.setLocation(new Coordinate(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        mHospitalRequest.setRadius(mRadius * Constants.INIT_1000);
        mHospitalRequest.setPoiType(LocationType.HOSPITAL);
        mHospitalRequest.setLanguage(String.valueOf(Constants.LANGUAGE_EN));
        SearchResultListener<NearbySearchResponse> mListener = new SearchResultListener<NearbySearchResponse>() {
            @Override
            public void onSearchResult(NearbySearchResponse nearbySearchResponse) {
                mSites = new ArrayList<>();
                mSites = (ArrayList<Site>) nearbySearchResponse.getSites();
                if (mSites != null) {
                    for (int i = INIT_ZERO; i < mSites.size(); i++) {
                        addHospitalMarkerToMap(mSites.get(i));
                    }
                } else {
                    Log.d(TAG, getString(R.string.no_near_hospital));
                }
            }

            @Override
            public void onSearchError(SearchStatus searchStatus) {
            }
        };
        mSearchService.nearbySearch(mHospitalRequest, mListener);
    }

    /**
     * Add markers to the map
     * @param site Add markers to the coordinates explicitly specified
     */
    private void addHospitalMarkerToMap(Site site) {
        Coordinate hospitalCoordinate = site.getLocation();
        LatLng hospitalLatLng = new LatLng(hospitalCoordinate.getLat(), hospitalCoordinate.getLng());
        if (hMap != null) {
            hMap.addMarker(new MarkerOptions().position(hospitalLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_marker)).snippet(site.getName()).clusterable(true)).showInfoWindow();
        }
    }

    private void initViews() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.INTERVAL_10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    List<Location> locations = locationResult.getLocations();
                    if (!locations.isEmpty()) {
                        for (Location location : locations) {
                            mCurrentLocation.setLatitude(location.getLatitude());
                            mCurrentLocation.setLongitude(location.getLongitude());
                            if (!isCalledHospitalApi && location.getAccuracy() < Constants.MAX_ACCURACY) {
                                isCalledHospitalApi = true;
                                callNearByHospitalApi(mRadius);
                            }
                            Log.d(TAG, getString(R.string.on_location_result) + location.getLongitude() + Constants.STR_COMMA + location.getLatitude() +Constants.STR_COMMA + location.getAccuracy());
                        }
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (locationAvailability != null) {
                    boolean flag = locationAvailability.isLocationAvailable();
                    Log.d(TAG, getString(R.string.on_location_availability_is_location_available) + flag);
                }
            }
        };
    }

    // To check location permission
    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.d(TAG, Constants.SDK_VERSION);
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
    protected void onResume() {
        super.onResume();
        requestLocationUpdatesCallback();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLocationUpdatesCallback();
        map.onPause();
    }

    private void requestLocationUpdatesCallback() {
        try {
            LocationSettingsRequest.Builder builders = new LocationSettingsRequest.Builder();
            builders.addLocationRequest(locationRequest);
            LocationSettingsRequest locationSettingRequest = builders.build();
            mSettingsClient.checkLocationSettings(locationSettingRequest)
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsRes) {
                            Log.d(TAG, getString(R.string.check_location_setting_success));
                            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, getString(R.string.request_loaction_update_with_success_callback));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e);

                    int statusCode = ((ApiException) e).getStatusCode();
                    if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException rae = (ResolvableApiException) e;
                    }
                }
            });
        } catch (Exception e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_exception_callback), e);
        }
    }

    private void removeLocationUpdatesCallback() {
        try {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, getString(R.string.request_loaction_update_with_success_callback));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_with_failure_callback), e);
                        }
                    });
        } catch (Exception e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.request_loaction_update_exception_callback), e);

        }
    }

    private String getApiKey(String apiKey) {
        String encodeKey = Constants.STR_EMPTY;
        try {
            encodeKey = URLEncoder.encode(apiKey, Constants.LANGUAGE_EN );
        } catch (UnsupportedEncodingException e) {
            ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_unsupported_encoding), e);
        }
        return encodeKey;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_1) {
            if (grantResults.length > Constants.INIT_1 && grantResults[INIT_ZERO] == PackageManager.PERMISSION_GRANTED
                    && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, getString(R.string.on_request_permission_result_location_successful));
            } else {
                Log.d(TAG, getString(R.string.on_request_permission_result_location_failed));
            }
        }
        if (requestCode == Constants.INIT_2) {
            if (grantResults.length > Constants.INIT_2 && grantResults[Constants.INIT_2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[INIT_ZERO] == PackageManager.PERMISSION_GRANTED
                    && grantResults[Constants.INIT_1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, getString(R.string.on_request_permission_result_background_location_successful));
            } else {
                Log.d(TAG, getString(R.string.on_request_permission_result_background_location_failed));
            }
        }
    }

    @Override
    public void onMapReady(HuaweiMap huaweiMap) {
        hMap = huaweiMap;
        hMap.setMyLocationEnabled(true);
        hMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        map.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        map.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }
}