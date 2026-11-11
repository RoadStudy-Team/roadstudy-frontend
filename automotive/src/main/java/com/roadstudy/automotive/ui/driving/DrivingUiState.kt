package com.roadstudy.automotive.ui.driving

/** 상태 점 색과 문구 색을 정하는 분위기 */
enum class DrivingTone { SAFE, CAUTION, FOCUS }

/** 화면 가운데에 보여줄 것 */
enum class DrivingVisual {
    /** 파형 (학습 중) */
    WAVE,

    /** 작고 흐린 파형 (질문 뒤 떠올리는 시간, 도착) */
    WAVE_QUIET,

    /** 위에 아이콘 자리가 있는 낮은 파형 (야간·빗길 가벼운 복습) */
    WAVE_WITH_ICON,

    /** 일시정지 원 (운전에 집중할 순간) */
    PAUSED
}

/** 오른쪽 아래 안내 */
enum class DrivingHint {
    /** "◀ 다시 들려줘 ▶ 알아요" 핸들 버튼 안내 */
    STEERING,

    /** "조건이 풀리면 자동으로 이어져요" */
    AUTO_RESUME,

    NONE
}

/** 주행 중 화면에 그대로 뿌릴 값. 문장 조립은 strings.xml 이 한다 */
data class DrivingUiState(
    val tone: DrivingTone,
    val visual: DrivingVisual,
    val hint: DrivingHint,
    /** 상단 한 줄 상태 문구 */
    val statusText: String,
    val destinationName: String,
    /** 남은 거리(km), 예: "12.1". 경로를 모르면 null */
    val distanceKm: String?,
    /** 남은 시간(분). 경로를 모르면 null */
    val etaMinutes: Int?,
    val conceptIndex: Int,
    val conceptTotal: Int,
    val sectionTitle: String,
    /** 도착해서 주차(P단)가 된 상태. 주차 후 복습 화면으로 넘어가야 한다 */
    val arrived: Boolean = false
)
