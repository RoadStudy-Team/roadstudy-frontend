package com.roadstudy.mobile.ui.main

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.roadstudy.mobile.R
import com.roadstudy.mobile.databinding.ActivityMainBinding
import com.roadstudy.mobile.ui.growth.GrowthFragment
import com.roadstudy.mobile.ui.mydata.MydataFragment
import com.roadstudy.mobile.ui.onboarding.OnBoardingRoutineFragment
import com.roadstudy.mobile.ui.setting.SettingFragment
import com.roadstudy.mobile.ui.today.TodayFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.parseColor("#0F1218")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 최초 실행 시: 온보딩 완료 전이면 온보딩, 완료했으면 Today
        if (savedInstanceState == null) {
            if (isOnboardingDone()) {
                changeFragment(TodayFragment())
            } else {
                setBottomNavigationVisibility(false)
                changeFragment(OnBoardingRoutineFragment())
            }
        }

        // 2. BottomNavigationView 아이템 선택 리스너 (기존 그대로)
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

    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment)
            .commit()
    }

    fun setBottomNavigationVisibility(isVisible: Boolean) {
        binding.mainBnv.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    // 온보딩 완료 후 호출: Today 화면으로 이동 + 바텀 네비 표시
    fun finishOnboarding() {
        getSharedPreferences("routine_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_done", true)
            .apply()

        setBottomNavigationVisibility(true)
        binding.mainBnv.selectedItemId = R.id.navigation_today // 리스너가 TodayFragment로 교체
    }

    private fun isOnboardingDone(): Boolean {
        return false // 개발 중 임시: 항상 온보딩 표시
        // return getSharedPreferences("routine_prefs", MODE_PRIVATE)
        //     .getBoolean("onboarding_done", false)
    }
}