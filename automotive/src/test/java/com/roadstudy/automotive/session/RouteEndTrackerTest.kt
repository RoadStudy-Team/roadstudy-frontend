package com.roadstudy.automotive.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteEndTrackerTest {

    private val guiding = NaviSignals(remainingMeters = 4_200, remainingSec = 600)
    private val noRoute = NaviSignals()

    @Test
    fun `처음부터 경로가 없으면 끝난 것이 아니다`() {
        val tracker = RouteEndTracker()

        assertFalse(tracker.update(noRoute))
        assertFalse(tracker.update(noRoute))
    }

    @Test
    fun `안내 중이던 경로가 없어지면 끝난 것이다`() {
        val tracker = RouteEndTracker()

        assertFalse(tracker.update(guiding))
        assertTrue(tracker.update(noRoute))
    }

    @Test
    fun `내비가 도착을 알리면 끝난 것이다`() {
        assertTrue(RouteEndTracker().update(guiding.copy(arrived = true)))
    }

    @Test
    fun `다시 안내가 시작되면 끝나지 않은 상태로 돌아간다`() {
        val tracker = RouteEndTracker()
        tracker.update(guiding)
        tracker.update(noRoute)

        assertFalse(tracker.update(guiding))
    }
}
