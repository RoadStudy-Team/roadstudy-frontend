package com.roadstudy.automotive.session

/**
 * 도착했는지 판단한다.
 *
 * - 한 번도 주행(D단)한 적이 없으면 도착이 아니다 (출발 전).
 * - P단이 아니면 도착이 아니다.
 * - MVP([REQUIRE_NEAR_DESTINATION] = false): 주행한 뒤 P단이면 도착이다. 에뮬레이터·PT 현장에서는 차가 움직이지 않아
 *   "목적지 근처"를 만들 수 없어서 단순하게 정했다. 대신 도착 전 길가에서 P단으로 바꿔도 도착으로 본다.
 * - 목적지 거리 조건을 켜면(실차 단계): 내비가 도착을 알렸거나 목적지가 [NEAR_DESTINATION_METERS] 안이면 도착이고,
 *   내비 경로가 없으면 주행한 뒤 P단이면 도착이다. 목적지에서 먼 곳의 P단은 잠시 정차다.
 */
object ArrivalRule {

    /** 목적지까지 이 거리 안에서 P단이면 도착으로 본다. 실제 내비 값을 보고 조정한다 */
    const val NEAR_DESTINATION_METERS = 300

    /** MVP 에서는 끈다. 실차 단계에서 켜면 목적지 근처의 P단만 도착으로 본다 */
    const val REQUIRE_NEAR_DESTINATION = false

    fun isArrived(
        vehicle: VehicleSignals,
        navi: NaviSignals,
        hasDriven: Boolean,
        requireNearDestination: Boolean = REQUIRE_NEAR_DESTINATION
    ): Boolean {
        if (!hasDriven || vehicle.gear != Gear.PARK) return false
        if (!requireNearDestination) return true
        if (navi.arrived) return true
        val remaining = navi.remainingMeters ?: return true
        return remaining <= NEAR_DESTINATION_METERS
    }
}
