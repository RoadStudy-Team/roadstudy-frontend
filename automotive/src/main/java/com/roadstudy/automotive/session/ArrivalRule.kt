package com.roadstudy.automotive.session

/**
 * 도착했는지 판단한다. P단이라는 것만으로는 부족하다 (신호 대기나 길가 정차에서도 P로 바꿀 수 있다).
 *
 * - 한 번도 주행(D단)한 적이 없으면 도착이 아니다 (출발 전).
 * - P단이 아니면 도착이 아니다.
 * - 내비가 도착을 알렸거나, 목적지가 [NEAR_DESTINATION_METERS] 안이면 도착이다.
 * - 내비 경로가 없으면(안내를 안 쓰는 경우) 주행한 뒤 P단이면 도착이다.
 * - 목적지에서 먼 곳의 P단은 잠시 정차다.
 */
object ArrivalRule {

    /** 목적지까지 이 거리 안에서 P단이면 도착으로 본다. 실제 내비 값을 보고 조정한다 */
    const val NEAR_DESTINATION_METERS = 300

    fun isArrived(vehicle: VehicleSignals, navi: NaviSignals, hasDriven: Boolean): Boolean {
        if (!hasDriven || vehicle.gear != Gear.PARK) return false
        if (navi.arrived) return true
        val remaining = navi.remainingMeters ?: return true
        return remaining <= NEAR_DESTINATION_METERS
    }
}
