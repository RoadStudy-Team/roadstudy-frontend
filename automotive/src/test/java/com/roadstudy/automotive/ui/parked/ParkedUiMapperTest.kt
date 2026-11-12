package com.roadstudy.automotive.ui.parked

import com.roadstudy.automotive.session.SelfCheck
import com.roadstudy.automotive.session.TestFixtures.dbSection
import com.roadstudy.automotive.session.TripType
import com.roadstudy.shared.model.ConceptStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ParkedUiMapperTest {

    private val mapper = ParkedUiMapper()

    private val ids = dbSection.concepts.map { it.id }.toSet()

    private fun input(
        heard: List<String> = listOf("c01", "c02", "c03", "c04"),
        statuses: Map<String, ConceptStatus> = emptyMap(),
        reviewQueue: List<String> = emptyList(),
        choices: Map<String, SelfCheck> = emptyMap(),
        tripMinutes: Int? = 38
    ) = ParkedInput(
        tripType = TripType.TO_WORK,
        tripMinutes = tripMinutes,
        conceptTotal = heard.size,
        sectionTitle = dbSection.title,
        fileName = "DB_정규화_정리.pdf",
        sectionConceptIds = ids,
        heardConcepts = heard.map { id -> dbSection.concepts.first { it.id == id } },
        statuses = statuses,
        reviewQueue = reviewQueue,
        choices = choices,
        streakDays = 12
    )

    @Test
    fun `오늘 학습한 개념은 들은 순서대로 최대 3개만 보여준다`() {
        val state = mapper.map(input(heard = listOf("c05", "c01", "c03", "c02")))

        assertEquals(listOf("c05", "c01", "c03"), state.concepts.map { it.conceptId })
        assertEquals(3, state.quizCount)
        // 이번 세션에서 들은 개념 수는 4개 전체
        assertEquals(4, state.conceptTotal)
    }

    @Test
    fun `카드에는 개념 이름과 핵심 한 줄(정답)이 담긴다`() {
        val card = mapper.map(input(heard = listOf("c01"))).concepts.single()

        assertEquals("개념 c01", card.title)
        assertEquals("정답 c01", card.summary)
    }

    @Test
    fun `들은 개념이 적으면 있는 만큼만 보여준다`() {
        val state = mapper.map(input(heard = listOf("c01", "c02")))

        assertEquals(2, state.concepts.size)
        assertEquals(2, state.quizCount)
    }

    @Test
    fun `들은 개념이 없으면 개념 목록이 비어 있다`() {
        val state = mapper.map(input(heard = emptyList()))

        assertFalse(state.hasConcepts)
        assertEquals(0, state.quizCount)
    }

    @Test
    fun `알고 있는 개념 수는 섹션 전체에서 센다`() {
        val learned = listOf("c01", "c02", "c03").associateWith { ConceptStatus.LEARNED } +
            ("c04" to ConceptStatus.NEEDS_REVIEW)

        assertEquals(3, mapper.map(input(statuses = learned)).knownCount)
    }

    @Test
    fun `다시 들을 개념 수는 이 섹션의 개념만 센다`() {
        // x99 는 어느 섹션에도 없다
        val state = mapper.map(input(reviewQueue = listOf("c02", "c05", "x99")))

        assertEquals(2, state.relistenCount)
    }

    @Test
    fun `고른 선택이 카드에 전달되고 고르지 않으면 비어 있다`() {
        val state = mapper.map(
            input(
                heard = listOf("c01", "c02", "c03"),
                choices = mapOf("c01" to SelfCheck.RELISTEN, "c02" to SelfCheck.KNOW)
            )
        )

        assertEquals(SelfCheck.RELISTEN, state.concepts[0].choice)
        assertEquals(SelfCheck.KNOW, state.concepts[1].choice)
        assertNull(state.concepts[2].choice)
    }

    @Test
    fun `이동 정보와 연속 학습일이 전달된다`() {
        val state = mapper.map(input())

        assertEquals(TripType.TO_WORK, state.tripType)
        assertEquals(38, state.tripMinutes)
        assertEquals(12, state.streakDays)
        assertEquals("DB 정규화 정리", state.sectionTitle)
        assertEquals("DB_정규화_정리.pdf", state.fileName)
    }

    @Test
    fun `이동 시간은 분 단위로 올림한다`() {
        assertEquals(39, ParkedUiMapper.tripMinutes(38 * 60 + 1))
        assertNull(ParkedUiMapper.tripMinutes(null))
    }
}
