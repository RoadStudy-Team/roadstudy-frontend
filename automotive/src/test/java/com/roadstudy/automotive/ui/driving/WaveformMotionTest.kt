package com.roadstudy.automotive.ui.driving

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaveformMotionTest {

    @Test
    fun `움직임 크기가 0 이면 막대 높이는 변하지 않는다`() {
        for (phase in listOf(0f, 1f, 2.5f, 5f)) {
            assertEquals(1f, WaveformMotion.barFactor(phase, index = 3, depth = 0f), 0.0001f)
        }
    }

    @Test
    fun `높이에 곱하는 값은 1 에서 움직임 크기를 뺀 값 이상 1 이하다`() {
        val depth = 0.3f
        for (index in 0 until 15) {
            for (step in 0..20) {
                val factor = WaveformMotion.barFactor(WaveformMotion.FULL_CYCLE * step / 20f, index, depth)
                assertTrue("factor=$factor", factor >= 1f - depth - 0.0001f && factor <= 1.0001f)
            }
        }
    }

    @Test
    fun `같은 시각에도 막대마다 값이 달라 파도처럼 보인다`() {
        val first = WaveformMotion.barFactor(0f, index = 0, depth = 0.5f)
        val second = WaveformMotion.barFactor(0f, index = 1, depth = 0.5f)

        assertNotEquals(first, second, 0.0001f)
    }

    @Test
    fun `한 바퀴를 돌면 처음과 같은 값으로 돌아온다`() {
        val start = WaveformMotion.barFactor(0f, index = 4, depth = 0.4f)
        val end = WaveformMotion.barFactor(WaveformMotion.FULL_CYCLE, index = 4, depth = 0.4f)

        assertEquals(start, end, 0.0001f)
    }
}
