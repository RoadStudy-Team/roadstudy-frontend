package com.roadstudy.mobile.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.roadstudy.mobile.R
import com.roadstudy.mobile.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1.5초 뒤 메인 화면으로 이동
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // 뒤로가기로 스플래시로 돌아오지 않게 종료
        }, 1500L)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}