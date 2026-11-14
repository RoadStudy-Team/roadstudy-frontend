package com.roadstudy.automotive.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveDrivingContextTest {

    private fun build(vehicle: VehicleSignals, navi: NaviSignals = NaviSignals(), hasDriven: Boolean = true) =
        LiveDrivingContext.build(vehicle, navi, hasDriven, fallbackMeters = 25_000, fallbackSec = 1_600, nowMillis = 0L)

    @Test
    fun `내비 안내가 없으면 홈에서 본 경로를 쓰고 지난 회전 정보는 버린다`() {
        val context = build(VehicleSignals(gear = Gear.DRIVE), NaviSignals(distanceToNextTurnMeters = 100))

        assertEquals(25_000, context.navi.remainingMeters)
        assertEquals(1_600, context.navi.remainingSec)
        assertNull(context.navi.distanceToNextTurnMeters)
    }

    @Test
    fun `내비 안내 중이면 내비 값을 그대로 쓴다`() {
        val navi = NaviSignals(remainingMeters = 4_000, remainingSec = 600, distanceToNextTurnMeters = 200)

        assertEquals(navi, build(VehicleSignals(gear = Gear.DRIVE), navi).navi)
    }

    @Test
    fun `아직 주행 전의 P단은 주행 중으로 본다`() {
        assertEquals(Gear.DRIVE, build(VehicleSignals(gear = Gear.PARK), hasDriven = false).vehicle.gear)
    }

    @Test
    fun `주행 전이어도 P단에서 내비 안내가 끝났으면 그대로 P단이다`() {
        val context = LiveDrivingContext.build(
            VehicleSignals(gear = Gear.PARK), NaviSignals(), hasDriven = false,
            fallbackMeters = 25_000, fallbackSec = 1_600, nowMillis = 0L, routeEnded = true
        )

        assertEquals(Gear.PARK, context.vehicle.gear)
    }

    @Test
    fun `주행한 뒤 목적지 근처의 P단은 그대로 P단이다`() {
        val nearDestination = NaviSignals(remainingMeters = 100, remainingSec = 30)

        assertEquals(Gear.PARK, build(VehicleSignals(gear = Gear.PARK), nearDestination).vehicle.gear)
    }

    @Test
    fun `개발용 옵션을 켜면 회전·주의 구간·재탐색은 무시하고 남은 거리는 그대로 쓴다`() {
        val navi = NaviSignals(
            remainingMeters = 25_000, remainingSec = 1_500,
            distanceToNextTurnMeters = 80, inCautionZone = true, lastRerouteAtMillis = 10L
        )
        val context = LiveDrivingContext.build(
            VehicleSignals(gear = Gear.DRIVE), navi, hasDriven = true,
            fallbackMeters = null, fallbackSec = null, nowMillis = 20L, ignoreNaviFocus = true
        )

        assertEquals(25_000, context.navi.remainingMeters)
        assertNull(context.navi.distanceToNextTurnMeters)
        assertEquals(false, context.navi.inCautionZone)
        assertNull(context.navi.lastRerouteAtMillis)
        assertEquals(LearningMode.NORMAL, LearningModeDecider().decide(context).mode)
    }
}
