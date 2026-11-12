package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.AudioPort
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
    TURN_AHEAD;

    fun next(): DrivingScenario = entries[(ordinal + 1) % entries.size]

    companion object {
        /** 실행 인자(--es driving_scenario night)로 받은 이름. 없거나 틀리면 SAFE */
        fun fromKey(key: String?): DrivingScenario =
            entries.firstOrNull { it.name.equals(key, ignoreCase = true) } ?: SAFE
    }
}

/**
 * 실제 차량·내비 신호 없이 주행 중 화면을 확인하기 위한 데모 러너.
 * 가짜 신호를 진짜 [SessionController] 에 넣어서, 실제와 같은 상태가 나오는지 본다.
 * TODO: Pleos SDK 연동 후에는 이 클래스 대신 실제 신호를 SessionController 에 전달한다.
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

    /** 처음부터 시작해서 [scenario] 상황이 된 시점의 세션 상태를 돌려준다 */
    fun run(scenario: DrivingScenario): SessionUiState {
        val controller = SessionController(
            plan = plan,
            progress = ProgressTracker(input.statuses, input.reviewQueue),
            audio = SilentAudioPort
        )
        val route = input.route
        fun context(vehicle: VehicleSignals, turnMeters: Int? = null) = DrivingContext(
            vehicle = vehicle,
            navi = NaviSignals(
                remainingSec = route?.etaSec,
                remainingMeters = route?.remainingMeters,
                distanceToNextTurnMeters = turnMeters
            ),
            nowMillis = NOW_MILLIS
        )

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
        }
        return controller.uiState
    }

    /** 소리를 내지 않는 오디오. 데모에서는 화면만 확인한다 */
    private object SilentAudioPort : AudioPort {
        override fun play(url: String) = Unit
        override fun pause() = Unit
        override fun resume() = Unit
        override fun stop() = Unit
    }

    private companion object {
        const val NOW_MILLIS = 0L
    }
}
