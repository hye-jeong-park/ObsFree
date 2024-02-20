package com.obsfreegdsc.obsfree

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class AlertWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val db = Firebase.firestore
    private val textToSpeechManager = TextToSpeechManager.getInstance(applicationContext)

    override fun doWork(): Result {
        if (ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }

        val prevLatitude = inputData.getDouble("PREV_LAT", Double.MIN_VALUE)
        val prevLongitude = inputData.getDouble("PREV_LON", Double.MIN_VALUE)

        val prevLocation = if (prevLatitude == Double.MIN_VALUE || prevLongitude == Double.MIN_VALUE) {
            null
        } else {
            Location("dummy").apply {
                latitude = prevLatitude
                longitude = prevLongitude
            }
        }

        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            location?.let {
                Log.d(
                    "AlertWork",
                    "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",
                )
            }

            db.collection("broken_blocks")
                .get()
                .addOnSuccessListener { result ->
                    val blockLocation = Location("dummy")
                    for (document in result) {
                        document.toObject<BrokenBlock>().let { brokenBlock ->
                                blockLocation.latitude = brokenBlock.latitude
                                blockLocation.longitude = brokenBlock.longitude
                        }

                        if (location.distanceTo(blockLocation) <= 100 &&
                            (prevLocation == null || prevLocation.distanceTo(blockLocation) > 100)) {
                            textToSpeechManager.speak("근처에 망가진 점자 블록이 있습니다.")
                            break
                        }
                    }
                    enqueueNextWork(location.latitude, location.longitude)
                }
                .addOnFailureListener {
                    enqueueNextWork(location.latitude, location.longitude)
                }
        }.addOnFailureListener {
            enqueueNextWork(Double.MIN_VALUE, Double.MIN_VALUE)
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun enqueueNextWork(latitude: Double, longitude: Double) {
        val data = workDataOf(
            "PREV_LAT" to latitude,
            "PREV_LON" to longitude
        )

        val workRequest =
            OneTimeWorkRequestBuilder<AlertWorker>()
                .addTag("alert")
                .setInputData(data)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueue(workRequest)
    }
}