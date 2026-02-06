package com.giiboy.fishcatchgame

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        val title = findViewById<TextView>(R.id.titleText)

        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        logo.startAnimation(fadeIn)
        title.startAnimation(fadeIn)

        // 🎵 Mainkan suara opening
        mediaPlayer = MediaPlayer.create(this, R.raw.opening)
        mediaPlayer.start()

        Handler(Looper.getMainLooper()).postDelayed({
            mediaPlayer.release()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2500)
    }
}
