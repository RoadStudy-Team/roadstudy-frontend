package com.roadstudy.mobile.ui.today.data

data class TodaySummary(
    val isGoToWork: Boolean,      // true: 출근길 / false: 퇴근길
    val distanceKm: Double,
    val durationMin: Int,
    val trafficKm: Double,
    val materialTitle: String,
    val studyCount: Int,
    val isCarConnected: Boolean
)