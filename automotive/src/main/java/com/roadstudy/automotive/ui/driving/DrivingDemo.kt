package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.DrivingContext
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.ProgressTracker
import com.roadstudy.automotive.session.SessionController
import com.roadstudy.automotive.session.SessionPlan
import com.roadstudy.automotive.session.SessionPlanner
import com.roadstudy.automotive.session.SessionUiState
import com.roadstudy.automotive.session.VehicleSignals
import com.roadstudy.automotive.ui.home.HomeInput

/** 데모에서 재현할 주행 상황. 도안 04~07 에 하나씩 대응한다 */
enum class DrivingScenario {
    /** 04: 안전 구간에서 설명을 듣는 중 */
    SAFE,

    /** 05: 질문을 듣고 떠올리는 중 */
    THINKING,

    /** 06: 야간이라 가벼운 복습만 */
    NIGHT,

    /** 07: 회전 300m 전이라 일시정지 */
    TURN_AHEAD,

    /** 도착해서 P단으로 바뀐 상황. 주차 후 복습 화면(08)으로 넘어간다 */
    ARRIVED;

    fun next(): DrivingScenario = entries[(ordinal + 1) % entries.size]

    companion object {
        /** 실행 인자(--es driving_scenario night)로 받은 이름. 없거나 틀리면 SAFE */
        fun fromKey(key: String?): DrivingScenario =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: SAFE
    }
}

/**
 * 주행 세션의 학습 계획을 세우고, 개발용 가짜 주행 상황([run])을 돌려 보는 곳.
 * 실제 신호로 진행하는 세션은 [com.roadstudy.automotive.playback.LearningSession] 이 이 계획으로 진행한다.
 */
class DrivingDemo(
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

    val destinationName: String get() = input.destinationName
    val sectionTitle: String get() = section?.title.orEmpty()

    /** 실제 신호로 진행하는 세션([com.roadstudy.automotive.playback.LearningSession])에 넘길 학습 계획과 진도 */
    val sessionPlan: SessionPlan get() = plan

    fun newProgress() = ProgressTracker(input.statuses, input.reviewQueue)

    /** 내비 안내가 없을 때 쓰는 남은 거리(m)·시간(초): 홈에서 본 경로 */
    val fallbackMeters: Int? get() = input.route?.remainingMeters
    val fallbackSec: Int? get() = input.route?.etaSec

    /** 처음부터 시작해서 [scenario] 상황이 된 시점의 세션 상태를 돌려준다 */
    fun run(scenario: DrivingScenario): SessionUiState {
        val controller = newController()

        // 먼저 안전한 주행으로 첫 개념 설명을 시작한 뒤, 상황을 바꾼다
        controller.onContextChanged(context(VehicleSignals(gear = Gear.DRIVE)))
        when (scenario) {
            DrivingScenario.SAFE -> Unit
            DrivingScenario.THINKING -> {
                controller.onAudioFinished(NOW_MILLIS) // 설명 끝 -> 질문
                controller.onAudioFinished(NOW_MILLIS) // 질문 끝 -> 떠올리기
            }
            DrivingScenario.NIGHT ->
                controller.onContextChanged(context(VehicleSignals(gear = Gear.DRIVE, lowBeamOn = true)))
            DrivingScenario.TURN_AHEAD ->
                controller.onContextChanged(context(VehicleSignals(gear = Gear.DRIVE), turnMeters = 300))
            DrivingScenario.ARRIVED ->
                controller.onContextChanged(context(VehicleSignals(gear = Gear.PARK)))
        }
        return controller.uiState
    }

    private fun newController() = SessionController(
        plan = plan,
        progress = ProgressTracker(input.statuses, input.reviewQueue),
        audio = SilentAudioPort
    )

    private fun context(vehicle: VehicleSignals, turnMeters: Int? = null) = context(
        vehicle,
        NaviSignals(
            remainingSec = input.route?.etaSec,
            remainingMeters = input.route?.remainingMeters,
            distanceToNextTurnMeters = turnMeters
        ),
        NOW_MILLIS
    )

    private fun context(vehicle: VehicleSignals, navi: NaviSignals, nowMillis: Long) =
        DrivingContext(vehicle = vehicle, navi = navi, nowMillis = nowMillis)

    private companion object {
        const val NOW_MILLIS = 0L
    }
}
