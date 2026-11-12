package com.roadstudy.automotive.session

import com.roadstudy.automotive.session.TestFixtures.driving
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LearningModeDeciderTest {

    private val decider = LearningModeDecider()

    @Test fun parkedWhenGearIsPark() =
        assertEquals(LearningMode.PARKED, decider.decide(driving(gear = Gear.PARK)).mode)

    @Test fun normalOnClearRoad() =
        assertEquals(ModeDecision(LearningMode.NORMAL, ModeReason.SAFE), decider.decide(driving()))

    @Test fun focusBeforeTurn() =
        assertEquals(ModeReason.UPCOMING_TURN, decider.decide(driving(turnMeters = 250)).reason)

    @Test fun focusInCautionZone() =
        assertEquals(LearningMode.FOCUS, decider.decide(driving(caution = true)).mode)

    @Test fun focusShortlyAfterReroute() {
        assertEquals(LearningMode.FOCUS, decider.decide(driving(rerouteAt = 0, now = 10_000)).mode)
        assertEquals(LearningMode.NORMAL, decider.decide(driving(rerouteAt = 0, now = 40_000)).mode)
    }

    @Test fun focusInHeavyRainOrFog() {
        assertEquals(LearningMode.FOCUS, decider.decide(driving(wiper = WiperLevel.HIGH)).mode)
        assertEquals(LearningMode.FOCUS, decider.decide(driving(fog = true)).mode)
    }

    @Test fun cautionAtNightOrLightRain() {
        assertEquals(ModeReason.NIGHT, decider.decide(driving(lowBeam = true)).reason)
        assertEquals(ModeReason.LIGHT_RAIN, decider.decide(driving(wiper = WiperLevel.LOW)).reason)
    }

    @Test fun turnBeatsNight() =
        assertEquals(LearningMode.FOCUS, decider.decide(driving(lowBeam = true, turnMeters = 100)).mode)

    @Test fun rerouteQuietRemainingCountsDownToCooldownEnd() {
        val cooldown = LearningModeDecider.REROUTE_COOLDOWN_MILLIS
        assertEquals(cooldown, LearningModeDecider.rerouteQuietRemainingMillis(1_000L, 1_000L))
        assertEquals(cooldown - 10_000L, LearningModeDecider.rerouteQuietRemainingMillis(1_000L, 11_000L))
    }

    @Test fun rerouteQuietRemainingIsNullWhenNoRerouteOrAlreadyOver() {
        val cooldown = LearningModeDecider.REROUTE_COOLDOWN_MILLIS
        assertNull(LearningModeDecider.rerouteQuietRemainingMillis(null, 5_000L))
        assertNull(LearningModeDecider.rerouteQuietRemainingMillis(0L, cooldown))
        assertNull(LearningModeDecider.rerouteQuietRemainingMillis(0L, cooldown + 1))
    }
}
