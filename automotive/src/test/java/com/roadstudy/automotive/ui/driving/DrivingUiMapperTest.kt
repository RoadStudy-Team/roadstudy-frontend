package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.LearningMode
import com.roadstudy.automotive.session.ModeReason
import com.roadstudy.automotive.session.SessionUiState
import com.roadstudy.automotive.session.Step
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrivingUiMapperTest {

    private val mapper = DrivingUiMapper()

    private fun session(
        mode: LearningMode = LearningMode.NORMAL,
        reason: ModeReason = ModeReason.SAFE,
        step: Step = Step.EXPLAIN,
        remainingMeters: Int? = 12_100,
        remainingSec: Int? = 24 * 60
    ) = SessionUiState(
        mode = mode,
        reason = reason,
        conceptIndex = 2,
        conceptTotal = 4,
        currentTitle = "BCNF",
        remainingMeters = remainingMeters,
        remainingSec = remainingSec,
        finished = false,
        step = step
    )

    private fun map(session: SessionUiState) = mapper.map(session, "회사", "DB 정규화 정리")

    @Test
    fun `안전 구간에서 설명을 들을 때는 파형과 핸들 안내를 보여준다`() {
        val state = map(session())

        assertEquals(DrivingTone.SAFE, state.tone)
        assertEquals(DrivingVisual.WAVE, state.visual)
        assertEquals(DrivingHint.STEERING, state.hint)
        assertEquals("안전 구간 · 학습 중", state.statusText)
    }

    @Test
    fun `질문 뒤 떠올리는 동안은 흐린 파형과 떠올리기 문구를 보여준다`() {
        val state = map(session(step = Step.THINK))

        assertEquals(DrivingTone.SAFE, state.tone)
        assertEquals(DrivingVisual.WAVE_QUIET, state.visual)
        assertEquals("질문 · 떠올려보세요", state.statusText)
    }

    @Test
    fun `야간에는 노란 점과 아이콘 자리가 있는 파형을 보여준다`() {
        val state = map(session(LearningMode.CAUTION, ModeReason.NIGHT))

        assertEquals(DrivingTone.CAUTION, state.tone)
        assertEquals(DrivingVisual.WAVE_WITH_ICON, state.visual)
        assertEquals(DrivingHint.STEERING, state.hint)
        assertEquals("야간 · 가벼운 복습만 진행 중", state.statusText)
    }

    @Test
    fun `집중 순간에는 일시정지와 자동 재개 안내를 보여준다`() {
        val state = map(session(LearningMode.FOCUS, ModeReason.UPCOMING_TURN))

        assertEquals(DrivingTone.FOCUS, state.tone)
        assertEquals(DrivingVisual.PAUSED, state.visual)
        assertEquals(DrivingHint.AUTO_RESUME, state.hint)
    }

    @Test
    fun `집중 순간에는 떠올리는 단계여도 일시정지가 우선이다`() {
        val state = map(session(LearningMode.FOCUS, ModeReason.CAUTION_ZONE, step = Step.THINK))

        assertEquals(DrivingVisual.PAUSED, state.visual)
        assertEquals("주의 구간 · 잠시 멈춤", state.statusText)
    }

    @Test
    fun `주차하면 안내 없이 도착 문구만 보여준다`() {
        val state = map(session(LearningMode.PARKED, ModeReason.PARKED))

        assertEquals(DrivingHint.NONE, state.hint)
        assertEquals("도착했어요", state.statusText)
    }

    @Test
    fun `주차 상태만 도착으로 표시된다`() {
        assertEquals(true, map(session(LearningMode.PARKED, ModeReason.PARKED)).arrived)
        assertEquals(false, map(session()).arrived)
        assertEquals(false, map(session(LearningMode.FOCUS, ModeReason.UPCOMING_TURN)).arrived)
    }

    @Test
    fun `경로 정보와 개념 진행이 화면 값으로 바뀐다`() {
        val state = map(session())

        assertEquals("12.1", state.distanceKm)
        assertEquals(24, state.etaMinutes)
        assertEquals(2, state.conceptIndex)
        assertEquals(4, state.conceptTotal)
        assertEquals("회사", state.destinationName)
        assertEquals("DB 정규화 정리", state.sectionTitle)
    }

    @Test
    fun `경로를 모르면 거리와 시간이 비어 있다`() {
        val state = map(session(remainingMeters = null, remainingSec = null))

        assertNull(state.distanceKm)
        assertNull(state.etaMinutes)
    }
}
