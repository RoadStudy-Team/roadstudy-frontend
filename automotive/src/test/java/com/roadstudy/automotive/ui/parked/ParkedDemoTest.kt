package com.roadstudy.automotive.ui.parked

import com.roadstudy.automotive.session.SelfCheck
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.SampleStudySections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParkedDemoTest {

    private val sectionId = SampleStudySections.dbNormalization.sectionId
    private val start = ParkedDemo(FakeHomeDataSource().load(), sectionId).start()
    private val mapper = ParkedUiMapper()

    @Test
    fun `계획한 개념을 모두 들은 세션이 끝난 상태로 시작한다`() {
        // 샘플 경로 38분: 새 개념 1개(반정규화) 뒤에 다시 듣기 3개
        val heard = start.input.heardConcepts.map { it.title }

        assertEquals(listOf("반정규화", "이상 현상", "제3정규형", "BCNF"), heard)
        assertEquals(4, start.input.conceptTotal)
        assertEquals(TripType.TO_WORK, start.input.tripType)
        assertEquals(38, start.input.tripMinutes)
    }

    @Test
    fun `세션에서 들은 개념은 알고 있는 상태가 된다`() {
        // 처음 5개를 알고 있었고, 이번에 BCNF와 반정규화를 새로 들어 7개
        val state = mapper.map(start.input)

        assertEquals(7, state.knownCount)
        assertEquals(ConceptStatus.LEARNED, start.input.statuses["c07"])
    }

    @Test
    fun `다시 들을 개념 수는 처음 상태 그대로다`() {
        assertEquals(3, mapper.map(start.input).relistenCount)
    }

    @Test
    fun `화면에는 들은 개념의 앞 3개가 나온다`() {
        val state = mapper.map(start.input)

        assertEquals(listOf("반정규화", "이상 현상", "제3정규형"), state.concepts.map { it.title })
        assertEquals(3, state.quizCount)
    }

    @Test
    fun `다시 들을래요를 고르면 다시 듣기 목록 맨 앞에 들어간다`() {
        start.tracker.record("c07", SelfCheck.RELISTEN)

        assertEquals("c07", start.tracker.reviewQueue().first())
        assertEquals(ConceptStatus.NEEDS_REVIEW, start.tracker.statuses()["c07"])
    }

    @Test
    fun `이제 알아요를 고르면 다시 듣기 목록에서 빠진다`() {
        start.tracker.record("c02", SelfCheck.KNOW)

        assertTrue("c02" !in start.tracker.reviewQueue())
        assertEquals(ConceptStatus.LEARNED, start.tracker.statuses()["c02"])
    }

    @Test
    fun `자료가 없으면 들은 개념이 없다`() {
        val empty = ParkedDemo(FakeHomeDataSource(startEmpty = true).load(), null).start()

        assertTrue(empty.input.heardConcepts.isEmpty())
        assertEquals("", empty.input.sectionTitle)
    }
}
