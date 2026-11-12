package com.roadstudy.automotive.ui.home

import com.roadstudy.automotive.session.NaviSignals

/**
 * 내비가 경로를 안내 중이면 그 목적지와 남은 거리·시간으로 홈의 "오늘의 경로"를 바꾼다.
 * 안내 중이 아니면 원래 입력(저장된 출퇴근 경로나 데모 값)을 그대로 쓴다.
 *
 * - 정체 구간 수는 내비 이벤트에 없으므로 비운다("-").
 * - 출근·퇴근 판별([HomeInput.tripType])은 바꾸지 않는다. 목적지 좌표로 판별하는 것은 따로 연결해야 한다.
 */
fun HomeInput.withNavi(navi: NaviSignals): HomeInput {
    val meters = navi.remainingMeters ?: return this
    val sec = navi.remainingSec ?: return this
    return copy(
        destinationName = navi.destinationName ?: destinationName,
        route = RouteSummary(remainingMeters = meters, etaSec = sec, congestionCount = null)
    )
}
