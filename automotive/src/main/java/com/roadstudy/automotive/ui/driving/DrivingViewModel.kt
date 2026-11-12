package com.roadstudy.automotive.ui.driving

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/** 주행 중 화면의 상태 보관소. 지금은 [DrivingDemo] 의 가짜 상황을 보여준다 */
class DrivingViewModel(
    private val demo: DrivingDemo,
    initialScenario: DrivingScenario,
    private val mapper: DrivingUiMapper = DrivingUiMapper()
) : ViewModel() {

    private var scenario = initialScenario

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<DrivingUiState> = _uiState

    /** 개발용: 다음 주행 상황으로 바꿔 본다 */
    fun nextScenario() {
        scenario = scenario.next()
        _uiState.value = buildState()
    }

    private fun buildState(): DrivingUiState =
        mapper.map(demo.run(scenario), demo.destinationName, demo.sectionTitle)
}
