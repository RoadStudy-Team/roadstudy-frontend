package com.roadstudy.automotive.ui.othertrip

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.roadstudy.automotive.ui.home.HomeInput

/** 다른 이동 안내 화면의 상태 보관소. "이 장소는 다음부터 묻지 않기" 체크 상태를 들고 있다 */
class OtherTripViewModel(
    private val input: HomeInput,
    private val sectionId: String?,
    private val mapper: OtherTripUiMapper = OtherTripUiMapper()
) : ViewModel() {

    private var dontAskAgain = false

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<OtherTripUiState> = _uiState

    fun toggleDontAskAgain() {
        dontAskAgain = !dontAskAgain
        _uiState.value = buildState()
    }

    private fun buildState(): OtherTripUiState = mapper.map(input, sectionId, dontAskAgain)
}
