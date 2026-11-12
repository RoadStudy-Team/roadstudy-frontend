package com.roadstudy.automotive.ui.othertrip

/** 다른 이동 안내 화면(출발 전 "이번 이동도 학습할까요?")에 그대로 뿌릴 값 */
data class OtherTripUiState(
    val destinationName: String,
    /** 남은 거리(km), 예: "24.6". 경로를 모르면 null */
    val distanceKm: String?,
    /** 이동 시간(분). 경로를 모르면 null */
    val etaMinutes: Int?,
    /** 이번에 학습할 최대 시간(분). 이동이 길어도 상한(30분)을 넘지 않는다 */
    val maxLearningMinutes: Int,
    /** 이번에 들을 개념 수 */
    val conceptCount: Int,
    val sectionTitle: String,
    /** "이 장소는 다음부터 묻지 않기" 체크 상태 */
    val dontAskAgain: Boolean
)
