package com.roadstudy.automotive.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.WindowArea
import ai.pleos.playground.vehicle.constant.area.WindowValue
import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.constant.control.WiperState
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import ai.pleos.playground.vehicle.listener.LightStateListener
import ai.pleos.playground.vehicle.listener.WiperStateListener
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.roadstudy.automotive.session.VehicleSignals

/**
 * Pleos Vehicle SDK 에서 기어, 조명, 와이퍼 신호를 받아 [VehicleSignals] 로 알려 준다.
 *
 * - [start] 로 SDK 에 연결하고 리스너를 등록하며, [stop] 으로 해제한다 (화면이 보이는 동안만 쓴다).
 * - SDK 콜백은 다른 스레드에서 올 수 있어 메인 스레드로 옮겨서 처리한다.
 * - 와이퍼 단계는 리스너로 오지 않아서(상태는 꺼짐/켜짐뿐) 스위치 값을 주기적으로 읽는다.
 *
 * TODO: 기어는 SDK 에서 숨김(@hide)·사용 중단(Deprecated) API 라서 에뮬레이터 실측으로 값이 오는지 확인한다.
 */
class VehicleSignalSource(
    context: Context,
    private val onChanged: (VehicleSignals) -> Unit,
    private val vehicle: Vehicle = Vehicle(context)
) {
    private val main = Handler(Looper.getMainLooper())
    private val aggregator = VehicleSignalAggregator()
    private var running = false

    private val frontWindshield = WindowArea(WindowValue.FRONT_WINDSHIELD)

    @Suppress("DEPRECATION")
    private val gearListener = object : GearSelectionListener {
        override fun onSelected(gear: VehicleGear?) = update("기어 $gear") { aggregator.onGear(gear) }
        override fun onFailed(e: Exception) = logFailure("기어", e)
    }

    private val lowBeamListener = lightListener("하향등") { aggregator.onLowBeam(it) }
    private val frontFogListener = lightListener("앞 안개등") { aggregator.onFrontFog(it) }
    private val rearFogListener = lightListener("뒤 안개등") { aggregator.onRearFog(it) }

    private val wiperStateListener = object : WiperStateListener {
        override fun onWiperStateUpdated(state: WiperState?) {
            update("와이퍼 상태 $state") { aggregator.onWiperState(state) }
            // 상태가 바뀌면 스위치(단계)도 바뀐 것이므로 다음 폴링을 기다리지 않고 바로 읽는다
            main.post { if (running) readWiperSwitch() }
        }
        override fun onFailed(e: Exception) = logFailure("와이퍼 상태", e)
    }

    private val wiperPoll = object : Runnable {
        override fun run() {
            if (!running) return
            readWiperSwitch()
            main.postDelayed(this, WIPER_POLL_MILLIS)
        }
    }

    fun start() {
        if (running) return
        running = true
        Log.d(TAG, "Vehicle SDK 연결 시작")
        vehicle.initialize()
        register()
        main.post(wiperPoll)
    }

    fun stop() {
        if (!running) return
        running = false
        main.removeCallbacks(wiperPoll)
        unregister()
        vehicle.release()
        Log.d(TAG, "Vehicle SDK 해제")
    }

    @Suppress("DEPRECATION")
    private fun register() = guarded("리스너 등록") {
        vehicle.getGear().registerGearSelection(gearListener)
        vehicle.getLight().registerLowBeamLightState(lowBeamListener)
        vehicle.getLight().registerFrontFogLightState(frontFogListener)
        vehicle.getLight().registerRearFogLightState(rearFogListener)
        vehicle.getWiper().registerWiperState(frontWindshield, wiperStateListener)
    }

    @Suppress("DEPRECATION")
    private fun unregister() = guarded("리스너 해제") {
        vehicle.getGear().unregisterGearSelection(gearListener)
        vehicle.getLight().unregisterLowBeamLightState(lowBeamListener)
        vehicle.getLight().unregisterFrontFogLightState(frontFogListener)
        vehicle.getLight().unregisterRearFogLightState(rearFogListener)
        vehicle.getWiper().unregisterWiperState(frontWindshield, wiperStateListener)
    }

    private fun readWiperSwitch() = guarded("와이퍼 스위치 읽기") {
        vehicle.getWiper().getCurrentWiperSwitch(
            area = frontWindshield,
            onComplete = { switch -> update("와이퍼 스위치 $switch") { aggregator.onWiperSwitch(switch) } },
            onFailed = { logFailure("와이퍼 스위치", it) }
        )
    }

    private fun lightListener(name: String, apply: (LightState?) -> Unit) = object : LightStateListener {
        override fun onLightStateUpdated(state: LightState?) = update("$name $state") { apply(state) }
        override fun onFailed(e: Exception) = logFailure(name, e)
    }

    /** 받은 값을 반영하고, 합쳐진 신호가 바뀌었을 때만 알린다 (메인 스레드에서 처리) */
    private fun update(description: String, apply: () -> Unit) {
        main.post {
            if (!running) return@post
            val before = aggregator.signals
            apply()
            val after = aggregator.signals
            Log.d(TAG, "$description -> $after")
            if (after != before) onChanged(after)
        }
    }

    private fun logFailure(name: String, e: Exception) {
        Log.w(TAG, "$name 신호를 받지 못했습니다: ${e.message}")
    }

    /** 권한이 빠졌거나 서비스가 없을 때 앱이 죽지 않도록 막고 원인을 남긴다 */
    private inline fun guarded(what: String, block: () -> Unit) {
        try {
            block()
        } catch (e: SecurityException) {
            Log.w(TAG, "$what 실패(권한): ${e.message}")
        } catch (e: RuntimeException) {
            Log.w(TAG, "$what 실패: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "RoadStudyVehicle"
        const val WIPER_POLL_MILLIS = 2_000L
    }
}
