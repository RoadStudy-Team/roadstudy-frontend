package com.roadstudy.automotive.ui.home

import android.content.Intent
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
    private val startEmpty: Boolean = false,
    /** true 면 출근도 퇴근도 아닌 "기타 이동"으로 시작한다. 시작 전에 학습할지 묻는 화면을 거친다 */
    private val otherTrip: Boolean = false
) : HomeDataSource {

    override fun load(): HomeInput = if (startEmpty) {
        sample().copy(sections = emptyList(), statuses = emptyMap(), reviewQueue = emptyList(), selectedSectionId = null)
    } else {
        sample()
    }

    override fun sample(): HomeInput = HomeInput(
        tripType = if (otherTrip) TripType.OTHER else TripType.TO_WORK,
        destinationName = if (otherTrip) "강남 OO빌딩" else "회사",
        route = if (otherTrip) {
            // 가짜 경로(기타 이동): 24.6km, 46분
            RouteSummary(remainingMeters = 24_600, etaSec = 46 * 60, congestionCount = 1)
        } else {
            // 가짜 경로: 18.4km, 38분, 정체 구간 1곳
            RouteSummary(remainingMeters = 18_400, etaSec = 38 * 60, congestionCount = 1)
        },
        sections = SampleStudySections.all,
        // 프로세스와 스레드는 10개 중 5개를 알고 있고, 오픽 영어는 6개 중 4개를 알고 있다
        statuses = listOf("p01", "p02", "p03", "p04", "p06", "e01", "e02", "e03", "e04")
            .associateWith { ConceptStatus.LEARNED },
        // 다시 듣기로 표시해 둔 개념 (프로세스와 스레드 3개, 오픽 영어 1개)
        reviewQueue = listOf("p02", "p06", "p07", "e03"),
        selectedSectionId = SampleStudySections.processThread.sectionId
    )

    companion object {
        /** 개발용: 실행 인자 `--ez home_other_trip true` 로 기타 이동 데모를 켠다 */
        const val EXTRA_OTHER_TRIP = "home_other_trip"

        /** 화면들이 같은 데모 설정(실행 인자)을 쓰도록 한곳에서 만든다 */
        fun fromIntent(intent: Intent, startEmpty: Boolean = false) = FakeHomeDataSource(
            startEmpty = startEmpty,
            otherTrip = intent.getBooleanExtra(EXTRA_OTHER_TRIP, false)
        )
    }
}
