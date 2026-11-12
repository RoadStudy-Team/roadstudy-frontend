package com.roadstudy.automotive.navi

import ai.pleos.playground.navi.constants.NavigationStatus
import com.roadstudy.automotive.session.NaviSignals

/**
 * 내비 이벤트가 따로따로 오는 값을 [NaviSignals] 하나로 합친다 (SDK 와 Android 에 의존하지 않아 단위 테스트가 쉽다).
 * 경로 안내가 끝나거나 취소되면 경로 값을 비워서, 지난 경로의 남은 거리가 화면에 남지 않게 한다.
 */
class NaviSignalAggregator(private val clock: () -> Long = System::currentTimeMillis) {

    var signals = NaviSignals()
        private set

    /** 목적지 정보: 이름과 남은 거리(m), 남은 시간(초) */
    fun onDestination(name: String, remainMeters: Int, remainSec: Int) {
        // 경로가 없을 때 SDK 는 이름·거리·시간이 모두 빈 목적지를 보낸다. 이것을 경로로 보면 0m 남은 것으로 오해한다
        if (name.isBlank() && remainMeters == 0 && remainSec == 0) {
            signals = NaviSignals()
            return
        }
        signals = signals.copy(
            destinationName = name.ifBlank { null },
            remainingMeters = remainMeters,
            remainingSec = remainSec,
            arrived = false
        )
    }

    /** 주행 정보: 현재 위치에서 목적지까지 걸리는 시간(초) */
    fun onDrivingEta(remainSec: Int) {
        if (!signals.hasRoute) return // 목적지를 모르는 상태의 시간만 먼저 받으면 경로가 있는 것처럼 보이지 않게 한다
        signals = signals.copy(remainingSec = remainSec)
    }

    /** 다음 회전까지 남은 거리(m). 안내할 회전이 없으면 null */
    fun onNextTurn(distanceMeters: Int?) {
        signals = signals.copy(distanceToNextTurnMeters = distanceMeters)
    }

    /** 주의 구간 목록(각각 남은 거리 m). 가까운 구간이 있으면 주의 구간 안으로 본다 */
    fun onCautionPoints(remainMeters: List<Int>) {
        signals = signals.copy(inCautionZone = remainMeters.any { it <= CAUTION_ZONE_METERS })
    }

    fun onRerouted() {
        signals = signals.copy(lastRerouteAtMillis = clock())
    }

    fun onArrived() {
        signals = signals.copy(arrived = true)
    }

    fun onRouteCancelled() {
        signals = NaviSignals()
    }

    /** 경로 안내 중이 아니게 되면 경로 값을 비우고, 안내가 시작되면 도착 표시를 지운다 */
    fun onStatus(status: NavigationStatus) {
        signals = when (status) {
            NavigationStatus.ROUTE_DRIVE -> signals.copy(arrived = false)
            else -> NaviSignals()
        }
    }

    companion object {
        /** 주의 구간까지 이 거리 안이면 집중 모드로 본다. 실제 이벤트 값을 보고 조정한다 */
        const val CAUTION_ZONE_METERS = 300
    }
}
