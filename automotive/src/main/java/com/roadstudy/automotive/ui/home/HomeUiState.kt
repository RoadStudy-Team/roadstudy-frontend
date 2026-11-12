package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.TripType
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.MaterialType
import com.roadstudy.shared.model.StudySection

/*
 * 홈 화면 데이터 흐름
 *   HomeInput   : 차량·내비·저장소에서 들어오는 원재료
 *   HomeUiState : 화면에 그대로 뿌릴 수 있게 가공한 값
 *
 *   HomeInput --(HomeUiMapper)--> HomeUiState --(HomeFragment)--> 화면
 */

/** 내비게이션이 알려주는 오늘의 경로 요약 */
data class RouteSummary(
    val remainingMeters: Int,
    val etaSec: Int,
    val congestionCount: Int
)

/** 홈 화면을 그리는 데 필요한 원재료 */
data class HomeInput(
    val tripType: TripType,
    val destinationName: String,
    val route: RouteSummary?,
    val sections: List<StudySection>,
    val statuses: Map<String, ConceptStatus>,
    val reviewQueue: List<String>,
    val selectedSectionId: String?
)

/** 스터디 섹션 카드 하나에 보여줄 값 */
data class SectionCardUi(
    val sectionId: String,
    val type: MaterialType,
    val title: String,
    val fileName: String?,
    val conceptCount: Int,
    val progressPercent: Int,
    val reviewCount: Int,
    val selected: Boolean,
    /** 카드 가운데에 보여줄 "다시 들을 개념" 제목들 (최대 MAX_REVIEW_TITLES 개) */
    val reviewTitles: List<String> = emptyList()
) {
    companion object {
        const val MAX_REVIEW_TITLES = 3
    }
}

/** 홈 화면 전체에 보여줄 값. 문장은 만들지 않고 숫자와 글자만 담는다 (문장은 strings.xml 이 만든다). */
data class HomeUiState(
    val tripType: TripType,
    val destinationName: String,
    /** 남은 거리(km), 예: "18.4". 경로가 없으면 null */
    val distanceKm: String?,
    /** 남은 시간(분). 경로가 없으면 null */
    val etaMinutes: Int?,
    /** 도착 예정 시각, 예: "08:52". 경로가 없으면 null */
    val arrivalTime: String?,
    val congestionCount: Int?,
    /** 오늘의 학습 분량 */
    val learningMinutes: Int,
    val learningConceptCount: Int,
    val sections: List<SectionCardUi>
) {
    /** 올린 자료가 하나도 없으면 빈 상태 화면을 보여준다 */
    val isEmpty: Boolean get() = sections.isEmpty()
}
