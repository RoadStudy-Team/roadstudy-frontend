package com.roadstudy.automotive.vehicle

import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.constant.control.WiperState
import ai.pleos.playground.vehicle.constant.control.WiperSwitch
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.session.WiperLevel

/**
 * 기어, 조명, 와이퍼는 SDK 리스너가 따로따로 알려 준다.
 * 이 클래스가 마지막으로 받은 값을 모아서 session 로직이 쓰는 하나의 [VehicleSignals] 로 만든다.
 * SDK 연결 없이 단위 테스트할 수 있도록 [VehicleSignalSource] 와 분리했다. (한 스레드에서만 부른다)
 */
class VehicleSignalAggregator {

    private var gear: Gear = Gear.PARK
    private var lowBeamOn = false
    private var frontFogOn = false
    private var rearFogOn = false
    private var wiperState: WiperState? = null
    private var wiperSwitch: WiperSwitch? = null

    /** 지금까지 받은 값으로 만든 차량 신호 */
    val signals: VehicleSignals
        get() = VehicleSignals(
            gear = gear,
            wiperLevel = VehicleSignalMapper.wiperLevelOf(wiperState, wiperSwitch),
            lowBeamOn = lowBeamOn,
            fogLightOn = frontFogOn || rearFogOn
        )

    /** 알 수 없는 기어 값이 오면 이전 값을 유지한다 */
    fun onGear(value: VehicleGear?) {
        VehicleSignalMapper.gearOf(value)?.let { gear = it }
    }

    fun onLowBeam(state: LightState?) {
        lowBeamOn = VehicleSignalMapper.isLampOn(state)
    }

    fun onFrontFog(state: LightState?) {
        frontFogOn = VehicleSignalMapper.isLampOn(state)
    }

    fun onRearFog(state: LightState?) {
        rearFogOn = VehicleSignalMapper.isLampOn(state)
    }

    fun onWiperState(state: WiperState?) {
        wiperState = state
    }

    fun onWiperSwitch(switch: WiperSwitch?) {
        wiperSwitch = switch
    }

    /** 현재 와이퍼 단계. 로그와 확인용 */
    val wiperLevel: WiperLevel get() = signals.wiperLevel
}
