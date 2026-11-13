package com.roadstudy.automotive.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.ActivityMainBinding
import com.roadstudy.automotive.playback.SignalSources
import com.roadstudy.automotive.ui.home.HomeFragment

/**
 * 차량 앱의 진입 Activity. 지금은 홈 화면([HomeFragment])만 붙인다.
 * 화면이 보이는 동안 차량·내비 신호를 받는다([SignalSources]). 받은 값은 각 허브로 가고 화면들이 구독한다.
 * 기어(D단 시작, P단 도착)에 따른 화면 전환은 각 화면(홈, 주행 중)이 허브를 구독해서 처리한다.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 화면 회전 등으로 재생성될 때는 Fragment 가 복원되므로 처음 한 번만 붙인다
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }

    override fun onStart() {
        super.onStart()
        // 화면이 보이는 동안 차량·내비 신호를 받는다. 학습 세션 중에는 세션도 따로 붙잡고 있어서 화면이 가려져도 이어진다
        SignalSources.acquire(applicationContext, SIGNAL_USER)
    }

    override fun onStop() {
        SignalSources.release(SIGNAL_USER)
        super.onStop()
    }

    private companion object {
        const val SIGNAL_USER = "activity"
    }
}
