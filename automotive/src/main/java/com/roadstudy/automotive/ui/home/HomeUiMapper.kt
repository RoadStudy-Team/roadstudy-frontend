package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.SessionPlanner
import com.roadstudy.shared.model.ConceptStatus
import com.roadstudy.shared.model.StudySection
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * HomeInput(원재료)을 HomeUiState(화면용 값)로 바꾼다.
 *
 * Android 에 의존하지 않는 순수 Kotlin 이라 단위 테스트로 검증한다 (HomeUiMapperTest).
 * "오늘의 학습 분량"은 [SessionPlanner] 의 계산 결과를 그대로 쓴다.
 */
class HomeUiMapper(
    private val planner: SessionPlanner = SessionPlanner()
) {

    fun map(input: HomeInput, now: LocalTime): HomeUiState {
        // 선택된 섹션이 없거나 목록에 없으면 첫 번째 섹션을 선택한 것으로 본다
        val selected = input.sections.firstOrNull { it.sectionId == input.selectedSectionId }
            ?: input.sections.firstOrNull()

        val route = input.route

        // 선택한 섹션을 이번 이동 시간에 맞춘 학습 계획 (경로나 섹션이 없으면 계산하지 않는다)
        val plan = if (selected != null && route != null) {
            planner.plan(
                section = selected,
                statuses = input.statuses,
                reviewQueue = input.reviewQueue,
                etaSec = route.etaSec,
                tripType = input.tripType
            )
        } else {
            null
        }

        return HomeUiState(
            tripType = input.tripType,
            destinationName = input.destinationName,
            distanceKm = route?.let { formatDistanceKm(it.remainingMeters) },
            etaMinutes = route?.let { ceilMinutes(it.etaSec) },
            arrivalTime = route?.let { formatArrival(now, it.etaSec) },
            congestionCount = route?.congestionCount,
            learningMinutes = plan?.let { ceilMinutes(it.estimatedLearningSec) } ?: 0,
            learningConceptCount = plan?.items?.size ?: 0,
            sections = input.sections.map { section ->
                toCard(section, input, isSelected = section.sectionId == selected?.sectionId)
            }
        )
    }

    private fun toCard(section: StudySection, input: HomeInput, isSelected: Boolean): SectionCardUi {
        val total = section.concepts.size
        val learned = section.concepts.count { input.statuses[it.id] == ConceptStatus.LEARNED }
        val conceptIds = section.concepts.map { it.id }.toSet()

        return SectionCardUi(
            sectionId = section.sectionId,
            type = section.type,
            title = section.title,
            fileName = section.sourceFileName,
            conceptCount = total,
            // 5/8 = 62.5% 는 소수점을 버려 62%로 보여준다
            progressPercent = if (total == 0) 0 else learned * 100 / total,
            reviewCount = input.reviewQueue.count { it in conceptIds },
            selected = isSelected,
            // 다시 듣기 목록의 순서대로, 이 섹션에 속한 개념 제목만 최대 3개
            reviewTitles = input.reviewQueue
                .mapNotNull { id -> section.concepts.firstOrNull { it.id == id }?.title }
                .take(SectionCardUi.MAX_REVIEW_TITLES)
        )
    }

    companion object {
        private val ARRIVAL_FORMAT = DateTimeFormatter.ofPattern("HH:mm")

        /** 미터를 km 로 바꿔 소수점 한 자리로 보여준다. 18400 -> "18.4" */
        fun formatDistanceKm(meters: Int): String =
            String.format(Locale.US, "%.1f", meters / 1000.0)

        /** 초를 분으로 바꾼다. 남은 시간은 올림해서 보여준다. 2281초 -> 39분 */
        fun ceilMinutes(sec: Int): Int = (sec + 59) / 60

        /** 지금 시각에 남은 시간을 더한 도착 예정 시각. 08:14 + 38분 -> "08:52" */
        fun formatArrival(now: LocalTime, etaSec: Int): String =
            now.plusSeconds(etaSec.toLong()).format(ARRIVAL_FORMAT)
    }
}
