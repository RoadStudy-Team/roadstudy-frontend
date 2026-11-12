package com.roadstudy.automotive.ui.parked

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.roadstudy.automotive.session.ProgressTracker
import com.roadstudy.automotive.session.SelfCheck

/** 주차 후 복습 화면의 상태 보관소. 개념별 선택을 진도([ProgressTracker])에 기록한다 */
class ParkedViewModel(
    start: ParkedStart,
    private val mapper: ParkedUiMapper = ParkedUiMapper()
) : ViewModel() {

    private val tracker: ProgressTracker = start.tracker
    private var input: ParkedInput = start.input

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<ParkedUiState> = _uiState

    /**
     * 개념 카드에서 `다시 들을래요`(RELISTEN) 또는 `이제 알아요`(KNOW)를 골랐을 때.
     * 진도와 다시 듣기 목록이 바뀌므로 위쪽 집계도 함께 바뀐다.
     */
    fun choose(conceptId: String, check: SelfCheck) {
        tracker.record(conceptId, check)
        input = input.copy(
            statuses = tracker.statuses(),
            reviewQueue = tracker.reviewQueue(),
            choices = input.choices + (conceptId to check)
        )
        _uiState.value = buildState()
    }

    private fun buildState(): ParkedUiState = mapper.map(input)
}
