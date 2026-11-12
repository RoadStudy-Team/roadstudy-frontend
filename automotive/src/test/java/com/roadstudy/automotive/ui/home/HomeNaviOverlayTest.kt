package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.NaviSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalTime

class HomeNaviOverlayTest {

    private val base = FakeHomeDataSource().load()

    @Test
    fun `내비 경로가 없으면 원래 입력 그대로다`() {
        assertSame(base, base.withNavi(NaviSignals()))
    }

    @Test
    fun `거리나 시간을 모르면 원래 입력 그대로다`() {
        assertSame(base, base.withNavi(NaviSignals(remainingMeters = 4_200)))
        assertSame(base, base.withNavi(NaviSignals(remainingSec = 600)))
    }

    @Test
    fun `내비 경로가 있으면 목적지와 남은 거리와 시간이 바뀐다`() {
        val input = base.withNavi(
            NaviSignals(remainingMeters = 23_000, remainingSec = 3_600, destinationName = "더현대서울")
        )

        assertEquals("더현대서울", input.destinationName)
        assertEquals(23_000, input.route?.remainingMeters)
        assertEquals(3_600, input.route?.etaSec)
    }

    @Test
    fun `내비가 목적지 이름을 안 주면 원래 이름을 쓴다`() {
        val input = base.withNavi(NaviSignals(remainingMeters = 23_000, remainingSec = 3_600))

        assertEquals(base.destinationName, input.destinationName)
    }

    @Test
    fun `정체 구간 수는 내비가 알려주지 않으므로 비운다`() {
        val input = base.withNavi(NaviSignals(remainingMeters = 23_000, remainingSec = 3_600))

        assertNull(input.route?.congestionCount)
    }

    @Test
    fun `출근 판별과 학습 자료는 바꾸지 않는다`() {
        val input = base.withNavi(NaviSignals(remainingMeters = 23_000, remainingSec = 3_600))

        assertEquals(base.tripType, input.tripType)
        assertEquals(base.sections, input.sections)
    }

    @Test
    fun `내비 경로의 시간이 길수록 오늘의 학습 분량도 달라진다`() {
        val mapper = HomeUiMapper()
        val now = LocalTime.of(8, 0)
        val short = mapper.map(base.withNavi(NaviSignals(remainingMeters = 2_000, remainingSec = 5 * 60)), now)
        val long = mapper.map(base.withNavi(NaviSignals(remainingMeters = 60_000, remainingSec = 90 * 60)), now)

        assertEquals(true, long.learningConceptCount > short.learningConceptCount)
        assertEquals("08:05", short.arrivalTime)
    }
}
