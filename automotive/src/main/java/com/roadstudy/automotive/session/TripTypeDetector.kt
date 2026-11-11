package com.roadstudy.automotive.session

import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(val latitude: Double, val longitude: Double)

/** 휴대폰 "내 이동 루틴"에서 한 번 설정한 값 */
data class CommuteRoutine(
    val home: GeoPoint,
    val work: GeoPoint,
    val toWorkWindow: ClosedRange<LocalTime> = LocalTime.of(6, 0)..LocalTime.of(11, 0),
    val toHomeWindow: ClosedRange<LocalTime> = LocalTime.of(16, 0)..LocalTime.of(23, 59),
    val suggestOnOtherTrips: Boolean = true
)

enum class TripType {
    /** 출근길: 이어서 배울 개념 중심 */
    TO_WORK,
    /** 퇴근길: 다시 듣기 중심 */
    TO_HOME,
    /** 그 외 이동: 출발 전 한 번만 묻고 최대 30분 */
    OTHER,
    /** 15분 미만 짧은 이동: 세션을 제안하지 않음 */
    SHORT
}

class TripTypeDetector(private val nearbyMeters: Double = NEARBY_METERS) {

    fun detect(
        routine: CommuteRoutine,
        start: GeoPoint,
        destination: GeoPoint?,
        etaSec: Int?,
        time: LocalTime,
        day: DayOfWeek
    ): TripType {
        if (etaSec != null && etaSec < SHORT_TRIP_SEC) return TripType.SHORT

        // 1순위: 목적지
        if (destination != null) {
            if (isNear(destination, routine.work)) return TripType.TO_WORK
            if (isNear(destination, routine.home)) return TripType.TO_HOME
            return TripType.OTHER
        }

        // 2순위: 출발 위치 + 3순위: 평일 시간대
        val isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
        if (isNear(start, routine.home) && isWeekday && time in routine.toWorkWindow) return TripType.TO_WORK
        if (isNear(start, routine.work) && isWeekday && time in routine.toHomeWindow) return TripType.TO_HOME

        return TripType.OTHER
    }

    private fun isNear(a: GeoPoint, b: GeoPoint): Boolean = distanceMeters(a, b) <= nearbyMeters

    companion object {
        const val NEARBY_METERS = 500.0
        const val SHORT_TRIP_SEC = 15 * 60
        private const val EARTH_RADIUS_METERS = 6_371_000.0

        fun distanceMeters(a: GeoPoint, b: GeoPoint): Double {
            val dLat = Math.toRadians(b.latitude - a.latitude)
            val dLon = Math.toRadians(b.longitude - a.longitude)
            val h = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2).pow(2)
            return 2 * EARTH_RADIUS_METERS * asin(sqrt(h))
        }
    }
}
