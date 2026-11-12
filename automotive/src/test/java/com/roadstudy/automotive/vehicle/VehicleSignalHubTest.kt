package com.roadstudy.automotive.vehicle

import com.roadstudy.automotive.session.Gear
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleSignalHubTest {

    @Test
    fun `주차나 중립에서 주행으로 바뀐 순간만 출발로 본다`() {
        assertTrue(VehicleSignalHub.startedDriving(Gear.PARK, Gear.DRIVE))
        assertTrue(VehicleSignalHub.startedDriving(Gear.NEUTRAL, Gear.DRIVE))
        assertTrue(VehicleSignalHub.startedDriving(Gear.REVERSE, Gear.DRIVE))
    }

    @Test
    fun `이미 주행 중이면 출발이 아니다`() {
        assertFalse(VehicleSignalHub.startedDriving(Gear.DRIVE, Gear.DRIVE))
    }

    @Test
    fun `주행이 아닌 기어로 바뀌면 출발이 아니다`() {
        assertFalse(VehicleSignalHub.startedDriving(Gear.DRIVE, Gear.PARK))
        assertFalse(VehicleSignalHub.startedDriving(Gear.PARK, Gear.REVERSE))
    }

    @Test
    fun `이전 값을 모르는 상태에서 주행이면 출발로 본다`() {
        assertTrue(VehicleSignalHub.startedDriving(null, Gear.DRIVE))
    }
}
