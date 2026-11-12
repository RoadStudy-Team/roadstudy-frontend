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

    /**
     * 실제 차량 신호([vehicle])로 만든 세션 상태를 돌려준다. 내비 값(남은 거리, 시간)은 아직 가짜다.
     * 처음에는 안전한 주행으로 첫 개념 설명을 시작한 뒤 받은 신호를 적용한다.
     *
     * [hasDriven] 이 false 면(시작 버튼으로 들어왔지만 아직 D단이 된 적 없음) P단을 도착으로 보지 않고
     * 주행 직전의 안전 구간으로 보여 준다. 그래야 들어오자마자 복습 화면으로 넘어가지 않는다.
     * TODO: 신호가 바뀔 때마다 같은 SessionController 에 전달하도록 바꾼다 (지금은 매번 처음부터 계산한다).
     */
    fun runLive(vehicle: VehicleSignals, hasDriven: Boolean): SessionUiState {
        val effective = if (!hasDriven && vehicle.gear == Gear.PARK) vehicle.copy(gear = Gear.DRIVE) else vehicle
        val controller = newController()
        controller.onContextChanged(context(VehicleSignals(gear = Gear.DRIVE)))
        controller.onContextChanged(context(effective))
        return controller.uiState
    }

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

    private fun context(vehicle: VehicleSignals, turnMeters: Int? = null) = DrivingContext(
        vehicle = vehicle,
        navi = NaviSignals(
            remainingSec = input.route?.etaSec,
            remainingMeters = input.route?.remainingMeters,
            distanceToNextTurnMeters = turnMeters
        ),
        nowMillis = NOW_MILLIS
    )

    private companion object {
        const val NOW_MILLIS = 0L
    }
}
