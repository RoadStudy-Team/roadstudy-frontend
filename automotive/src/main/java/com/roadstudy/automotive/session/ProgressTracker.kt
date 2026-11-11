package com.roadstudy.automotive.session

import com.roadstudy.shared.model.ConceptStatus

/** 핸들 버튼 또는 주차 후 정리 화면에서 받은 자기평가 */
enum class SelfCheck { KNOW, RELISTEN }

/**
 * 개념별 상태와 "다시 듣기" 대기열을 관리한다.
 * 출근길에 다시 듣기로 표시한 개념은 퇴근길(다음 세션) 앞쪽에 다시 나온다.
 * 실제 앱에서는 data 패키지(Room)에 저장한다.
 */
class ProgressTracker(
    initialStatuses: Map<String, ConceptStatus> = emptyMap(),
    initialReviewQueue: List<String> = emptyList()
) {
    private val statuses = initialStatuses.toMutableMap()
    private val reviewQueue = ArrayDeque(initialReviewQueue)

    fun statuses(): Map<String, ConceptStatus> = statuses.toMap()

    fun reviewQueue(): List<String> = reviewQueue.toList()

    /** 개념 설명을 끝까지 들었을 때 */
    fun markListened(conceptId: String) {
        if (statuses[conceptId] == null || statuses[conceptId] == ConceptStatus.NOT_STARTED) {
            statuses[conceptId] = ConceptStatus.LEARNED
        }
    }

    fun record(conceptId: String, check: SelfCheck) {
        when (check) {
            SelfCheck.KNOW -> {
                statuses[conceptId] = ConceptStatus.LEARNED
                reviewQueue.remove(conceptId)
            }
            SelfCheck.RELISTEN -> {
                statuses[conceptId] = ConceptStatus.NEEDS_REVIEW
                reviewQueue.remove(conceptId)
                reviewQueue.addFirst(conceptId)
            }
        }
    }
}
