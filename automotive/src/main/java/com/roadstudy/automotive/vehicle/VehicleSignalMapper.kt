package com.roadstudy.automotive.vehicle

import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.constant.control.WiperState
import ai.pleos.playground.vehicle.constant.control.WiperSwitch
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.WiperLevel

/**
 * Pleos Vehicle SDK 의 값을 session 로직이 쓰는 입력 형식으로 바꾼다.
 * session 패키지는 Pleos SDK 에 의존하지 않으므로, SDK 타입은 이 변환 안에서만 다룬다.
 *
 * TODO: 와이퍼 단계(약/강) 기준은 에뮬레이터 실측 뒤에 확정한다.
 */
object VehicleSignalMapper {

    /**
     * 기어 선택 값을 바꾼다. 알 수 없는 값(null, UNKNOWN)은 null 을 돌려주고, 호출한 쪽은 이전 값을 유지한다.
     * 숫자 기어(GEAR_1~GEAR_9)는 수동 변속 중이라 모두 주행(DRIVE)으로 본다.
     */
    fun gearOf(gear: VehicleGear?): Gear? = when (gear) {
        null, VehicleGear.GEAR_UNKNOWN -> null
        VehicleGear.GEAR_PARK -> Gear.PARK
        VehicleGear.GEAR_REVERSE -> Gear.REVERSE
        VehicleGear.GEAR_NEUTRAL -> Gear.NEUTRAL
        else -> Gear.DRIVE
    }

    /** 전조등, 안개등이 켜져 있는지. 주간주행등(DAYTIME_RUNNING)은 야간 신호가 아니므로 꺼진 것으로 본다 */
    fun isLampOn(state: LightState?): Boolean = state == LightState.ON

    /**
     * 와이퍼 단계를 정한다.
     * 상태(WiperState)에는 단계가 없고 스위치(WiperSwitch)에만 있어서 둘을 함께 본다.
     *
     * - 약(LOW): 간헐 1~5단, 연속 1단
     * - 강(HIGH): 연속 2~5단
     * - 자동(AUTO): 와이퍼가 실제로 동작 중(ON)이면 약, 아니면 꺼짐 (정확한 속도를 알 수 없다)
     * - 미스트(MIST)는 한 번 닦는 동작이라 비로 보지 않고, 정비(SERVICE)와 알 수 없음(OTHER)도 꺼짐
     * - 스위치 값을 읽지 못했으면 동작 중(ON)일 때 약으로 본다
     */
    fun wiperLevelOf(state: WiperState?, switch: WiperSwitch?): WiperLevel {
        val running = state == WiperState.ON
        return when (switch) {
            null -> if (running) WiperLevel.LOW else WiperLevel.OFF
            WiperSwitch.OFF, WiperSwitch.MIST, WiperSwitch.SERVICE, WiperSwitch.OTHER -> WiperLevel.OFF
            WiperSwitch.INTERMITTENT_LEVEL_1,
            WiperSwitch.INTERMITTENT_LEVEL_2,
            WiperSwitch.INTERMITTENT_LEVEL_3,
            WiperSwitch.INTERMITTENT_LEVEL_4,
            WiperSwitch.INTERMITTENT_LEVEL_5,
            WiperSwitch.CONTINUOUS_LEVEL_1 -> WiperLevel.LOW
            WiperSwitch.CONTINUOUS_LEVEL_2,
            WiperSwitch.CONTINUOUS_LEVEL_3,
            WiperSwitch.CONTINUOUS_LEVEL_4,
            WiperSwitch.CONTINUOUS_LEVEL_5 -> WiperLevel.HIGH
            WiperSwitch.AUTO -> if (running) WiperLevel.LOW else WiperLevel.OFF
        }
    }
}
