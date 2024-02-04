package com.obsfreegdsc.obsfree

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class AlertWorker(appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        // Do the work here
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed( {
            Toast.makeText(applicationContext, "Worker Test", Toast.LENGTH_SHORT).show()
        }, 0)

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