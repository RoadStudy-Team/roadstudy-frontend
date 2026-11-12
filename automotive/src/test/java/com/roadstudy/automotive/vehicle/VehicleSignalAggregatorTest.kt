package com.roadstudy.automotive.vehicle

import ai.pleos.playground.vehicle.constant.control.LightState
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.constant.control.WiperState
import ai.pleos.playground.vehicle.constant.control.WiperSwitch
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.session.WiperLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class VehicleSignalAggregatorTest {

    private val aggregator = VehicleSignalAggregator()

    @Test
    fun `아무 신호도 받지 않았으면 주차 상태의 기본 신호다`() {
        assertEquals(VehicleSignals(), aggregator.signals)
    }

    @Test
    fun `기어가 바뀌면 신호에 반영된다`() {
        aggregator.onGear(VehicleGear.GEAR_DRIVE)

        assertEquals(Gear.DRIVE, aggregator.signals.gear)
    }

    @Test
    fun `알 수 없는 기어 값이 와도 이전 기어를 유지한다`() {
        aggregator.onGear(VehicleGear.GEAR_DRIVE)
        aggregator.onGear(null)
        aggregator.onGear(VehicleGear.GEAR_UNKNOWN)

        assertEquals(Gear.DRIVE, aggregator.signals.gear)
    }

    @Test
    fun `하향등이 켜지면 야간 신호가 켜진다`() {
        aggregator.onLowBeam(LightState.ON)
        assertEquals(true, aggregator.signals.lowBeamOn)

        aggregator.onLowBeam(LightState.OFF)
        assertEquals(false, aggregator.signals.lowBeamOn)
    }

    @Test
    fun `앞 안개등과 뒤 안개등 중 하나만 켜져도 안개등이 켜진 것이다`() {
        aggregator.onFrontFog(LightState.ON)
        assertEquals(true, aggregator.signals.fogLightOn)

        aggregator.onFrontFog(LightState.OFF)
        aggregator.onRearFog(LightState.ON)
        assertEquals(true, aggregator.signals.fogLightOn)

        aggregator.onRearFog(LightState.OFF)
        assertEquals(false, aggregator.signals.fogLightOn)
    }

    @Test
    fun `와이퍼 단계는 상태와 스위치를 함께 보고 정한다`() {
        aggregator.onWiperState(WiperState.ON)
        aggregator.onWiperSwitch(WiperSwitch.CONTINUOUS_LEVEL_3)

        assertEquals(WiperLevel.HIGH, aggregator.signals.wiperLevel)
    }

    @Test
    fun `스위치를 아직 읽지 못했으면 동작 중일 때 약으로 본다`() {
        aggregator.onWiperState(WiperState.ON)

        assertEquals(WiperLevel.LOW, aggregator.wiperLevel)
    }

    @Test
    fun `와이퍼가 꺼지면 스위치가 오래된 값이어도 꺼짐으로 돌아간다`() {
        aggregator.onWiperSwitch(WiperSwitch.OFF)
        aggregator.onWiperState(WiperState.OFF)

        assertEquals(WiperLevel.OFF, aggregator.signals.wiperLevel)
    }

    @Test
    fun `여러 신호가 한꺼번에 반영된다`() {
        aggregator.onGear(VehicleGear.GEAR_DRIVE)
        aggregator.onLowBeam(LightState.ON)
        aggregator.onWiperState(WiperState.ON)
        aggregator.onWiperSwitch(WiperSwitch.INTERMITTENT_LEVEL_2)

        assertEquals(
            VehicleSignals(gear = Gear.DRIVE, wiperLevel = WiperLevel.LOW, lowBeamOn = true, fogLightOn = false),
            aggregator.signals
        )
    }
}
