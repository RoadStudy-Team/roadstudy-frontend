package com.roadstudy.automotive.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.roadstudy.automotive.R
import com.roadstudy.automotive.databinding.ActivityMainBinding
import com.roadstudy.automotive.ui.home.HomeFragment
import com.roadstudy.automotive.vehicle.VehicleSignalHub
import com.roadstudy.automotive.vehicle.VehicleSignalSource

/**
 * 차량 앱의 진입 Activity. 지금은 홈 화면([HomeFragment])만 붙인다.
 * 화면이 보이는 동안 Pleos Vehicle SDK 에서 차량 신호를 받아 [VehicleSignalHub] 로 알린다.
 * 기어(D단 시작, P단 도착)에 따른 화면 전환은 각 화면(홈, 주행 중)이 허브를 구독해서 처리한다.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var vehicleSignals: VehicleSignalSource? = null

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
        // 받은 신호는 허브로 보내고, 화면들이 구독한다. 변화는 로그(태그 RoadStudyVehicle)로도 남는다
        vehicleSignals = VehicleSignalSource(applicationContext, onChanged = VehicleSignalHub::update)
            .also { it.start() }
    }

    override fun onStop() {
        vehicleSignals?.stop()
        vehicleSignals = null
        super.onStop()
    }
}
