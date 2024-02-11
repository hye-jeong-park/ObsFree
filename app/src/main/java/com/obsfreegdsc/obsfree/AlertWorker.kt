package com.obsfreegdsc.obsfree

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.concurrent.TimeUnit

class AlertWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(appContext)

    override fun doWork(): Result {
        if (ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }

        locationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            location?.let {
                Log.d(
                    "TAG",
                    "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",
                )
            }
        }

        val workRequest =
            OneTimeWorkRequestBuilder<AlertWorker>()
                .addTag("alert")
                .setInitialDelay(10, TimeUnit.SECONDS)
                .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueue(workRequest)

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}