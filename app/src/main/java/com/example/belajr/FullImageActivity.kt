package com.example.belajr

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val ivFullImage = findViewById<ImageView>(R.id.ivFullImage)
        val btnClose = findViewById<ImageView>(R.id.btnClose)

        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .into(ivFullImage)
        }

        btnClose.setOnClickListener {
            finish()
        }
    }
}
