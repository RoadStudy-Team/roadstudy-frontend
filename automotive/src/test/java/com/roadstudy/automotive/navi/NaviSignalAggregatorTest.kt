package com.roadstudy.automotive.navi

import ai.pleos.playground.navi.constants.NavigationStatus
import com.roadstudy.automotive.session.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NaviSignalAggregatorTest {

    private var now = 1_000L
    private val aggregator = NaviSignalAggregator(clock = { now })

    @Test
    fun `처음에는 경로가 없는 기본값이다`() {
        assertFalse(aggregator.signals.hasRoute)
        assertNull(aggregator.signals.destinationName)
    }

    @Test
    fun `목적지 정보는 이름과 남은 거리와 시간을 채운다`() {
        aggregator.onDestination("회사", remainMeters = 4_200, remainSec = 600)

        val signals = aggregator.signals
        assertEquals("회사", signals.destinationName)
        assertEquals(4_200, signals.remainingMeters)
        assertEquals(600, signals.remainingSec)
        assertTrue(signals.hasRoute)
    }

    @Test
    fun `목적지 좌표를 함께 담고, 경로가 끝나면 지운다`() {
        aggregator.onDestination("회사", 4_200, 600, GeoPoint(37.5656, 126.9889))
        assertEquals(GeoPoint(37.5656, 126.9889), aggregator.signals.destinationPoint)

        aggregator.onRouteCancelled()
        assertNull(aggregator.signals.destinationPoint)
    }

    @Test
    fun `이름이 빈 목적지는 이름 없음으로 둔다`() {
        aggregator.onDestination("", remainMeters = 100, remainSec = 30)

        assertNull(aggregator.signals.destinationName)
    }

    @Test
    fun `이름과 거리와 시간이 모두 빈 목적지는 경로 없음이다`() {
        // 에뮬레이터 실측: 경로가 없을 때 SDK 가 이런 빈 목적지를 보낸다
        aggregator.onDestination("회사", 4_200, 600)
        aggregator.onDestination("", remainMeters = 0, remainSec = 0)

        assertFalse(aggregator.signals.hasRoute)
        assertNull(aggregator.signals.destinationName)
    }

    @Test
    fun `경로를 모르는 상태에서 온 주행 시간은 무시한다`() {
        aggregator.onDrivingEta(0)

        assertFalse(aggregator.signals.hasRoute)
    }

    @Test
    fun `주행 정보는 남은 시간만 갱신한다`() {
        aggregator.onDestination("회사", 4_200, 600)
        aggregator.onDrivingEta(540)

        assertEquals(540, aggregator.signals.remainingSec)
        assertEquals(4_200, aggregator.signals.remainingMeters)
    }

    @Test
    fun `다음 회전 거리는 갱신되고 없으면 비워진다`() {
        aggregator.onNextTurn(250)
        assertEquals(250, aggregator.signals.distanceToNextTurnMeters)

        aggregator.onNextTurn(null)
        assertNull(aggregator.signals.distanceToNextTurnMeters)
    }

    @Test
    fun `가까운 주의 구간이 있으면 주의 구간 안이다`() {
        aggregator.onCautionPoints(listOf(1_500, NaviSignalAggregator.CAUTION_ZONE_METERS))
        assertTrue(aggregator.signals.inCautionZone)

        aggregator.onCautionPoints(listOf(1_500))
        assertFalse(aggregator.signals.inCautionZone)

        aggregator.onCautionPoints(emptyList())
        assertFalse(aggregator.signals.inCautionZone)
    }

    @Test
    fun `재탐색이 끝난 시각을 기록한다`() {
        now = 5_000L
        aggregator.onRerouted()

        assertEquals(5_000L, aggregator.signals.lastRerouteAtMillis)
    }

    @Test
    fun `도착 이벤트는 도착 표시를 켜고 새 목적지 정보가 오면 꺼진다`() {
        aggregator.onArrived()
        assertTrue(aggregator.signals.arrived)

        aggregator.onDestination("집", 8_000, 900)
        assertFalse(aggregator.signals.arrived)
    }

    @Test
    fun `경로 취소나 안내 종료는 경로 값을 비운다`() {
        aggregator.onDestination("회사", 4_200, 600)
        aggregator.onNextTurn(250)
        aggregator.onRouteCancelled()
        assertFalse(aggregator.signals.hasRoute)
        assertNull(aggregator.signals.distanceToNextTurnMeters)

        aggregator.onDestination("회사", 4_200, 600)
        aggregator.onStatus(NavigationStatus.FREE_DRIVE)
        assertFalse(aggregator.signals.hasRoute)
    }

    @Test
    fun `경로 안내 상태는 경로 값을 지우지 않는다`() {
        aggregator.onDestination("회사", 4_200, 600)
        aggregator.onStatus(NavigationStatus.ROUTE_DRIVE)

        assertEquals(4_200, aggregator.signals.remainingMeters)
    }
}
