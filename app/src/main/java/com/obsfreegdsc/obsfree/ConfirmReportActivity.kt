package com.obsfreegdsc.obsfree

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.obsfreegdsc.obsfree.databinding.ActivityConfirmreportBinding
import java.io.File

class ConfirmReportActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityConfirmreportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityConfirmreportBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        val name = intent.getStringExtra("FILE_NAME") as String
        val cacheFile = File(applicationContext.cacheDir, name)
        val bitmap = BitmapFactory.decodeFile(cacheFile.absolutePath)
        viewBinding.imgConfirmreportPhoto.setImageBitmap(bitmap)
    }
}