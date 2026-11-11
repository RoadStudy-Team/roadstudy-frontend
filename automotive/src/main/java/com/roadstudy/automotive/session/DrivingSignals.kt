package com.roadstudy.automotive.session

/**
 * Pleos SDK 에서 읽어온 값을 앱이 쓰기 쉬운 형태로 옮겨 담은 것.
 * session 로직은 이 클래스만 보고 판단하므로 Pleos SDK 에 직접 의존하지 않는다.
 * (vehicle / navi 패키지에서 SDK 값을 이 형태로 변환해 넣어준다)
 */
enum class Gear { PARK, REVERSE, NEUTRAL, DRIVE }

enum class WiperLevel { OFF, LOW, HIGH }

data class VehicleSignals(
    val gear: Gear = Gear.PARK,
    val wiperLevel: WiperLevel = WiperLevel.OFF,
    val lowBeamOn: Boolean = false,
    val fogLightOn: Boolean = false
)

data class NaviSignals(
    val remainingSec: Int? = null,
    val remainingMeters: Int? = null,
    val distanceToNextTurnMeters: Int? = null,
    val inCautionZone: Boolean = false,
    val lastRerouteAtMillis: Long? = null
)

data class DrivingContext(
    val vehicle: VehicleSignals,
    val navi: NaviSignals,
    val nowMillis: Long
)
