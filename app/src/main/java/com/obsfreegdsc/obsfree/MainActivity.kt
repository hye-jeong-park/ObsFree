package com.obsfreegdsc.obsfree

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.obsfreegdsc.obsfree.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

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
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ALERT_SET] = isChecked
            }
        }

        if (isChecked) {
            val workRequest =
                OneTimeWorkRequestBuilder<AlertWorker>()
                    .addTag("alert")
                    .build()

            WorkManager
                .getInstance(applicationContext)
                .enqueue(workRequest)
        } else {
            WorkManager
                .getInstance(applicationContext)
                .cancelAllWorkByTag("alert")
        }
    }

    private fun intentCapture() {
        intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
    }
}

private object PreferencesKeys {
    val ALERT_SET = booleanPreferencesKey("alert_set")
}