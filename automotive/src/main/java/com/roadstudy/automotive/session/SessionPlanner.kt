package com.roadstudy.automotive.session

import com.roadstudy.shared.model.Concept
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.StudySection

enum class PlanItemKind { NEW, REVIEW }

data class PlanItem(val concept: Concept, val kind: PlanItemKind)

data class SessionPlan(
    val items: List<PlanItem>,
    /** 예상 학습 시간(초). 회전·보호구역 등에서 멈추는 시간을 뺀 값 */
    val estimatedLearningSec: Int,
    /** 계산·코드형처럼 운전 중 들을 수 없어 주차 화면에서만 볼 개념 */
    val parkOnlyConcepts: List<Concept>
) {
    val newCount: Int get() = items.count { it.kind == PlanItemKind.NEW }
    val reviewCount: Int get() = items.count { it.kind == PlanItemKind.REVIEW }
}

/**
 * 남은 시간(ETA)에 맞춰 이번 세션에 들을 개념을 고른다.
 * - 출근길: 이어서 배울 개념 → 다시 듣기
 * - 퇴근길: 다시 듣기 → 이어서 배울 개념
 * - 그 외 이동: 최대 30분
 */
class SessionPlanner(
    private val usableRatio: Double = USABLE_RATIO,
    private val questionOverheadSec: Int = QUESTION_OVERHEAD_SEC,
    private val otherTripCapSec: Int = OTHER_TRIP_CAP_SEC
) {

    fun plan(
        section: StudySection,
        statuses: Map<String, ConceptStatus>,
        reviewQueue: List<String>,
        etaSec: Int,
        tripType: TripType
    ): SessionPlan {
        if (tripType == TripType.SHORT) return SessionPlan(emptyList(), 0, emptyList())

        val driveTimeSec = if (tripType == TripType.OTHER) minOf(etaSec, otherTripCapSec) else etaSec
        val budgetSec = (driveTimeSec * usableRatio).toInt()

        val byId = section.concepts.associateBy { it.id }
        val reviews = reviewQueue.mapNotNull { byId[it] }
            .filter { it.driveSafe }
            .map { PlanItem(it, PlanItemKind.REVIEW) }
        val news = section.concepts
            .filter { it.driveSafe && (statuses[it.id] ?: ConceptStatus.NOT_STARTED) == ConceptStatus.NOT_STARTED }
            .filterNot { it.id in reviewQueue }
            .map { PlanItem(it, PlanItemKind.NEW) }

        val ordered = if (tripType == TripType.TO_HOME) reviews + news else news + reviews

        val picked = mutableListOf<PlanItem>()
        var usedSec = 0
        for (item in ordered) {
            val cost = costOf(item.concept)
            if (usedSec + cost > budgetSec) continue
            picked += item
            usedSec += cost
        }

        val parkOnly = section.concepts.filter { !it.driveSafe && statuses[it.id] != ConceptStatus.LEARNED }
        return SessionPlan(picked, usedSec, parkOnly)
    }

    private fun costOf(concept: Concept): Int = concept.durationSec + questionOverheadSec

    companion object {
        /** 42분 운전 중 약 25분 학습 → 회전·보호구역·재탐색 등에서 쉬는 시간을 뺀 비율 */
        const val USABLE_RATIO = 0.6
        /** 질문 + 떠올리는 시간 + 정답 */
        const val QUESTION_OVERHEAD_SEC = 15
        const val OTHER_TRIP_CAP_SEC = 30 * 60
    }
}
