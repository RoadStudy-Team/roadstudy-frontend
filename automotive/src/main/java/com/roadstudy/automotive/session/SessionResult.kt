package com.roadstudy.automotive.session

import com.roadstudy.shared.model.Concept
import com.roadstudy.shared.model.ConceptStatus

/**
 * 도착으로 끝난 주행 학습 세션의 실제 결과. 주차 후 복습 화면이 데모 값 대신 이 값을 보여 준다.
 * 진도(statuses, reviewQueue)는 아직 저장소(Room)가 없어서 이 앱이 켜져 있는 동안만 유지된다.
 */
data class SessionResult(
    val sectionId: String?,
    val tripType: TripType,
    /** 세션을 시작해서 도착할 때까지 걸린 시간 */
    val elapsedMillis: Long,
    /** 이번 세션에서 들은 개념 (들은 순서). 듣던 중에 도착한 개념도 넣는다 */
    val heardConcepts: List<Concept>,
    val statuses: Map<String, ConceptStatus>,
    val reviewQueue: List<String>
) {
    companion object {
        fun of(
            plan: SessionPlan,
            state: SessionUiState,
            progress: ProgressTracker,
            sectionId: String?,
            tripType: TripType,
            elapsedMillis: Long
        ) = SessionResult(
            sectionId = sectionId,
            tripType = tripType,
            elapsedMillis = elapsedMillis,
            heardConcepts = plan.items.take(state.conceptIndex).map { it.concept },
            statuses = progress.statuses(),
            reviewQueue = progress.reviewQueue()
        )
    }
}
