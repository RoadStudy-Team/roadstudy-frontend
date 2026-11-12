package com.roadstudy.automotive.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalTime

/**
 * 홈 화면의 상태 보관소. [HomeDataSource] 에서 받은 [HomeInput] 을 [HomeUiMapper] 로 가공해 [uiState] 로 내보낸다.
 * 도착 예정 시각 계산에 쓰는 현재 시각([clock])은 테스트에서 바꿔 끼울 수 있다.
 */
class HomeViewModel(
    private val dataSource: HomeDataSource,
    private val mapper: HomeUiMapper = HomeUiMapper(),
    private val clock: () -> LocalTime = { LocalTime.now() }
) : ViewModel() {

    private var input: HomeInput = dataSource.load()

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<HomeUiState> = _uiState

    fun selectSection(sectionId: String) {
        input = input.copy(selectedSectionId = sectionId)
        _uiState.value = buildState()
    }

    /** 빈 상태에서 "샘플 자료로 체험하기"를 눌렀을 때 */
    fun startSampleExperience() {
        input = dataSource.sample()
        _uiState.value = buildState()
    }

    private fun buildState(): HomeUiState = mapper.map(input, clock())
}
