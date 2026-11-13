package com.roadstudy.automotive.session

import com.roadstudy.automotive.session.TestFixtures.concept
import com.roadstudy.automotive.session.TestFixtures.driving
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionResultTest {

    private val plan = SessionPlan(
        items = listOf(
            PlanItem(concept("a"), PlanItemKind.NEW),
            PlanItem(concept("b"), PlanItemKind.NEW),
            PlanItem(concept("c"), PlanItemKind.NEW)
        ),
        estimatedLearningSec = 400,
        parkOnlyConcepts = emptyList()
    )

    @Test
    fun `듣던 중에 도착한 개념까지 들은 개념으로 넣는다`() {
        val progress = ProgressTracker()
        val controller = SessionController(plan, progress, FakeAudio())
        controller.onContextChanged(driving(now = 0))
        repeat(3) { controller.onAudioFinished(1_000) } // a: 설명·질문 끝, 떠올리기
        controller.tick(10_000) // a 정답
        controller.onAudioFinished(11_000) // b 설명 시작

        val result = SessionResult.of(plan, controller.uiState, progress, "s", TripType.TO_WORK, 60_000L)

        assertEquals(listOf("a", "b"), result.heardConcepts.map { it.id })
    }
}
