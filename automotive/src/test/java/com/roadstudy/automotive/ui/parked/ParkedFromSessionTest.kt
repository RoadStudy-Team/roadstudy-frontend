package com.roadstudy.automotive.ui.parked

import com.roadstudy.automotive.session.ProgressTracker
import com.roadstudy.automotive.session.SessionResult
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.SampleStudySections
import org.junit.Assert.assertEquals
import org.junit.Test

class ParkedFromSessionTest {

    private val section = SampleStudySections.processThread
    private val heard = section.concepts.filter { it.id in setOf("p08", "p09") }

    private fun result(elapsedMillis: Long = 12 * 60_000L) = SessionResult(
        sectionId = section.sectionId,
        tripType = TripType.TO_HOME,
        elapsedMillis = elapsedMillis,
        heardConcepts = heard,
        statuses = mapOf("p08" to ConceptStatus.LEARNED),
        reviewQueue = listOf("p09")
    )

    @Test
    fun `실제 세션 결과의 들은 개념과 이동 유형을 보여 준다`() {
        val start = ParkedDemo.fromSession(FakeHomeDataSource().load(), result())

        assertEquals(listOf("스케줄링", "컨텍스트 스위칭"), start.input.heardConcepts.map { it.title })
        assertEquals(2, start.input.conceptTotal)
        assertEquals(TripType.TO_HOME, start.input.tripType)
        assertEquals("프로세스와 스레드 소개", start.input.sectionTitle)
    }

    @Test
    fun `걸린 시간은 분 단위로 올림한다`() {
        val start = ParkedDemo.fromSession(FakeHomeDataSource().load(), result(elapsedMillis = 61_000L))

        assertEquals(2, start.input.tripMinutes)
    }

    @Test
    fun `진도는 세션이 끝날 때의 값을 쓴다`() {
        val start = ParkedDemo.fromSession(FakeHomeDataSource().load(), result())

        assertEquals(ConceptStatus.LEARNED, start.input.statuses["p08"])
        assertEquals(listOf("p09"), start.tracker.reviewQueue())
    }
}
