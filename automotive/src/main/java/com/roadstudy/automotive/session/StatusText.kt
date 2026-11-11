package com.roadstudy.automotive.session

/** 주행 중 화면 상단 한 줄 상태 문구 */
object StatusText {

    /** 안전 구간에서 질문을 들려준 뒤 속으로 떠올리는 동안의 문구 */
    const val THINKING = "질문 · 떠올려보세요"

    fun of(reason: ModeReason, step: Step = Step.EXPLAIN): String = when {
        reason == ModeReason.SAFE && step == Step.THINK -> THINKING
        else -> when (reason) {
            ModeReason.SAFE -> "안전 구간 · 학습 중"
            ModeReason.NIGHT -> "야간 · 가벼운 복습만 진행 중"
            ModeReason.LIGHT_RAIN -> "빗길 · 가벼운 복습만 진행 중"
            ModeReason.UPCOMING_TURN -> "회전 앞 · 잠시 멈춤"
            ModeReason.CAUTION_ZONE -> "주의 구간 · 잠시 멈춤"
            ModeReason.RECENT_REROUTE -> "경로 변경 · 잠시 멈춤"
            ModeReason.HEAVY_RAIN, ModeReason.FOG -> "운전에 집중할 시간 · 잠시 멈춤"
            ModeReason.PARKED -> "도착했어요"
        }
    }
}
