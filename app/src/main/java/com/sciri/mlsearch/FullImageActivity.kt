package com.sciri.mlsearch

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import coil.load
import com.sciri.mlsearch.databinding.ActivityFullImageBinding
import java.io.File

class FullImageActivity : AppCompatActivity() {
    companion object {
        const val PHOTO_URI = "photo_uri"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val photoUri = Uri.parse(intent.extras?.getString(PHOTO_URI))
        val path = photoUri.path
        if (path.isNullOrEmpty()) {
            Toast.makeText(this, "Error cargando la URI", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.fullImage.load(File(path))
    }
}