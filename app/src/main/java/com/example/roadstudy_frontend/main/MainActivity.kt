package com.example.roadstudy_frontend.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.roadstudy_frontend.R
import com.example.roadstudy_frontend.databinding.ActivityMainBinding
import com.example.roadstudy_frontend.growth.GrowthFragment
import com.example.roadstudy_frontend.mydata.MydataFragment
import com.example.roadstudy_frontend.setting.SettingFragment
import com.example.roadstudy_frontend.today.TodayFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 앱 최초 실행 시 기본으로 보여줄 프래그먼트 설정
        if (savedInstanceState == null) {
            changeFragment(TodayFragment())
        }

        // 2. BottomNavigationView 아이템 선택 리스너 설정
        binding.mainBnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_today -> {
                    changeFragment(TodayFragment())
                    true
                }
                R.id.navigation_mydata -> {
                    changeFragment(MydataFragment())
                    true
                }
                R.id.navigation_growth -> {
                    changeFragment(GrowthFragment())
                    true
                }
                R.id.navigation_setting -> {
                    changeFragment(SettingFragment())
                    true
                }
                else -> false
            }
        }
    }

    // 3. 프래그먼트 교체를 담당하는 헬퍼 함수
    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .commit()
    }
}