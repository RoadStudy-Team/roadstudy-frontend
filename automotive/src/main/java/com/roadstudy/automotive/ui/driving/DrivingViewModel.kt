package com.roadstudy.automotive.ui.driving

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.SessionUiState
import com.roadstudy.automotive.session.VehicleSignals

/**
 * 주행 중 화면의 상태 보관소.
 * 기본은 실제 신호로 진행 중인 학습 세션([com.roadstudy.automotive.playback.LearningHub])의 상태([onSessionState])를 그리고,
 * 개발용으로 화면을 누르면 [DrivingDemo] 의 가짜 상황을 돌려 본다.
 */
class DrivingViewModel(
    private val demo: DrivingDemo,
    initialScenario: DrivingScenario?,
    /** 이 화면이 만들어진 시점의 세션 상태 */
    initialSession: SessionUiState?,
    initialNavi: NaviSignals = NaviSignals(),
    private val mapper: DrivingUiMapper = DrivingUiMapper()
) : ViewModel() {

    /** null 이면 실제 신호로 진행하는 세션을 따른다 (라이브 모드) */
    private var scenario: DrivingScenario? = initialScenario

    /** 세션이 끝나 허브가 비워져도 마지막 화면을 유지하기 위해 기억한다 */
    private var liveSession: SessionUiState? = initialSession
    private var liveNavi = initialNavi
    private var arrivalHandled = false

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<DrivingUiState> = _uiState

    /** 라이브 모드인지 (개발용 가짜 상황을 보고 있지 않은지) */
    val isLive: Boolean get() = scenario == null

    /** 세션 상태가 바뀌었을 때. 데모 상황을 보고 있을 때는 무시한다 */
    fun onSessionState(state: SessionUiState?) {
        if (scenario != null || state == null || state == liveSession) return
        liveSession = state
        _uiState.value = buildState()
    }

    /** 내비 신호가 바뀌었을 때. 목적지 이름을 내비 값으로 보여 주는 데 쓴다 */
    fun onNaviSignals(signals: NaviSignals) {
        if (scenario != null || signals == liveNavi) return
        liveNavi = signals
        _uiState.value = buildState()
    }

    /** 실제 차량 신호가 바뀌었을 때. 도착 처리는 다시 주행(D)을 시작했을 때만 되살린다 */
    fun onVehicleSignals(signals: VehicleSignals) {
        if (scenario != null) return
        if (signals.gear == Gear.DRIVE) arrivalHandled = false
    }

    /** 개발용: 다음 주행 상황으로 바꿔 본다. 라이브 모드에서는 첫 상황(안전)부터 시작한다 */
    fun nextScenario() {
        scenario = scenario?.next() ?: DrivingScenario.SAFE
        arrivalHandled = false
        _uiState.value = buildState()
    }

    /**
     * 도착 상태에서 주차 후 복습 화면으로 넘어갈 때 한 번만 true 를 돌려준다.
     * 복습 화면에서 뒤로 돌아왔을 때 다시 넘어가는 일을 막는다.
     */
    fun consumeArrival(): Boolean {
        if (arrivalHandled) return false
        arrivalHandled = true
        return true
    }

    private fun buildState(): DrivingUiState {
        val current = scenario
        val session = if (current != null) demo.run(current) else liveSession ?: demo.run(DrivingScenario.SAFE)
        val destination = if (current == null) liveNavi.destinationName else null
        return mapper.map(session, destination ?: demo.destinationName, demo.sectionTitle)
    }
}
