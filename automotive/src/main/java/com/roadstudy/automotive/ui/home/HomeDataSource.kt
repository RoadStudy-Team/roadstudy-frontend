package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.TripType
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.SampleStudySections

/**
 * 홈 화면의 입력([HomeInput])을 공급하는 곳.
 * TODO: 지금은 가짜 값을 주는 [FakeHomeDataSource] 뿐이다. Pleos SDK(내비, 기어)와 저장소(Room) 구현으로 교체한다.
 */
interface HomeDataSource {
    fun load(): HomeInput

    /** 빈 상태에서 "샘플 자료로 체험하기"를 눌렀을 때 쓰는 샘플 입력 (PT 시연용) */
    fun sample(): HomeInput
}

class FakeHomeDataSource(
    /** true 면 올린 자료가 하나도 없는 빈 상태로 시작한다 */
    private val startEmpty: Boolean = false
) : HomeDataSource {

    override fun load(): HomeInput = if (startEmpty) {
        sample().copy(sections = emptyList(), statuses = emptyMap(), reviewQueue = emptyList(), selectedSectionId = null)
    } else {
        sample()
    }

    override fun sample(): HomeInput = HomeInput(
        tripType = TripType.TO_WORK,
        destinationName = "회사",
        // 가짜 경로: 18.4km, 38분, 정체 구간 1곳
        route = RouteSummary(remainingMeters = 18_400, etaSec = 38 * 60, congestionCount = 1),
        sections = SampleStudySections.all,
        // DB 정규화는 8개 중 5개를 알고 있고, 비즈니스 영어는 6개 중 4개를 알고 있다
        statuses = listOf("c01", "c02", "c03", "c04", "c05", "e01", "e02", "e03", "e04")
            .associateWith { ConceptStatus.LEARNED },
        // 다시 듣기로 표시해 둔 개념 (DB 정규화 3개, 비즈니스 영어 1개)
        reviewQueue = listOf("c02", "c05", "c06", "e03"),
        selectedSectionId = SampleStudySections.dbNormalization.sectionId
    )
}
