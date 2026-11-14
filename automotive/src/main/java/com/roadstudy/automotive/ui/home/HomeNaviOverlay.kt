package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.CommuteInfo
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.CommuteTargetPicker
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.TripType
import com.roadstudy.automotive.session.TripTypeDetector
import java.time.LocalTime

/**
 * 내비 값으로 "오늘의 경로"와 출근/퇴근 판별을 바꾼다. 우선순위는 다음과 같다.
 *
 * 1. 내비가 경로를 안내 중이면 그 목적지와 남은 거리·시간을 쓴다.
 *    목적지가 즐겨찾기의 회사·집 근처인지로 출근/퇴근/기타 이동을 나눈다. 정체 구간 수는 내비 이벤트에 없어 비운다("-").
 * 2. 안내 중이 아니면 회사·집 중 하나의 경로 미리보기를 쓴다. 사용자가 홈에서 고른 쪽([chosen])이 먼저고,
 *    고르지 않았으면 시간대로 고른다([CommuteTargetPicker]). 미리보기가 아직 오지 않았으면 이름만 보이고 거리·시간은 비운다.
 * 3. 즐겨찾기를 받았지만 집·회사가 모두 없으면 경로를 비우고 기타 이동으로 본다.
 * 4. 아직 내비에서 아무것도 받지 못했으면(연결 전, SDK 없는 환경) 원래 입력(데모 값)을 그대로 쓴다.
 */
fun HomeInput.withRoute(
    navi: NaviSignals,
    commute: CommuteInfo = CommuteInfo(),
    chosen: CommuteTarget? = null,
    time: LocalTime = LocalTime.now(),
    detector: TripTypeDetector = TripTypeDetector()
): HomeInput {
    val places = commute.places
    val meters = navi.remainingMeters
    val sec = navi.remainingSec
    if (meters != null && sec != null) {
        val destination = navi.destinationPoint
        return copy(
            tripType = if (places != null && destination != null) {
                detector.classifyDestination(places, destination)
            } else {
                tripType
            },
            destinationName = navi.destinationName ?: destinationName,
            route = RouteSummary(remainingMeters = meters, etaSec = sec, congestionCount = null)
        )
    }
    if (places == null) return this

    val target = CommuteTargetPicker.resolve(places, chosen, time)
    val place = target?.let { CommuteTargetPicker.placeOf(places, it) }
    if (target == null || place == null) {
        return copy(tripType = TripType.OTHER, destinationName = "", route = null)
    }
    val preview = commute.previews[target]
    return copy(
        tripType = if (target == CommuteTarget.WORK) TripType.TO_WORK else TripType.TO_HOME,
        destinationName = place.name,
        route = preview?.let {
            RouteSummary(remainingMeters = it.remainingMeters, etaSec = it.etaSec, congestionCount = it.congestionCount)
        }
    )
}

/**
 * 경로 카드의 회사/집 버튼 상태. 내비가 안내 중이거나(실제 목적지가 먼저) 집·회사가 하나도 등록돼 있지 않으면 null(버튼 숨김).
 */
fun commuteToggle(
    navi: NaviSignals,
    commute: CommuteInfo,
    chosen: CommuteTarget?,
    time: LocalTime
): CommuteToggleUi? {
    if (navi.hasRoute) return null
    val places = commute.places ?: return null
    val selected = CommuteTargetPicker.resolve(places, chosen, time) ?: return null
    return CommuteToggleUi(
        selected = selected,
        workEnabled = places.work != null,
        homeEnabled = places.home != null
    )
}
