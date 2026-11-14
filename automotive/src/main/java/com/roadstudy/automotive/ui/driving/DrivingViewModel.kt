package com.roadstudy.automotive.ui.driving

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.roadstudy.automotive.session.Gear
import com.roadstudy.automotive.session.LearningModeDecider
import com.roadstudy.automotive.session.NaviSignals
import com.roadstudy.automotive.session.VehicleSignals

/**
 * 주행 중 화면의 상태 보관소.
 * 기본은 실제 차량 신호([onVehicleSignals])를 따르고, 개발용으로 화면을 누르면 [DrivingDemo] 의 가짜 상황을 돌려 본다.
 */
class DrivingViewModel(
    private val demo: DrivingDemo,
    initialScenario: DrivingScenario?,
    /** 이 화면이 만들어진 시점의 차량 신호 */
    initialSignals: VehicleSignals = VehicleSignals(),
    initialNavi: NaviSignals = NaviSignals(),
    private val mapper: DrivingUiMapper = DrivingUiMapper()
) : ViewModel() {

    /** null 이면 실제 차량 신호를 따른다 (라이브 모드) */
    private var scenario: DrivingScenario? = initialScenario
    private var liveSignals = initialSignals
    private var liveNavi = initialNavi
    private var hasDriven = initialSignals.gear == Gear.DRIVE
    private var arrivalHandled = false

    private val handler = Handler(Looper.getMainLooper())
    private val refreshAfterReroute = Runnable { _uiState.value = buildState() }

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<DrivingUiState> = _uiState

    init {
        scheduleRerouteRefresh()
    }

    /** 내비 신호가 바뀌었을 때. 데모 상황을 보고 있을 때는 무시한다 */
    fun onNaviSignals(signals: NaviSignals) {
        if (scenario != null || signals == liveNavi) return
        liveNavi = signals
        _uiState.value = buildState()
        scheduleRerouteRefresh()
    }

    /**
     * 재탐색 직후에는 집중 모드로 학습을 멈추는데, 화면은 새 신호가 올 때만 다시 계산한다.
     * 신호가 오지 않아도 집중 모드가 끝나는 시점에 한 번 더 계산해서 학습이 다시 이어지게 한다.
     */
    private fun scheduleRerouteRefresh() {
        handler.removeCallbacks(refreshAfterReroute)
        if (scenario != null || !liveNavi.hasRoute) return
        val remaining = LearningModeDecider.rerouteQuietRemainingMillis(
            liveNavi.lastRerouteAtMillis, System.currentTimeMillis()
        ) ?: return
        handler.postDelayed(refreshAfterReroute, remaining + REFRESH_MARGIN_MILLIS)
    }

    override fun onCleared() {
        handler.removeCallbacks(refreshAfterReroute)
    }

    /** 실제 차량 신호가 바뀌었을 때. 데모 상황을 보고 있을 때는 무시한다 */
    fun onVehicleSignals(signals: VehicleSignals) {
        if (scenario != null || signals == liveSignals) return
        liveSignals = signals
        // 도착 처리는 다시 주행(D)을 시작했을 때만 되살린다. 조명·와이퍼가 바뀐 것만으로는 복습 화면으로 다시 넘어가지 않는다
        if (signals.gear == Gear.DRIVE) {
            hasDriven = true
            arrivalHandled = false
        }
        _uiState.value = buildState()
    }

    /** 개발용: 다음 주행 상황으로 바꿔 본다. 라이브 모드에서는 첫 상황(안전)부터 시작한다 */
    fun nextScenario() {
        scenario = scenario?.next() ?: DrivingScenario.SAFE
        arrivalHandled = false
        _uiState.value = buildState()
        scheduleRerouteRefresh()
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
        val session = if (current != null) demo.run(current) else demo.runLive(liveSignals, liveNavi, hasDriven)
        val destination = if (current == null) liveNavi.destinationName else null
        return mapper.map(session, destination ?: demo.destinationName, demo.sectionTitle)
    }

    private companion object {
        /** 집중 모드 종료 시각을 막 지난 뒤에 계산하도록 두는 여유 */
        const val REFRESH_MARGIN_MILLIS = 200L
    }
}
