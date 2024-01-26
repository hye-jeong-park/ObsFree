package com.obsfreegdsc.obsfree

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.obsfreegdsc.obsfree.databinding.ActivityConfirmreportBinding
import java.io.File
import java.util.Locale
import java.util.UUID

class ConfirmReportActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityConfirmreportBinding

    private var cacheFile: File? = null
    private var location: Location? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        if (location == null) {
            Toast.makeText(
                this,
                "Cannot find current location.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val db = Firebase.firestore

        val uuid = UUID.randomUUID().toString()

        val brokenBlock = BrokenBlock(
            location!!.latitude,
            location!!.longitude,
            "$uuid.jpg"
        )

        db.collection("broken_blocks").add(brokenBlock)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
                location = task.result
                val latitude = location!!.latitude
                val longitude = location!!.longitude

                Geocoder(this, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1) {
                        runOnUiThread {
                            viewBinding.tvConfirmreportAddress.text = it.first().getAddressLine(0)
                        }
                }
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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