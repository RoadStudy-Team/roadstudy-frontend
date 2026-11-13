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
    fun `MVP 기본값은 목적지에서 멀어도 주행한 뒤 P단이면 도착이다`() {
        assertFalse(ArrivalRule.REQUIRE_NEAR_DESTINATION)
        assertTrue(ArrivalRule.isArrived(parked, farRoute, hasDriven = true))
        assertTrue(ArrivalRule.isArrived(parked, NaviSignals(), hasDriven = true))
    }

    // 아래는 목적지 거리 조건을 켰을 때(실차 단계)의 규칙

    @Test
    fun `내비가 도착을 알렸으면 거리와 관계없이 P단에서 도착이다`() {
        assertTrue(isArrivedNear(parked, farRoute.copy(arrived = true)))
    }

    @Test
    fun `목적지 근처의 P단은 도착이다`() {
        assertTrue(isArrivedNear(parked, NaviSignals(remainingMeters = ArrivalRule.NEAR_DESTINATION_METERS)))
    }

    @Test
    fun `목적지에서 먼 곳의 P단은 도착이 아니다`() {
        assertFalse(isArrivedNear(parked, NaviSignals(remainingMeters = ArrivalRule.NEAR_DESTINATION_METERS + 1)))
        assertFalse(isArrivedNear(parked, farRoute))
    }

    @Test
    fun `내비 경로가 없으면 주행한 뒤 P단이 도착이다`() {
        assertTrue(isArrivedNear(parked, NaviSignals()))
    }

    private fun isArrivedNear(vehicle: VehicleSignals, navi: NaviSignals) =
        ArrivalRule.isArrived(vehicle, navi, hasDriven = true, requireNearDestination = true)
}
