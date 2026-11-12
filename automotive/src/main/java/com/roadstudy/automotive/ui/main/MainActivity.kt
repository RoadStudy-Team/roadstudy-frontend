package com.roadstudy.automotive.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.ActivityMainBinding
import com.roadstudy.automotive.ui.home.HomeFragment

/**
 * 차량 앱의 진입 Activity. 지금은 홈 화면([HomeFragment])만 붙인다.
 * TODO: 주행 중·주차 후 화면이 생기면 기어 상태에 따라 여기서 화면을 바꿔 끼운다.
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
}
