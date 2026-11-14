package com.roadstudy.automotive.session

/**
 * 실제 차량·내비 신호로 세션에 넘길 [DrivingContext] 를 만든다 (SDK 와 Android 에 의존하지 않는다).
 *
 * - 내비가 경로를 안내 중이 아니면 남은 거리·시간은 홈에서 본 경로([fallbackMeters], [fallbackSec])를 쓴다.
 *   에뮬레이터 실측: 안내가 끝난 뒤에도 내비가 지난 회전 정보를 돌려주므로, 경로가 없을 때의 회전·주의 구간·재탐색 값은 무시한다.
 * - [routeEnded]: 세션 중에 내비 안내가 끝났는지([RouteEndTracker]). P단에서 안내가 끝나면 도착이다.
 * - P단이어도 [ArrivalRule] 이 도착으로 보지 않으면(아직 주행 전, 또는 목적지 거리 조건을 켰을 때 먼 곳의 정차) 주행 중으로 본다.
 *   그래야 시작하자마자, 또는 길가 정차에서 세션이 끝나지 않는다.
 * - [ignoreNaviFocus] (개발·시연용): 내비의 회전·주의 구간·재탐색 신호를 무시한다. 에뮬레이터 차는 움직이지 않아
 *   안내를 시작하면 첫 회전이 계속 앞에 있는 것으로 나와 학습이 "잠시 멈춤"에서 풀리지 않기 때문이다.
 *   남은 거리·시간, 기어, 와이퍼, 조명은 그대로 반영한다.
 */
object LiveDrivingContext {

    fun build(
        vehicle: VehicleSignals,
        navi: NaviSignals,
        hasDriven: Boolean,
        fallbackMeters: Int?,
        fallbackSec: Int?,
        nowMillis: Long,
        ignoreNaviFocus: Boolean = false,
        routeEnded: Boolean = false
    ): DrivingContext {
        val stoppedButNotArrived =
            vehicle.gear == Gear.PARK && !ArrivalRule.isArrived(vehicle, navi, hasDriven, routeEnded = routeEnded)
        val effective = if (stoppedButNotArrived) vehicle.copy(gear = Gear.DRIVE) else vehicle
        val routeNavi = if (navi.hasRoute) navi else NaviSignals(remainingSec = fallbackSec, remainingMeters = fallbackMeters)
        val liveNavi = if (ignoreNaviFocus) {
            routeNavi.copy(distanceToNextTurnMeters = null, inCautionZone = false, lastRerouteAtMillis = null)
        } else {
            routeNavi
        }
        return DrivingContext(vehicle = effective, navi = liveNavi, nowMillis = nowMillis)
    }
}
