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

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.airbnb.lottie.LottieAnimationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.classification.MLImageClassification;
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer;
import com.huawei.hms.mlsdk.classification.MLRemoteClassificationAnalyzerSetting;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.objects.MLObject;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzer;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting;
import com.huawei.mediscalhealth.R;
import com.huawei.mediscalhealth.java.utils.Constants;
import com.huawei.mediscalhealth.java.utils.ExceptionHandling;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.huawei.mediscalhealth.java.utils.Constants.CAMERA_PERMISSION;
import static com.huawei.mediscalhealth.java.utils.Constants.WRITE_EXTERNAL_PERMISSION;

public class FoodDetectionActivity extends AppCompatActivity {
    // Internal variables
    PreviewView mPreviewView;
    LottieAnimationView captureImage;
    ImageCapture imageCapture;
    MLObjectAnalyzer objectAnalyzer;
    MLImageClassificationAnalyzer cloudImageClassificationAnalyzer;
    private Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "MainActivity";

    // Internal constant values
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{CAMERA_PERMISSION, WRITE_EXTERNAL_PERMISSION};

    // Layout texts
    private TextView mDetectedFood;

    // Views
    private Dialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detection);
        mPreviewView = findViewById(R.id.camera);
        captureImage = findViewById(R.id.btncapture);
        mDetectedFood = findViewById(R.id.tv_detectedFood);
        alertDialog = new Dialog(this);
        MLApplication.getInstance().setApiKey(String.valueOf(R.string.API_KEY));
        if (checkingAllPermissionsGranted()) {
            startingCamera(); // start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
    private void startingCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviders = ProcessCameraProvider.getInstance(this);
        cameraProviders.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProviderFuture = cameraProviders.get();
                    bindPreviews(cameraProviderFuture);
                } catch (ExecutionException | InterruptedException exception) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // To show no internet dialog
    private void showLoadingDialog() {
        alertDialog.setContentView(R.layout.loadingdialog);
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    private void dismissLoadingDialog() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
    private void bindPreviews(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        ImageCapture.Builder builder = new ImageCapture.Builder();
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }
        imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = null;
                showLoadingDialog();
                SimpleDateFormat mDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.US);
                try {
                    file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + Constants.IMAGE_TYPE_JPG);
                } catch (IOException e) {
                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_io), e);
                }
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                File finalFile = file;
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap myBitmap = null;
                                try {
                                    myBitmap = BitmapFactory.decodeFile(finalFile.getCanonicalPath());
                                } catch (IOException e) {
                                    ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_io), e);
                                }
                                MLObjectAnalyzerSetting setting = new MLObjectAnalyzerSetting.Factory()
                                        .setAnalyzerType(MLObjectAnalyzerSetting.TYPE_PICTURE)
                                        .allowMultiResults()
                                        .allowClassification()
                                        .create();
                                objectAnalyzer = MLAnalyzerFactory.getInstance().getLocalObjectAnalyzer(setting);
                                final MLFrame frame = MLFrame.fromBitmap(myBitmap);
                                Task<List<MLObject>> task = objectAnalyzer.asyncAnalyseFrame(frame);
                                // Asynchronously process the result returned by the object detector.
                                Bitmap finalMyBitmap = myBitmap;
                                task.addOnSuccessListener(new OnSuccessListener<List<MLObject>>() {
                                    @Override
                                    public void onSuccess(List<MLObject> objects) {
                                        SparseArray<MLObject> objectSparseArray = objectAnalyzer.analyseFrame(frame);
                                        for (int i = Constants.INIT_ZERO; i < objectSparseArray.size(); i++) {
                                            if (objectSparseArray.valueAt(i).getTypeIdentity() == MLObject.TYPE_FOOD) {
                                                // IMAGE Classification ...
                                                MLRemoteClassificationAnalyzerSetting cloudSetting =
                                                        new MLRemoteClassificationAnalyzerSetting.Factory()
                                                                .setMinAcceptablePossibility(Constants.FLOAT_8F)
                                                                .create();
                                                cloudImageClassificationAnalyzer = MLAnalyzerFactory.getInstance().getRemoteImageClassificationAnalyzer(cloudSetting);
                                                MLFrame frame = MLFrame.fromBitmap(finalMyBitmap);
                                                Task<List<MLImageClassification>> task = cloudImageClassificationAnalyzer.asyncAnalyseFrame(frame);
                                                task.addOnSuccessListener(new OnSuccessListener<List<MLImageClassification>>() {
                                                    @Override
                                                    public void onSuccess(List<MLImageClassification> classifications) {
                                                        dismissLoadingDialog();
                                                        ArrayList<String> result = new ArrayList<>();
                                                        for (MLImageClassification classification : classifications) {
                                                            result.add(classification.getName());
                                                        }
                                                        StringBuilder detectedFood = new StringBuilder();
                                                        for (String details : result) {
                                                            detectedFood.append(details).append(Constants.STR_COMMA);
                                                        }
                                                        mDetectedFood.setText(detectedFood.toString());
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        try {
                                                            MLException mlException = (MLException) e;
                                                            int errorCode = mlException.getErrCode();
                                                            String errorMessage = mlException.getMessage();
                                                            Toast.makeText(FoodDetectionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                                        } catch (Exception error) {
                                                            // Handle the conversion error.
                                                            Toast.makeText(FoodDetectionActivity.this,error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                dismissLoadingDialog();
                                                Toast.makeText(FoodDetectionActivity.this, getString(R.string.not_food), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        dismissLoadingDialog();
                                        // Detection failure.
                                        Toast.makeText(FoodDetectionActivity.this, R.string.detection_failed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_image_capture), exception);
                        dismissLoadingDialog();
                        Toast.makeText(FoodDetectionActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public String getBatchDirectoryName() throws IOException {
        String appFolderPath = "";
        File dir = new File(this.getExternalCacheDir(), R.string.app_name + Constants.IMAGE_TYPE_JPG);
        if (!dir.exists() && !dir.mkdirs()) {
        }
        return dir.getCanonicalPath();
    }
    private boolean checkingAllPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkingAllPermissionsGranted()) {
                startingCamera();
            } else {
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (objectAnalyzer != null) {
            try {
                objectAnalyzer.stop();
            } catch (IOException e) {
                ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_io), e);
            }
        }
        if (cloudImageClassificationAnalyzer != null) {
            try {
                cloudImageClassificationAnalyzer.stop();
            } catch (IOException e) {
                ExceptionHandling.getInstance().printExceptionInfo(getString(R.string.exception_str_io), e);
            }
        }
    }
}