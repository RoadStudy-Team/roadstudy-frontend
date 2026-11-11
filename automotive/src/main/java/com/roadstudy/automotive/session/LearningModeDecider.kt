package com.roadstudy.automotive.session

/**
 * 지금 학습을 어떻게 진행할지 정한다.
 * - PARKED : 주차(P단). 정리 화면·확인 퀴즈 표시
 * - FOCUS  : 운전에 집중할 순간. 학습 완전히 멈춤
 * - CAUTION: 야간·약한 비. 새 개념 없이 가벼운 복습만
 * - NORMAL : 안전 구간. 정상 학습
 */
enum class LearningMode { PARKED, FOCUS, CAUTION, NORMAL }

enum class ModeReason {
    PARKED,
    UPCOMING_TURN,
    CAUTION_ZONE,
    RECENT_REROUTE,
    HEAVY_RAIN,
    FOG,
    NIGHT,
    LIGHT_RAIN,
    SAFE
}

data class ModeDecision(val mode: LearningMode, val reason: ModeReason)

class LearningModeDecider(
    private val turnQuietDistanceMeters: Int = TURN_QUIET_DISTANCE_METERS,
    private val rerouteCooldownMillis: Long = REROUTE_COOLDOWN_MILLIS
) {

    fun decide(context: DrivingContext): ModeDecision {
        val vehicle = context.vehicle
        val navi = context.navi

        if (vehicle.gear == Gear.PARK) return ModeDecision(LearningMode.PARKED, ModeReason.PARKED)

        // 집중: 운전이 바쁜 순간에는 학습을 멈춘다 (우선순위 높은 순서)
        val turnDistance = navi.distanceToNextTurnMeters
        if (turnDistance != null && turnDistance <= turnQuietDistanceMeters) {
            return ModeDecision(LearningMode.FOCUS, ModeReason.UPCOMING_TURN)
        }
        if (navi.inCautionZone) return ModeDecision(LearningMode.FOCUS, ModeReason.CAUTION_ZONE)
        val reroutedAt = navi.lastRerouteAtMillis
        if (reroutedAt != null && context.nowMillis - reroutedAt < rerouteCooldownMillis) {
            return ModeDecision(LearningMode.FOCUS, ModeReason.RECENT_REROUTE)
        }
        if (vehicle.wiperLevel == WiperLevel.HIGH) return ModeDecision(LearningMode.FOCUS, ModeReason.HEAVY_RAIN)
        if (vehicle.fogLightOn) return ModeDecision(LearningMode.FOCUS, ModeReason.FOG)

        // 주의: 야간·약한 비에는 가벼운 복습만
        if (vehicle.wiperLevel == WiperLevel.LOW) return ModeDecision(LearningMode.CAUTION, ModeReason.LIGHT_RAIN)
        if (vehicle.lowBeamOn) return ModeDecision(LearningMode.CAUTION, ModeReason.NIGHT)

        return ModeDecision(LearningMode.NORMAL, ModeReason.SAFE)
    }

    companion object {
        const val TURN_QUIET_DISTANCE_METERS = 300
        const val REROUTE_COOLDOWN_MILLIS = 30_000L
    }
}
