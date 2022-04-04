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

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.extensions.HdrImageCaptureExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer
import com.huawei.hms.mlsdk.classification.MLRemoteClassificationAnalyzerSetting
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.objects.MLObject
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzer
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting
import com.huawei.mediscalhealth.R
import com.huawei.mediscalhealth.kotlin.utils.Constants
import com.huawei.mediscalhealth.kotlin.utils.ExceptionHandling
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class FoodDetectionActivity : AppCompatActivity() {
    //Internal variables
    var mPreviewView: PreviewView? = null
    var captureImage: LottieAnimationView? = null
    var imageCapture: ImageCapture? = null
    var objectAnalyzer: MLObjectAnalyzer? = null
    var cloudImageClassificationAnalyzer: MLImageClassificationAnalyzer? = null
    private val executor: Executor = Executors.newSingleThreadExecutor()

    //Internal constant values
    private val REQUEST_CODE_PERMISSIONS = 1001
    private val REQUIRED_PERMISSIONS = arrayOf<String?>(Constants.CAMERA_PERMISSION, Constants.WRITE_EXTERNAL_PERMISSION)

    //Layout texts
    private var mDetectedFood: TextView? = null

    //Views
    private var alertDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detection)
        mPreviewView = findViewById(R.id.camera)
        captureImage = findViewById(R.id.btncapture)
        mDetectedFood = findViewById(R.id.tv_detectedFood)
        alertDialog = Dialog(this)
        MLApplication.getInstance().apiKey = R.string.API_KEY.toString()
        if (checkingAllPermissionsGranted()) {
            startingCamera() //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startingCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            } catch (e: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    //To show no internet dialog
    private fun showLoadingDialog() {
        //alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog!!.setContentView(R.layout.loadingdialog)
        if (alertDialog!!.window != null) alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog!!.setCancelable(false)
        alertDialog!!.show()
    }

    private fun dismissLoadingDialog() {
        if (alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
                .build()
        val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        val imageAnalysis = ImageAnalysis.Builder()
                .build()
        val builder = ImageCapture.Builder()
        val hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder)
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector)
        }
        imageCapture = builder
                .setTargetRotation(this.windowManager.defaultDisplay.rotation)
                .build()
        preview.setSurfaceProvider(mPreviewView!!.surfaceProvider)
        val camera = cameraProvider.bindToLifecycle((this as LifecycleOwner), cameraSelector, preview, imageAnalysis, imageCapture)
        captureImage!!.setOnClickListener {
            var file: File? = null
            showLoadingDialog()
            val mDateFormat = SimpleDateFormat(Constants.DATE_FORMAT, Locale.US)
            try {
                file = File(batchDirectoryName, mDateFormat.format(Date()) + Constants.IMAGE_TYPE_JPG)
            } catch (e: IOException) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_io), e)
            }
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file!!).build()
            val finalFile = file
            imageCapture!!.takePicture(outputFileOptions, executor, object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        //   Toast.makeText(CameraActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                        var myBitmap: Bitmap? = null
                        try {
                            myBitmap = BitmapFactory.decodeFile(finalFile.canonicalPath)
                        } catch (e: IOException) {
                            ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_io), e)
                        }
                        val setting = MLObjectAnalyzerSetting.Factory()
                                .setAnalyzerType(MLObjectAnalyzerSetting.TYPE_PICTURE)
                                .allowMultiResults()
                                .allowClassification()
                                .create()
                        objectAnalyzer = MLAnalyzerFactory.getInstance().getLocalObjectAnalyzer(setting)
                        val frame = MLFrame.fromBitmap(myBitmap)
                        val task = objectAnalyzer?.asyncAnalyseFrame(frame)
                        // Asynchronously process the result returned by the object detector.
                        val finalMyBitmap = myBitmap
                        task?.addOnSuccessListener {
                            val objectSparseArray = objectAnalyzer?.analyseFrame(frame)
                            for (i in Constants.INIT_ZERO until objectSparseArray?.size()!!) {
                                if (objectSparseArray?.valueAt(i)?.typeIdentity == MLObject.TYPE_FOOD) {
                                    // IMAGE Classification ...
                                    val cloudSetting = MLRemoteClassificationAnalyzerSetting.Factory()
                                            .setMinAcceptablePossibility(Constants.FLOAT_8F)
                                            .create()
                                    cloudImageClassificationAnalyzer = MLAnalyzerFactory.getInstance().getRemoteImageClassificationAnalyzer(cloudSetting)
                                    val frame = MLFrame.fromBitmap(finalMyBitmap)
                                    val task = cloudImageClassificationAnalyzer?.asyncAnalyseFrame(frame)
                                    task?.addOnSuccessListener { classifications ->
                                        dismissLoadingDialog()
                                        val result = ArrayList<String>()
                                        for (classification in classifications) {
                                            result.add(classification.name)
                                        }
                                        val detectedFood = StringBuilder()
                                        for (details in result) {
                                            detectedFood.append(details).append(Constants.STR_COMMA)
                                        }
                                        mDetectedFood!!.text = detectedFood.toString()
                                    }?.addOnFailureListener { e ->
                                        try {
                                            val mlException = e as MLException
                                            val errorCode = mlException.errCode
                                            val errorMessage = mlException.message
                                            Toast.makeText(this@FoodDetectionActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                        } catch (error: Exception) {
                                            // Handle the conversion error.
                                            Toast.makeText(this@FoodDetectionActivity, error.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    dismissLoadingDialog()
                                    Toast.makeText(this@FoodDetectionActivity, getString(R.string.not_food), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }?.addOnFailureListener {
                            dismissLoadingDialog()
                            // Detection failure.
                            Toast.makeText(this@FoodDetectionActivity, R.string.detection_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_image_capture), exception)
                    dismissLoadingDialog()
                    Toast.makeText(this@FoodDetectionActivity, exception.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    @get:Throws(IOException::class)
    val batchDirectoryName: String
        get() {
            val app_folder_path = ""
            val dir = File(this.externalCacheDir, R.string.app_name.toString() + Constants.IMAGE_TYPE_JPG)
            if (!dir.exists() && !dir.mkdirs()) {
            }
            return dir.canonicalPath
        }

    private fun checkingAllPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission!!) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkingAllPermissionsGranted()) {
                startingCamera()
            } else {
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (objectAnalyzer != null) {
            try {
                objectAnalyzer!!.stop()
            } catch (e: IOException) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_io), e)
            }
        }
        if (cloudImageClassificationAnalyzer != null) {
            try {
                cloudImageClassificationAnalyzer!!.stop()
            } catch (e: IOException) {
                ExceptionHandling.printExceptionInfo(getString(R.string.exception_str_io), e)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}