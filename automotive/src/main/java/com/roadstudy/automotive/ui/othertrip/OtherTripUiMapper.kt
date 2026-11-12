package com.roadstudy.automotive.ui.othertrip

import com.roadstudy.automotive.session.SessionPlanner
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.ui.home.HomeInput
import com.roadstudy.automotive.ui.home.HomeUiMapper

/**
 * 홈의 입력([HomeInput])으로 다른 이동 안내 화면용 값([OtherTripUiState])을 만든다.
 * 이 화면은 "기타 이동"을 위한 것이라, 이동 유형과 상관없이 기타 이동 기준(최대 30분)으로 계획한다.
 * 순수 Kotlin 이라 단위 테스트로 검증한다.
 */
class OtherTripUiMapper(
    private val planner: SessionPlanner = SessionPlanner()
) {

    fun map(input: HomeInput, sectionId: String?, dontAskAgain: Boolean = false): OtherTripUiState {
        val section = input.sections.firstOrNull { it.sectionId == sectionId }
            ?: input.sections.firstOrNull()
        val route = input.route

        val plan = if (section != null && route != null) {
            planner.plan(section, input.statuses, input.reviewQueue, route.etaSec, TripType.OTHER)
        } else {
            null
        }

        // 이동이 30분보다 짧으면 이동 시간까지만 학습한다
        val capMinutes = SessionPlanner.OTHER_TRIP_CAP_SEC / SECONDS_PER_MINUTE
        val etaMinutes = route?.let { HomeUiMapper.ceilMinutes(it.etaSec) }

        return OtherTripUiState(
            destinationName = input.destinationName,
            distanceKm = route?.let { HomeUiMapper.formatDistanceKm(it.remainingMeters) },
            etaMinutes = etaMinutes,
            maxLearningMinutes = if (etaMinutes == null) capMinutes else minOf(etaMinutes, capMinutes),
            conceptCount = plan?.items?.size ?: 0,
            sectionTitle = section?.title.orEmpty(),
            dontAskAgain = dontAskAgain
        )
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60
    }
}
