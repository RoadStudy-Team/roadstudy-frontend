package com.roadstudy.automotive.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.roadstudy.automotive.session.CommuteInfo
import com.roadstudy.automotive.session.CommuteTarget
import com.roadstudy.automotive.session.NaviSignals
import java.time.LocalTime

/**
 * 홈 화면의 상태 보관소. [HomeDataSource] 에서 받은 [HomeInput] 을 [HomeUiMapper] 로 가공해 [uiState] 로 내보낸다.
 * 도착 예정 시각 계산에 쓰는 현재 시각([clock])은 테스트에서 바꿔 끼울 수 있다.
 */
class HomeViewModel(
    private val dataSource: HomeDataSource,
    private val mapper: HomeUiMapper = HomeUiMapper(),
    private val clock: () -> LocalTime = { LocalTime.now() },
    initialNavi: NaviSignals = NaviSignals(),
    initialCommute: CommuteInfo = CommuteInfo()
) : ViewModel() {

    private var input: HomeInput = dataSource.load()
    private var navi: NaviSignals = initialNavi
    private var commute: CommuteInfo = initialCommute

    /** 사용자가 경로 카드에서 고른 회사/집. null 이면 시간대로 고른다. 앱을 다시 켜면 처음으로 돌아간다 */
    private var chosenTarget: CommuteTarget? = null

    private val _uiState = MutableLiveData(buildState())
    val uiState: LiveData<HomeUiState> = _uiState

    /** 내비 경로가 바뀌었을 때. 안내 중이면 오늘의 경로와 학습 분량이 그 경로로 바뀐다 */
    fun onNaviSignals(signals: NaviSignals) {
        if (signals == navi) return
        navi = signals
        _uiState.value = buildState()
    }

    /** 내비 즐겨찾기나 경로 미리보기가 바뀌었을 때. 안내 중이 아니면 오늘의 경로가 미리보기로 바뀐다 */
    fun onCommute(info: CommuteInfo) {
        if (info == commute) return
        commute = info
        _uiState.value = buildState()
    }

    /** 경로 카드의 회사/집 버튼을 눌렀을 때. 오늘의 경로·학습 분량·시작 버튼이 그쪽 기준으로 바뀐다 */
    fun selectCommute(target: CommuteTarget) {
        if (target == chosenTarget) return
        chosenTarget = target
        _uiState.value = buildState()
    }

    fun selectSection(sectionId: String) {
        input = input.copy(selectedSectionId = sectionId)
        _uiState.value = buildState()
    }

    /** 빈 상태에서 "샘플 자료로 체험하기"를 눌렀을 때 */
    fun startSampleExperience() {
        input = dataSource.sample()
        _uiState.value = buildState()
    }

    private fun buildState(): HomeUiState {
        val now = clock()
        return mapper.map(input.withRoute(navi, commute, chosenTarget, now), now)
            .copy(commuteToggle = commuteToggle(navi, commute, chosenTarget, now))
    }
}
