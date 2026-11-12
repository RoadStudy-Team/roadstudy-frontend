package com.roadstudy.mobile.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.ActivityMainBinding
import com.roadstudy.mobile.growth.GrowthFragment
import com.roadstudy.mobile.mydata.MydataFragment
import com.roadstudy.mobile.setting.SettingFragment
import com.roadstudy.mobile.today.TodayFragment

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

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        binding.mainBnv.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}