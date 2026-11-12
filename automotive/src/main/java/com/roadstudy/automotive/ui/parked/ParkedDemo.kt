package com.roadstudy.automotive.ui.parked

import com.roadstudy.automotive.session.DrivingContext
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.ProgressTracker
import com.roadstudy.automotive.session.SessionController
import com.roadstudy.automotive.session.SessionPlan
import com.roadstudy.automotive.session.SessionPlanner
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.ui.driving.SilentAudioPort
import com.roadstudy.automotive.ui.home.HomeInput

/** 데모 세션을 마치고 주차한 시점의 결과 */
class ParkedStart(
    val input: ParkedInput,
    /** 세션을 진행하며 갱신된 진도. 화면에서 고른 선택도 여기에 기록한다 */
    val tracker: ProgressTracker
)

/**
 * 실제 주행 없이 주차 후 복습 화면을 확인하기 위한 데모.
 * 가짜 신호로 계획한 개념을 모두 듣는 세션을 진짜 [SessionController] 로 끝까지 돌린 뒤 주차한다.
 * TODO: Pleos SDK 연동과 저장소(Room) 후에는 실제 주행 결과와 저장된 진도를 쓴다.
 */
class ParkedDemo(
    private val input: HomeInput,
    sectionId: String?,
    planner: SessionPlanner = SessionPlanner()
) {
    private val section = input.sections.firstOrNull { it.sectionId == sectionId }
        ?: input.sections.firstOrNull()

    private val plan: SessionPlan = if (section != null && input.route != null) {
        planner.plan(section, input.statuses, input.reviewQueue, input.route.etaSec, input.tripType)
    } else {
        SessionPlan(emptyList(), 0, emptyList())
    }

    fun start(): ParkedStart {
        val tracker = ProgressTracker(input.statuses, input.reviewQueue)
        val controller = SessionController(plan, tracker, SilentAudioPort)
        val navi = NaviSignals(
            remainingSec = input.route?.etaSec,
            remainingMeters = input.route?.remainingMeters
        )
        fun context(gear: Gear, nowMillis: Long) = DrivingContext(VehicleSignals(gear = gear), navi, nowMillis)

        var now = 0L
        controller.onContextChanged(context(Gear.DRIVE, now))
        repeat(plan.items.size) {
            // 한 개념: 설명 끝 -> 질문 끝 -> 떠올리기 시간 경과 -> 정답 끝
            now += STEP_MILLIS
            controller.onAudioFinished(now)
            now += STEP_MILLIS
            controller.onAudioFinished(now)
            now += SessionController.THINK_MILLIS
            controller.tick(now)
            now += STEP_MILLIS
            controller.onAudioFinished(now)
        }
        controller.onContextChanged(context(Gear.PARK, now + STEP_MILLIS))

        val parkedInput = ParkedInput(
            tripType = input.tripType,
            tripMinutes = ParkedUiMapper.tripMinutes(input.route?.etaSec),
            conceptTotal = plan.items.size,
            sectionTitle = section?.title.orEmpty(),
            fileName = section?.sourceFileName,
            sectionConceptIds = section?.concepts?.map { it.id }?.toSet().orEmpty(),
            heardConcepts = plan.items.map { it.concept },
            statuses = tracker.statuses(),
            reviewQueue = tracker.reviewQueue(),
            choices = emptyMap(),
            streakDays = DEMO_STREAK_DAYS
        )
        return ParkedStart(parkedInput, tracker)
    }

    private companion object {
        const val STEP_MILLIS = 10_000L

        /** 연속 학습일 저장소가 없어서 쓰는 임시 값 */
        const val DEMO_STREAK_DAYS = 12
    }
}
