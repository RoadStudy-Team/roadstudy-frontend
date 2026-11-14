package com.roadstudy.automotive.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrivalRuleTest {

    private val parked = VehicleSignals(gear = Gear.PARK)
    private val driving = VehicleSignals(gear = Gear.DRIVE)
    private val farRoute = NaviSignals(remainingMeters = 4_200, remainingSec = 600)

    @Test
    fun `주행 중이면 도착이 아니다`() {
        assertFalse(ArrivalRule.isArrived(driving, NaviSignals(remainingMeters = 50), hasDriven = true))
    }

    @Test
    fun `한 번도 주행하지 않았으면 P단이어도 도착이 아니다`() {
        assertFalse(ArrivalRule.isArrived(parked, NaviSignals(), hasDriven = false))
        assertFalse(ArrivalRule.isArrived(parked, NaviSignals(arrived = true), hasDriven = false))
    }

    @Test
    fun `내비가 도착을 알렸으면 거리와 관계없이 P단에서 도착이다`() {
        assertTrue(ArrivalRule.isArrived(parked, farRoute.copy(arrived = true), hasDriven = true))
    }

    @Test
    fun `목적지 근처의 P단은 도착이다`() {
        val near = NaviSignals(remainingMeters = ArrivalRule.NEAR_DESTINATION_METERS)

        assertTrue(ArrivalRule.isArrived(parked, near, hasDriven = true))
    }

    @Test
    fun `목적지에서 먼 곳의 P단은 도착이 아니다`() {
        val justOver = NaviSignals(remainingMeters = ArrivalRule.NEAR_DESTINATION_METERS + 1)

        assertFalse(ArrivalRule.isArrived(parked, justOver, hasDriven = true))
        assertFalse(ArrivalRule.isArrived(parked, farRoute, hasDriven = true))
    }

    @Test
    fun `내비 경로가 없으면 주행한 뒤 P단이 도착이다`() {
        assertTrue(ArrivalRule.isArrived(parked, NaviSignals(), hasDriven = true))
    }
}
