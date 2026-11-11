package com.roadstudy.automotive.session

import com.roadstudy.automotive.session.TestFixtures.concept
import com.roadstudy.automotive.session.TestFixtures.driving
import com.roadstudy.shared.model.ConceptStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionControllerTest {

    private val plan = SessionPlan(
        items = listOf(
            PlanItem(concept("n1"), PlanItemKind.NEW),
            PlanItem(concept("r1"), PlanItemKind.REVIEW)
        ),
        estimatedLearningSec = 300,
        parkOnlyConcepts = emptyList()
    )

    @Test fun fullFlowExplainQuestionThinkAnswer() {
        val audio = FakeAudio()
        val controller = SessionController(plan, ProgressTracker(), audio)

        controller.onContextChanged(driving(now = 0))
        controller.onAudioFinished(1_000)
        controller.onAudioFinished(2_000)
        controller.tick(3_000)
        controller.tick(6_100)

        assertEquals(
            listOf("play:n1/explain.mp3", "play:n1/question.mp3", "play:n1/answer.mp3"),
            audio.calls
        )
    }

    @Test fun upcomingTurnPausesAndResumes() {
        val audio = FakeAudio()
        val controller = SessionController(plan, ProgressTracker(), audio)

        controller.onContextChanged(driving(now = 0))
        controller.onContextChanged(driving(turnMeters = 200, now = 500))
        assertEquals(LearningMode.FOCUS, controller.uiState.mode)
        controller.onContextChanged(driving(turnMeters = 1_500, now = 9_000))

        assertEquals(listOf("play:n1/explain.mp3", "pause", "resume"), audio.calls)
    }

    @Test fun thinkTimeRestartsAfterFocus() {
        val audio = FakeAudio()
        val controller = SessionController(plan, ProgressTracker(), audio)

        controller.onContextChanged(driving(now = 0))
        controller.onAudioFinished(1_000)
        controller.onAudioFinished(2_000)
        controller.onContextChanged(driving(caution = true, now = 3_000))
        controller.tick(7_000)
        controller.onContextChanged(driving(now = 20_000))
        controller.tick(22_000)
        assertTrue(audio.calls.none { it.endsWith("answer.mp3") })
        controller.tick(24_100)
        assertTrue(audio.calls.last().endsWith("n1/answer.mp3"))
    }

    @Test fun cautionModeSkipsNewConceptsAndPlaysReviewExplanationOnly() {
        val audio = FakeAudio()
        val controller = SessionController(plan, ProgressTracker(), audio)

        controller.onContextChanged(driving(lowBeam = true, now = 0))
        assertEquals(listOf("play:r1/explain.mp3"), audio.calls)
        controller.onAudioFinished(1_000)
        assertTrue(controller.uiState.finished)
    }

    @Test fun steeringPreviousQueuesConceptForNextSession() {
        val progress = ProgressTracker()
        val controller = SessionController(plan, progress, FakeAudio())

        controller.onContextChanged(driving(now = 0))
        controller.onAudioFinished(1_000)
        controller.onSteeringPrevious()

        assertEquals(ConceptStatus.NEEDS_REVIEW, progress.statuses()["n1"])
        assertEquals(listOf("n1"), progress.reviewQueue())
    }

    @Test fun steeringIgnoredWhileFocused() {
        val progress = ProgressTracker()
        val controller = SessionController(plan, progress, FakeAudio())

        controller.onContextChanged(driving(now = 0))
        controller.onAudioFinished(1_000)
        controller.onContextChanged(driving(turnMeters = 100, now = 1_500))
        controller.onSteeringNext()

        assertTrue(progress.reviewQueue().isEmpty())
        assertEquals(ConceptStatus.LEARNED, progress.statuses()["n1"])
    }

    @Test fun parkingEndsSession() {
        val audio = FakeAudio()
        val controller = SessionController(plan, ProgressTracker(), audio)

        controller.onContextChanged(driving(now = 0))
        controller.onContextChanged(driving(gear = Gear.PARK, now = 5_000))

        assertTrue(controller.uiState.finished)
        assertEquals("stop", audio.calls.last())
    }
}
