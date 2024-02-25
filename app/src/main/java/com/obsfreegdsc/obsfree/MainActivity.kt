package com.obsfreegdsc.obsfree

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.obsfreegdsc.obsfree.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnMainIntentcapture.setOnClickListener{ intentCapture() }
        viewBinding.btnMainIntentmap.setOnClickListener{ intentMaps() }

    }

    private fun intentMaps() {
        intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun intentCapture() {
        intent = Intent(this, CaptureActivity::class.java)
        startActivity(intent)
    }
}