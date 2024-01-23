package com.obsfreegdsc.obsfree

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.obsfreegdsc.obsfree.databinding.ActivityConfirmreportBinding
import java.io.File

class ConfirmReportActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityConfirmreportBinding

    private var cacheFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityConfirmreportBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (locationPermissionsGranted()) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val name = intent.getStringExtra("FILE_NAME") as String
        cacheFile = File(applicationContext.cacheDir, name)
        val bitmap = BitmapFactory.decodeFile(cacheFile!!.absolutePath)
        viewBinding.imgConfirmreportPhoto.setImageBitmap(bitmap)

        viewBinding.btnComfirmreportCancel.setOnClickListener{ cancel() }
        viewBinding.btnConfirmreportReport.setOnClickListener{ report() }
    }

    private fun cancel() {
        finish()
    }

    private fun report() {

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val locationClient = LocationServices.getFusedLocationProviderClient(baseContext)
        val priority = Priority.PRIORITY_HIGH_ACCURACY

        locationClient.getCurrentLocation(
            priority,
            CancellationTokenSource().token,
        ).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                val latitude = location.latitude
                val longitude = location.longitude

                Toast.makeText(
                    baseContext,
                    "latitude: $latitude, longitude: $longitude",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    baseContext,
                    "Failed to get current location.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun locationPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (locationPermissionsGranted()) {
                getCurrentLocation()
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
        cacheFile!!.delete()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
    }
}