package com.obsfreegdsc.obsfree

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.obsfreegdsc.obsfree.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.swMainAlert.setOnCheckedChangeListener{ _, isChecked -> changeAlert(isChecked) }
        viewBinding.btnMainIntentcapture.setOnClickListener{ intentCapture() }
    }

    private fun changeAlert(isChecked: Boolean) {
        if (isChecked) {
            if (!foregroundLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    FOREGROUND_REQUIRED_PERMISSIONS,
                    FOREGROUND_REQUEST_CODE_PERMISSIONS
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !backgroundLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    FOREGROUND_REQUEST_CODE_PERMISSIONS
                )
            } else {
                scheduleAlertWorker()
            }
        } else {
            WorkManager
                .getInstance(applicationContext)
                .cancelAllWorkByTag("alert")
        }
    }

    private fun scheduleAlertWorker() {
        val workRequest =
            OneTimeWorkRequestBuilder<AlertWorker>()
                .addTag("alert")
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueue(workRequest)
    }

    private fun intentCapture() {
        intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
    }

    private fun foregroundLocationPermissionsGranted() = FOREGROUND_REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun backgroundLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        baseContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FOREGROUND_REQUEST_CODE_PERMISSIONS) {
            if (!foregroundLocationPermissionsGranted()) {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                viewBinding.swMainAlert.isChecked = false
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !backgroundLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    BACKGROUND_REQUEST_CODE_PERMISSIONS
                )
            } else {
                scheduleAlertWorker()
            }
        } else if (requestCode == BACKGROUND_REQUEST_CODE_PERMISSIONS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && backgroundLocationPermissionsGranted()) {
                scheduleAlertWorker()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                viewBinding.swMainAlert.isChecked = false
            }
        }
    }

    companion object {
        private const val FOREGROUND_REQUEST_CODE_PERMISSIONS = 30
        private val FOREGROUND_REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).toTypedArray()
        private const val BACKGROUND_REQUEST_CODE_PERMISSIONS = 40
    }
}