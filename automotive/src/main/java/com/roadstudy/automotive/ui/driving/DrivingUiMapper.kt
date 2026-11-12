package com.roadstudy.automotive.ui.driving

import com.roadstudy.automotive.session.LearningMode
import com.roadstudy.automotive.session.SessionUiState
import com.roadstudy.automotive.session.StatusText
import com.roadstudy.automotive.session.Step
import com.roadstudy.automotive.ui.home.HomeUiMapper

/**
 * 세션 진행기의 상태([SessionUiState])를 주행 중 화면용 값([DrivingUiState])으로 바꾼다.
 * 모드별 화면: NORMAL 파형 / 떠올리기 흐린 파형 / CAUTION 아이콘+파형 / FOCUS 일시정지.
 */
class DrivingUiMapper {

    fun map(session: SessionUiState, destinationName: String, sectionTitle: String): DrivingUiState {
        val tone = when (session.mode) {
            LearningMode.CAUTION -> DrivingTone.CAUTION
            LearningMode.FOCUS -> DrivingTone.FOCUS
            LearningMode.NORMAL, LearningMode.PARKED -> DrivingTone.SAFE
        }
        val visual = when (session.mode) {
            LearningMode.FOCUS -> DrivingVisual.PAUSED
            LearningMode.CAUTION -> DrivingVisual.WAVE_WITH_ICON
            LearningMode.PARKED -> DrivingVisual.WAVE_QUIET
            LearningMode.NORMAL ->
                if (session.step == Step.THINK) DrivingVisual.WAVE_QUIET else DrivingVisual.WAVE
        }
        val hint = when (session.mode) {
            LearningMode.FOCUS -> DrivingHint.AUTO_RESUME
            LearningMode.PARKED -> DrivingHint.NONE
            LearningMode.NORMAL, LearningMode.CAUTION -> DrivingHint.STEERING
        }

        return DrivingUiState(
            tone = tone,
            visual = visual,
            hint = hint,
            statusText = StatusText.of(session.reason, session.step),
            destinationName = destinationName,
            distanceKm = session.remainingMeters?.let { HomeUiMapper.formatDistanceKm(it) },
            etaMinutes = session.remainingSec?.let { HomeUiMapper.ceilMinutes(it) },
            conceptIndex = session.conceptIndex,
            conceptTotal = session.conceptTotal,
            sectionTitle = sectionTitle,
            arrived = session.mode == LearningMode.PARKED
        )
    }
}
