package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.TestFixtures
import com.roadstudy.automotive.session.TestFixtures.dbSection
import com.roadstudy.automotive.session.TripType
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.MaterialType
import com.roadstudy.shared.model.StudySection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HomeUiMapperTest {

    private val mapper = HomeUiMapper()
    private val now = LocalTime.of(8, 14)

    private val englishSection = StudySection(
        sectionId = "english",
        title = "영어 표현",
        type = MaterialType.ENGLISH,
        concepts = listOf(TestFixtures.concept("e01"), TestFixtures.concept("e02"))
    )

    private fun input(
        sections: List<StudySection> = listOf(dbSection, englishSection),
        statuses: Map<String, ConceptStatus> = emptyMap(),
        reviewQueue: List<String> = emptyList(),
        selectedSectionId: String? = null,
        tripType: TripType = TripType.TO_WORK,
        route: RouteSummary? = RouteSummary(remainingMeters = 18_400, etaSec = 38 * 60, congestionCount = 1)
    ) = HomeInput(
        tripType = tripType,
        destinationName = "회사",
        route = route,
        sections = sections,
        statuses = statuses,
        reviewQueue = reviewQueue,
        selectedSectionId = selectedSectionId
    )

    // ----- 경로 숫자 -----

    @Test
    fun `거리는 km 로 바꿔 소수점 한 자리로 보여준다`() {
        assertEquals("18.4", HomeUiMapper.formatDistanceKm(18_400))
        assertEquals("12.1", HomeUiMapper.formatDistanceKm(12_100))
        assertEquals("0.8", HomeUiMapper.formatDistanceKm(800))
    }

    @Test
    fun `남은 시간은 분 단위로 올림한다`() {
        assertEquals(38, HomeUiMapper.ceilMinutes(38 * 60))
        assertEquals(39, HomeUiMapper.ceilMinutes(38 * 60 + 1))
        assertEquals(0, HomeUiMapper.ceilMinutes(0))
    }

    @Test
    fun `도착 예정 시각은 지금 시각에 남은 시간을 더한다`() {
        assertEquals("08:52", HomeUiMapper.formatArrival(now, 38 * 60))
        // 자정을 넘기면 다음 날 시각으로 이어진다
        assertEquals("00:10", HomeUiMapper.formatArrival(LocalTime.of(23, 40), 30 * 60))
    }

    @Test
    fun `경로 정보가 화면 값으로 바뀐다`() {
        val state = mapper.map(input(), now)

        assertEquals("18.4", state.distanceKm)
        assertEquals(38, state.etaMinutes)
        assertEquals("08:52", state.arrivalTime)
        assertEquals(1, state.congestionCount)
        assertEquals("회사", state.destinationName)
    }

    @Test
    fun `경로가 없으면 경로 값과 학습 분량이 비어 있다`() {
        val state = mapper.map(input(route = null), now)

        assertNull(state.distanceKm)
        assertNull(state.etaMinutes)
        assertNull(state.arrivalTime)
        assertNull(state.congestionCount)
        assertEquals(0, state.learningMinutes)
        assertEquals(0, state.learningConceptCount)
    }

    // ----- 스터디 섹션 카드 -----

    @Test
    fun `진도율은 알고 있는 개념 비율에서 소수점을 버린다`() {
        // dbSection 은 개념 8개. 5개를 알고 있으면 62.5% -> 62%
        val learned = listOf("c01", "c02", "c03", "c04", "c05").associateWith { ConceptStatus.LEARNED }

        val card = mapper.map(input(statuses = learned), now).sections.first { it.sectionId == dbSection.sectionId }

        assertEquals(8, card.conceptCount)
        assertEquals(62, card.progressPercent)
    }

    @Test
    fun `다시 듣기 개수는 그 섹션의 개념만 센다`() {
        // x99 는 어느 섹션에도 없는 개념이라 세지 않는다
        val review = listOf("c02", "c05", "e01", "x99")

        val cards = mapper.map(input(reviewQueue = review), now).sections

        assertEquals(2, cards.first { it.sectionId == dbSection.sectionId }.reviewCount)
        assertEquals(1, cards.first { it.sectionId == "english" }.reviewCount)
    }

    @Test
    fun `다시 들을 개념 제목은 다시 듣기 순서대로 그 섹션 것만 담긴다`() {
        val review = listOf("c05", "e01", "c02", "x99")

        val cards = mapper.map(input(reviewQueue = review), now).sections

        assertEquals(listOf("개념 c05", "개념 c02"), cards.first { it.sectionId == dbSection.sectionId }.reviewTitles)
        assertEquals(listOf("개념 e01"), cards.first { it.sectionId == "english" }.reviewTitles)
    }

    @Test
    fun `다시 들을 개념 제목은 최대 3개까지만 담긴다`() {
        val review = listOf("c01", "c02", "c03", "c04", "c05")

        val card = mapper.map(input(reviewQueue = review), now).sections.first { it.sectionId == dbSection.sectionId }

        assertEquals(3, card.reviewTitles.size)
        assertEquals(5, card.reviewCount)
    }

    @Test
    fun `선택한 섹션이 없으면 첫 번째 섹션이 선택된다`() {
        val cards = mapper.map(input(selectedSectionId = null), now).sections

        assertTrue(cards[0].selected)
        assertFalse(cards[1].selected)
    }

    @Test
    fun `선택한 섹션만 선택 표시가 된다`() {
        val cards = mapper.map(input(selectedSectionId = "english"), now).sections

        assertFalse(cards[0].selected)
        assertTrue(cards[1].selected)
    }

    @Test
    fun `존재하지 않는 섹션을 선택하면 첫 번째 섹션으로 되돌아간다`() {
        val cards = mapper.map(input(selectedSectionId = "없는-섹션"), now).sections

        assertTrue(cards[0].selected)
        assertFalse(cards[1].selected)
    }

    @Test
    fun `원본 파일 이름이 카드에 전달된다`() {
        val withFile = dbSection.copy(sourceFileName = "DB_정규화_정리.pdf")

        val cards = mapper.map(input(sections = listOf(withFile)), now).sections

        assertEquals("DB_정규화_정리.pdf", cards.single().fileName)
    }

    // ----- 오늘의 학습 분량 -----

    @Test
    fun `학습 분량은 선택한 섹션을 이동 시간에 맞춰 나눈 결과다`() {
        // 개념 하나당 120초 + 질문 15초 = 135초. 운전 중 들을 수 있는 개념은 7개(1개는 주차 전용)
        // 38분 * 0.6 = 1368초 이내에 7개(945초)가 모두 들어간다. 945초는 올림해서 16분
        val state = mapper.map(input(selectedSectionId = dbSection.sectionId), now)

        assertEquals(7, state.learningConceptCount)
        assertEquals(16, state.learningMinutes)
    }

    @Test
    fun `짧은 이동이면 학습 분량이 0이다`() {
        val state = mapper.map(input(tripType = TripType.SHORT), now)

        assertEquals(0, state.learningConceptCount)
        assertEquals(0, state.learningMinutes)
    }

    // ----- 빈 상태 -----

    @Test
    fun `올린 자료가 없으면 빈 상태다`() {
        val state = mapper.map(input(sections = emptyList()), now)

        assertTrue(state.isEmpty)
        assertEquals(0, state.learningConceptCount)
    }

    @Test
    fun `올린 자료가 있으면 빈 상태가 아니다`() {
        assertFalse(mapper.map(input(), now).isEmpty)
    }
}
