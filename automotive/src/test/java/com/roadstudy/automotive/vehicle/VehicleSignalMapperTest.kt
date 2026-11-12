package com.roadstudy.automotive.vehicle

import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.constant.control.WiperState
import ai.pleos.playground.vehicle.constant.control.WiperSwitch
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.WiperLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleSignalMapperTest {

    // ----- 기어 -----

    @Test
    fun `P R N D 기어가 앱의 기어로 바뀐다`() {
        assertEquals(Gear.PARK, VehicleSignalMapper.gearOf(VehicleGear.GEAR_PARK))
        assertEquals(Gear.REVERSE, VehicleSignalMapper.gearOf(VehicleGear.GEAR_REVERSE))
        assertEquals(Gear.NEUTRAL, VehicleSignalMapper.gearOf(VehicleGear.GEAR_NEUTRAL))
        assertEquals(Gear.DRIVE, VehicleSignalMapper.gearOf(VehicleGear.GEAR_DRIVE))
    }

    @Test
    fun `숫자 기어는 모두 주행으로 본다`() {
        val numbered = listOf(
            VehicleGear.GEAR_1, VehicleGear.GEAR_2, VehicleGear.GEAR_3, VehicleGear.GEAR_4, VehicleGear.GEAR_5,
            VehicleGear.GEAR_6, VehicleGear.GEAR_7, VehicleGear.GEAR_8, VehicleGear.GEAR_9
        )

        assertTrue(numbered.all { VehicleSignalMapper.gearOf(it) == Gear.DRIVE })
    }

    @Test
    fun `알 수 없는 기어는 null 이라 이전 값을 유지하게 한다`() {
        assertNull(VehicleSignalMapper.gearOf(null))
        assertNull(VehicleSignalMapper.gearOf(VehicleGear.GEAR_UNKNOWN))
    }

    @Test
    fun `SDK 의 모든 기어 값을 처리한다`() {
        // 새 기어 값이 SDK 에 추가되어도 예외 없이 변환되는지 확인
        VehicleGear.entries.forEach { VehicleSignalMapper.gearOf(it) }
    }

    // ----- 조명 -----

    @Test
    fun `조명은 켜졌을 때만 켜진 것으로 본다`() {
        assertTrue(VehicleSignalMapper.isLampOn(LightState.ON))
        assertFalse(VehicleSignalMapper.isLampOn(LightState.OFF))
        assertFalse(VehicleSignalMapper.isLampOn(null))
    }

    @Test
    fun `주간주행등은 야간 신호가 아니다`() {
        assertFalse(VehicleSignalMapper.isLampOn(LightState.DAYTIME_RUNNING))
    }

    // ----- 와이퍼 -----

    @Test
    fun `스위치가 꺼짐이면 동작 중이어도 꺼짐이다`() {
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.ON, WiperSwitch.OFF))
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.OFF, WiperSwitch.OFF))
    }

    @Test
    fun `간헐 와이퍼는 모든 단계가 약이다`() {
        val intermittent = listOf(
            WiperSwitch.INTERMITTENT_LEVEL_1, WiperSwitch.INTERMITTENT_LEVEL_2, WiperSwitch.INTERMITTENT_LEVEL_3,
            WiperSwitch.INTERMITTENT_LEVEL_4, WiperSwitch.INTERMITTENT_LEVEL_5
        )

        assertTrue(intermittent.all { VehicleSignalMapper.wiperLevelOf(WiperState.ON, it) == WiperLevel.LOW })
    }

    @Test
    fun `연속 1단은 약이고 2단부터는 강이다`() {
        assertEquals(WiperLevel.LOW, VehicleSignalMapper.wiperLevelOf(WiperState.ON, WiperSwitch.CONTINUOUS_LEVEL_1))
        val high = listOf(
            WiperSwitch.CONTINUOUS_LEVEL_2, WiperSwitch.CONTINUOUS_LEVEL_3,
            WiperSwitch.CONTINUOUS_LEVEL_4, WiperSwitch.CONTINUOUS_LEVEL_5
        )

        assertTrue(high.all { VehicleSignalMapper.wiperLevelOf(WiperState.ON, it) == WiperLevel.HIGH })
    }

    @Test
    fun `자동 와이퍼는 동작 중일 때만 약으로 본다`() {
        assertEquals(WiperLevel.LOW, VehicleSignalMapper.wiperLevelOf(WiperState.ON, WiperSwitch.AUTO))
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.OFF, WiperSwitch.AUTO))
    }

    @Test
    fun `미스트와 정비 모드는 비로 보지 않는다`() {
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.ON, WiperSwitch.MIST))
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.SERVICE, WiperSwitch.SERVICE))
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.OTHER, WiperSwitch.OTHER))
    }

    @Test
    fun `스위치를 읽지 못하면 동작 중일 때 약으로 본다`() {
        assertEquals(WiperLevel.LOW, VehicleSignalMapper.wiperLevelOf(WiperState.ON, null))
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(WiperState.OFF, null))
        assertEquals(WiperLevel.OFF, VehicleSignalMapper.wiperLevelOf(null, null))
    }

    @Test
    fun `SDK 의 모든 와이퍼 스위치 값을 처리한다`() {
        WiperSwitch.entries.forEach { VehicleSignalMapper.wiperLevelOf(WiperState.ON, it) }
    }
}
