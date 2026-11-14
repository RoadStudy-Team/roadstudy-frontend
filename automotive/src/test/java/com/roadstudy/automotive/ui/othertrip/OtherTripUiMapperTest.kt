package com.roadstudy.automotive.ui.othertrip

import com.roadstudy.automotive.ui.home.FakeHomeDataSource
import com.roadstudy.automotive.ui.home.HomeInput
import com.roadstudy.automotive.ui.home.RouteSummary
import com.roadstudy.shared.model.SampleStudySections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OtherTripUiMapperTest {

    private val mapper = OtherTripUiMapper()
    private val sectionId = SampleStudySections.processThread.sectionId

    private fun otherTripInput(route: RouteSummary? = RouteSummary(24_600, 46 * 60, 1)): HomeInput =
        FakeHomeDataSource(otherTrip = true).load().copy(route = route)

    @Test
    fun `기타 이동의 목적지와 거리, 이동 시간이 화면 값으로 바뀐다`() {
        val state = mapper.map(otherTripInput(), sectionId)

        assertEquals("강남 OO빌딩", state.destinationName)
        assertEquals("24.6", state.distanceKm)
        assertEquals(46, state.etaMinutes)
    }

    @Test
    fun `이동이 30분보다 길면 학습은 최대 30분이다`() {
        assertEquals(30, mapper.map(otherTripInput(), sectionId).maxLearningMinutes)
    }

    @Test
    fun `이동이 30분보다 짧으면 이동 시간까지만 학습한다`() {
        val state = mapper.map(otherTripInput(RouteSummary(9_000, 20 * 60, 0)), sectionId)

        assertEquals(20, state.maxLearningMinutes)
    }

    @Test
    fun `경로를 모르면 거리와 이동 시간은 비고 학습은 최대 30분이다`() {
        val state = mapper.map(otherTripInput(route = null), sectionId)

        assertNull(state.distanceKm)
        assertNull(state.etaMinutes)
        assertEquals(30, state.maxLearningMinutes)
        assertEquals(0, state.conceptCount)
    }

    @Test
    fun `들을 개념 수는 최대 30분 안에서 계획한 개념 수다`() {
        // 46분 이동이라도 30분 상한의 60%(1080초)만 쓰고, 개념 하나가 135초라 들을 수 있는 샘플 6개가 모두 들어간다
        assertEquals(6, mapper.map(otherTripInput(), sectionId).conceptCount)
    }

    @Test
    fun `이동 유형이 출근이어도 이 화면은 기타 이동 기준으로 계획한다`() {
        val toWork = FakeHomeDataSource().load().copy(route = RouteSummary(40_000, 90 * 60, 0))

        val state = mapper.map(toWork, sectionId)

        // 90분 이동이지만 기타 이동 상한(30분)이 적용되어 개념은 8개(1080초 ÷ 135초) 이하
        assertEquals(30, state.maxLearningMinutes)
        assertTrue(state.conceptCount <= 8)
    }

    @Test
    fun `선택한 섹션의 이름이 담기고 없는 섹션이면 첫 섹션을 쓴다`() {
        assertEquals("프로세스와 스레드 소개", mapper.map(otherTripInput(), sectionId).sectionTitle)
        assertEquals("프로세스와 스레드 소개", mapper.map(otherTripInput(), "없는섹션").sectionTitle)
        assertEquals("오픽 답변 영어 표현", mapper.map(otherTripInput(), "opic-english").sectionTitle)
    }

    @Test
    fun `다음부터 묻지 않기 체크 상태가 그대로 전달된다`() {
        assertFalse(mapper.map(otherTripInput(), sectionId).dontAskAgain)
        assertTrue(mapper.map(otherTripInput(), sectionId, dontAskAgain = true).dontAskAgain)
    }

    @Test
    fun `자료가 없으면 개념 0개이고 섹션 이름이 비어 있다`() {
        val empty = FakeHomeDataSource(startEmpty = true, otherTrip = true).load()

        val state = mapper.map(empty, null)

        assertEquals(0, state.conceptCount)
        assertEquals("", state.sectionTitle)
    }
}
