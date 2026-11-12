package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.LearningMode
import com.roadstudy.automotive.session.ModeReason
import com.roadstudy.automotive.session.Step
import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.shared.model.SampleStudySections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrivingDemoTest {

    private val demo = DrivingDemo(FakeHomeDataSource().load(), SampleStudySections.dbNormalization.sectionId)

    @Test
    fun `안전 상황은 첫 개념의 설명을 듣는 상태다`() {
        val state = demo.run(DrivingScenario.SAFE)

        assertEquals(LearningMode.NORMAL, state.mode)
        assertEquals(ModeReason.SAFE, state.reason)
        assertEquals(Step.EXPLAIN, state.step)
        assertEquals(1, state.conceptIndex)
        // 샘플 경로 38분에 들을 수 있는 개념은 4개
        assertEquals(4, state.conceptTotal)
    }

    @Test
    fun `떠올리기 상황은 질문을 끝낸 뒤의 단계다`() {
        val state = demo.run(DrivingScenario.THINKING)

        assertEquals(LearningMode.NORMAL, state.mode)
        assertEquals(Step.THINK, state.step)
    }

    @Test
    fun `야간 상황은 주의 모드다`() {
        val state = demo.run(DrivingScenario.NIGHT)

        assertEquals(LearningMode.CAUTION, state.mode)
        assertEquals(ModeReason.NIGHT, state.reason)
    }

    @Test
    fun `회전 앞 상황은 집중 모드이고 진행 중이던 개념이 유지된다`() {
        val state = demo.run(DrivingScenario.TURN_AHEAD)

        assertEquals(LearningMode.FOCUS, state.mode)
        assertEquals(ModeReason.UPCOMING_TURN, state.reason)
        assertEquals(1, state.conceptIndex)
    }

    @Test
    fun `도착 상황은 주차 모드이고 세션이 끝난 상태다`() {
        val state = demo.run(DrivingScenario.ARRIVED)

        assertEquals(LearningMode.PARKED, state.mode)
        assertEquals(ModeReason.PARKED, state.reason)
        assertTrue(state.finished)
    }

    @Test
    fun `같은 상황은 몇 번을 돌려도 같은 결과다`() {
        assertEquals(demo.run(DrivingScenario.NIGHT), demo.run(DrivingScenario.NIGHT))
    }

    @Test
    fun `상황은 순서대로 돌고 마지막 다음은 처음이다`() {
        assertEquals(DrivingScenario.THINKING, DrivingScenario.SAFE.next())
        assertEquals(DrivingScenario.ARRIVED, DrivingScenario.TURN_AHEAD.next())
        assertEquals(DrivingScenario.SAFE, DrivingScenario.ARRIVED.next())
    }

    @Test
    fun `실행 인자의 이름으로 상황을 고르고 없으면 안전 상황이다`() {
        assertEquals(DrivingScenario.NIGHT, DrivingScenario.fromKey("night"))
        assertEquals(DrivingScenario.TURN_AHEAD, DrivingScenario.fromKey("TURN_AHEAD"))
        assertEquals(DrivingScenario.SAFE, DrivingScenario.fromKey(null))
        assertEquals(DrivingScenario.SAFE, DrivingScenario.fromKey("엉뚱한값"))
    }

    @Test
    fun `섹션이 없으면 개념이 없고 제목도 비어 있다`() {
        val emptyDemo = DrivingDemo(FakeHomeDataSource(startEmpty = true).load(), null)

        assertEquals("", emptyDemo.sectionTitle)
        assertFalse(emptyDemo.run(DrivingScenario.SAFE).conceptTotal > 0)
    }
}
