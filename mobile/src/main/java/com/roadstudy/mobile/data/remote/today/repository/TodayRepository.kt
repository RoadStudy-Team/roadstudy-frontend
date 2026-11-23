package com.roadstudy.mobile.data.remote.today.repository

import android.content.Context
import android.location.Location
import com.roadstudy.mobile.data.remote.onboarding.repository.OnBoardingRepository
import com.roadstudy.mobile.ui.today.data.TodaySummary
import java.time.LocalTime
import kotlin.math.roundToInt

class TodayRepository(context: Context) {

    private val prefs = context.getSharedPreferences("routine_prefs", Context.MODE_PRIVATE)
    private val placeRepository = OnBoardingRepository(context)

    // TODO: API 연동 시 이 함수 내용만 서버/SDK 응답으로 교체
    fun getTodaySummary(): TodaySummary {
        val isGoToWork = isNextSessionGoToWork()
        val (distanceKm, durationMin) = estimateRoute()

        return TodaySummary(
            isGoToWork = isGoToWork,
            distanceKm = distanceKm,
            durationMin = durationMin,
            trafficKm = 1.2,                 // TODO: 실시간 교통 API
            materialTitle = "DB 정규화 정리",  // TODO: 서버(업로드 자료)
            studyCount = 14,                 // TODO: 서버
            isCarConnected = true            // TODO: Pleos SDK 차량 연결 상태
        )
    }

    // 다음 세션: 출근 시간 전이면 출근길, 퇴근 시간 전이면 퇴근길, 이후는 다음 날 출근길
    private fun isNextSessionGoToWork(): Boolean {
        val now = LocalTime.now()
        val go = LocalTime.of(prefs.getInt("go_hour", 8), prefs.getInt("go_minute", 0))
        val back = LocalTime.of(prefs.getInt("back_hour", 18), prefs.getInt("back_minute", 0))
        return when {
            now <= go -> true
            now <= back -> false
            else -> true
        }
    }

    // 저장된 집/회사 좌표로 직선거리 기반 추정 (임시). TODO: 경로 API로 교체
    private fun estimateRoute(): Pair<Double, Int> {
        val home = placeRepository.loadPlace(OnBoardingRepository.KEY_HOME)
        val work = placeRepository.loadPlace(OnBoardingRepository.KEY_WORK)
        if (home == null || work == null) return 18.4 to 38

        val result = FloatArray(1)
        Location.distanceBetween(
            home.latitude, home.longitude,
            work.latitude, work.longitude,
            result
        )
        val km = (result[0] / 1000.0 * 1.3)          // 도로 보정 1.3배
        val roundedKm = (km * 10).roundToInt() / 10.0
        val minutes = (km / 30.0 * 60).roundToInt()  // 평균 30km/h 가정
        return roundedKm to minutes.coerceAtLeast(1)
    }
}