package com.roadstudy.automotive.session

import com.roadstudy.shared.model.Concept
import com.roadstudy.shared.model.MaterialType
import com.roadstudy.shared.model.StudySection

object TestFixtures {

    fun concept(id: String, durationSec: Int = 120, driveSafe: Boolean = true) = Concept(
        id = id,
        title = "개념 $id",
        explanation = "설명 $id",
        explanationAudioUrl = "$id/explain.mp3",
        questionAudioUrl = "$id/question.mp3",
        answerAudioUrl = "$id/answer.mp3",
        durationSec = durationSec,
        question = "질문 $id",
        answer = "정답 $id",
        driveSafe = driveSafe
    )

    val dbSection = StudySection(
        sectionId = "db-normalization",
        title = "DB 정규화 정리",
        type = MaterialType.CERTIFICATE,
        concepts = listOf(
            concept("c01"), concept("c02"), concept("c03"), concept("c04"),
            concept("c05"), concept("c06"), concept("c07"),
            concept("c08", driveSafe = false)
        )
    )

    fun driving(
        gear: Gear = Gear.DRIVE,
        wiper: WiperLevel = WiperLevel.OFF,
        lowBeam: Boolean = false,
        fog: Boolean = false,
        turnMeters: Int? = 2_000,
        caution: Boolean = false,
        rerouteAt: Long? = null,
        now: Long = 0L
    ) = DrivingContext(
        vehicle = VehicleSignals(gear, wiper, lowBeam, fog),
        navi = NaviSignals(
            remainingSec = 42 * 60,
            remainingMeters = 18_600,
            distanceToNextTurnMeters = turnMeters,
            inCautionZone = caution,
            lastRerouteAtMillis = rerouteAt
        ),
        nowMillis = now
    )
}

class FakeAudio : AudioPort {
    val calls = mutableListOf<String>()
    override fun play(url: String) { calls += "play:$url" }
    override fun pause() { calls += "pause" }
    override fun resume() { calls += "resume" }
    override fun stop() { calls += "stop" }
}
