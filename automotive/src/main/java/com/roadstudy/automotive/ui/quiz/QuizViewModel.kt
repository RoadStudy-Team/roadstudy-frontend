package com.roadstudy.automotive.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/** 확인 퀴즈 화면의 상태 보관소. 푼 결과는 [QuizSession] 이 채점한다 */
class QuizViewModel(
    private val session: QuizSession,
    private val sectionTitle: String,
    private val mapper: QuizUiMapper = QuizUiMapper()
) : ViewModel() {

    private val _uiState = MutableLiveData(mapper.map(session, sectionTitle))

    /** 지금 푸는 문제. 문제가 하나도 없으면 null */
    val uiState: LiveData<QuizUiState?> = _uiState

    val hasQuestions: Boolean get() = session.total > 0

    fun select(choice: Int) {
        session.select(choice)
        publish()
    }

    /** `다음 문제` 또는 `결과 보기`. 모든 문제를 풀었으면 결과를 돌려주고, 아니면 null */
    fun next(): QuizResult? {
        session.next()
        return afterMove()
    }

    /** `건너뛰기`. 모든 문제를 지났으면 결과를 돌려주고, 아니면 null */
    fun skip(): QuizResult? {
        session.skip()
        return afterMove()
    }

    private fun afterMove(): QuizResult? {
        if (session.finished) return session.result()
        publish()
        return null
    }

    private fun publish() {
        _uiState.value = mapper.map(session, sectionTitle)
    }
}
