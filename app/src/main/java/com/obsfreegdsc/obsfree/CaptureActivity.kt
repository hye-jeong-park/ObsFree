package com.obsfreegdsc.obsfree

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.rotationMatrix
import com.obsfreegdsc.obsfree.databinding.ActivityCaptureBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias CanePositionListener = (canePosition: ArrayList<Recognition>) -> Unit

class CaptureActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCaptureBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var textToSpeechManager: TextToSpeechManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCaptureBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        textToSpeechManager = TextToSpeechManager.getInstance(applicationContext)

        if (cameraPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS)
        }

        viewBinding.btnCaptureCapture.setOnClickListener{ takePhoto() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.prvvCaptureRearcam.surfaceProvider)
                }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, WhiteCaneAnalyzer(
                            Yolov5TFLiteDetector(
                                this,
                                "last-fp16.tflite"
                            ), 5000
                        ) { recognitions ->
                            if (recognitions.isNotEmpty() &&
                                recognitions[0].confidence > 0.5) {
                                val location = recognitions[0].location

                                val bound = 100
                                var speakText = ""

                                if (location.centerX() < 320 - bound) {
                                    speakText += "왼쪽"
                                } else if (location.centerX() > 320 + bound) {
                                    speakText += "오른쪽"
                                }

                                if (location.top < 320 - bound) {
                                    speakText += "위"
                                } else if (location.top > 320 + bound) {
                                    speakText += "아래"
                                }

                                if (speakText.isNotEmpty()) {
                                    speakText += "를 비춰주세요."
                                } else {
                                    speakText = "카메라 위치가 정상적입니다."
                                }

                                textToSpeechManager.speak(speakText)

                                Log.d(
                                    "YOLO",
                                    "(${location.left}, ${location.top}, ${location.right}, ${location.bottom})"
                                )
                            } else {
                                textToSpeechManager.speak("흰 지팡이가 인식되지 않았습니다.")
                            }
                        }
                    )
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = Bitmap.createBitmap(image.toBitmap(), 0, 0,
                        image.width, image.height, rotationMatrix(image.imageInfo.rotationDegrees.toFloat()), true)

                    val file = File(applicationContext.cacheDir, name)

                    try {
                        val fileOutputStream = FileOutputStream(file)

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

                        fileOutputStream.flush()
                        fileOutputStream.close()

                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)

                        intentConfirmReport(name)
                    } catch (exception: Exception) {
                        val msg = "Photo save failed: ${exception.message}"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, msg, exception)
                    }
                }
            }
        )
    }

    private fun intentConfirmReport(name: String) {
        intent = Intent(this, ConfirmReportActivity::class.java)
        intent.putExtra("FILE_NAME", name)
        startActivity(intent)
    }

    private fun cameraPermissionGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (cameraPermissionGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class WhiteCaneAnalyzer(private val detector: Yolov5TFLiteDetector,
                                    private val interval: Int,
                                    private val listener: CanePositionListener) : ImageAnalysis.Analyzer {
        private var lastTime = System.currentTimeMillis()

        override fun analyze(image: ImageProxy) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastTime > interval) {
                val bitmap = Bitmap.createBitmap(image.toBitmap(), 0, 0,
                    image.width, image.height, rotationMatrix(image.imageInfo.rotationDegrees.toFloat()), true)
                val canePosition = detector.detect(bitmap)

                listener(canePosition)

                lastTime = currentTime
            }

            image.close()
        }
    }

    companion object {
        private const val TAG = "ObsFree"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}