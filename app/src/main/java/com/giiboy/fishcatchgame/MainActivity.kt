package com.giiboy.fishcatchgame

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import android.net.Uri
import android.media.MediaPlayer
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.giiboy.fishcatchgame.databaseAPP.AppDatabase
import com.giiboy.fishcatchgame.databaseAPP.Fish
import android.graphics.drawable.AnimationDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var castSound: MediaPlayer
    private lateinit var prefs: SharedPreferences

    private var score = 0
    private var coin = 0
    private var level = 1
    private var baitLevel = 1
    private var tapPower = 1

    private var isFishing = false
    private var progress = 0
    private val maxProgress = 100

    private var castAnimation: AnimationDrawable? = null

    data class FishChance(
        val name: String,
        val point: Int,
        val image: Int,
        val chance: Int
    )

    private val fishList = listOf(
        FishChance("Ikan Kecil", 10, R.drawable.ikan_kecil, 55),
        FishChance("Ikan Sedang", 20, R.drawable.ikan_sedang, 30),
        FishChance("Ikan Besar", 30, R.drawable.ikan_besar, 14),
        FishChance("IKAN LEGENDA!", 100, R.drawable.ikan_legenda, 1)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imgFish = findViewById<ImageView>(R.id.imgFish)
        val tvFish = findViewById<TextView>(R.id.tvFish)
        val tvLevel = findViewById<TextView>(R.id.tvLevel)
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvCoin = findViewById<TextView>(R.id.tvCoin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnCast = findViewById<Button>(R.id.btnCast)
        val btnBait = findViewById<Button>(R.id.btnBait)
        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        val bgVideo = findViewById<VideoView>(R.id.bgVideo)

        castSound = MediaPlayer.create(this, R.raw.cast_sound)


        // SHARED PREF
        prefs = getSharedPreferences("fish_game", MODE_PRIVATE)

        level = prefs.getInt("LEVEL", 1)
        baitLevel = prefs.getInt("BAIT", 1)
        tapPower = prefs.getInt("TAP", 1)
        coin = prefs.getInt("COIN", 0)

        // BACKGROUND VIDEO
        val bgUri = Uri.parse("android.resource://$packageName/${R.raw.bg_ocean}")
        bgVideo.setVideoURI(bgUri)
        bgVideo.setOnPreparedListener { mp ->

            mp.isLooping = true
            mp.setVolume(1f, 1f)

            val videoWidth = mp.videoWidth
            val videoHeight = mp.videoHeight

            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels

            val videoRatio = videoWidth.toFloat() / videoHeight
            val screenRatio = screenWidth.toFloat() / screenHeight

            val layoutParams = bgVideo.layoutParams

            if (videoRatio > screenRatio) {
                layoutParams.height = screenHeight
                layoutParams.width = (screenHeight * videoRatio).toInt()
            } else {
                layoutParams.width = screenWidth
                layoutParams.height = (screenWidth / videoRatio).toInt()
            }

            bgVideo.layoutParams = layoutParams
        }

        bgVideo.start()


        // DATABASE
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fish_db"
        ).allowMainThreadQueries().build()

        score = db.fishDao().getTotalPoint() ?: 0

        tvScore.text = "Score: $score"
        tvLevel.text = "Level: $level"
        tvCoin.text = "Coin: $coin"

        // SAVE FUNCTION
        fun saveData() {
            prefs.edit().apply {
                putInt("LEVEL", level)
                putInt("BAIT", baitLevel)
                putInt("TAP", tapPower)
                putInt("COIN", coin)
                apply()
            }
        }

        fun getRandomFish(): FishChance {
            val rand = (1..100).random()
            var cumulative = 0
            for (fish in fishList) {
                cumulative += fish.chance
                if (rand <= cumulative) return fish
            }
            return fishList.first()
        }

        fun catchFish() {
            val fish = getRandomFish()
            isFishing = false

            imgFish.visibility = View.VISIBLE
            imgFish.setImageResource(fish.image)

            score += fish.point
            coin += fish.point / 2

            // LEVEL NAIK DARI SCORE
            level = (score / 100) + 1

            tvScore.text = "Score: $score"
            tvLevel.text = "Level: $level"
            tvCoin.text = "Coin: $coin"
            tvFish.text = "🎉 Dapat ${fish.name}!"

            db.fishDao().insert(Fish(0, fish.name, fish.point))

            saveData()
        }

        // TAP LAYAR
        rootLayout.setOnClickListener {
            if (!isFishing) return@setOnClickListener

            progress += tapPower
            if (progress > maxProgress) progress = maxProgress

            progressBar.progress = progress
            tvFish.text = "Menarik ikan... $progress%"

            rootLayout.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY
            )

            if (progress >= maxProgress) {
                catchFish()
            }
        }

        // BELI UMPAN
        btnBait.setOnClickListener {
            if (coin >= 10) {
                coin -= 10
                baitLevel++
                tapPower++

                tvCoin.text = "Coin: $coin"
                tvFish.text = "Reeler Lv $baitLevel | Tap +$tapPower"

                saveData()
            } else {
                tvFish.text = "Coin tidak cukup!"
            }
        }

        // CAST
        btnCast.setOnClickListener {

            castSound.start()

            progress = 0
            progressBar.progress = 0

            isFishing = true
            tvFish.text = "Tap layar untuk menarik ikan!"

            imgFish.visibility = View.VISIBLE
            imgFish.setImageResource(R.drawable.cast_anim)

            castAnimation = imgFish.drawable as AnimationDrawable
            castAnimation?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        prefs.edit().apply {
            putInt("LEVEL", level)
            putInt("BAIT", baitLevel)
            putInt("TAP", tapPower)
            putInt("COIN", coin)
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        castSound.release()
    }
}
