package com.roadstudy.automotive.ui.quiz

import com.roadstudy.shared.model.QuizQuestion

/** 퀴즈를 다 푼 결과 */
data class QuizResult(val correct: Int, val total: Int)

/**
 * 확인 퀴즈 진행과 채점. 한 번에 한 문제(한 페이지)씩 풀고 마지막에 결과를 낸다.
 *
 * - 선택지를 고르면 바로 정오답을 보여주므로, 한 번 고른 것은 바꿀 수 없다. 고른 뒤에만 다음 문제로 갈 수 있다.
 * - 건너뛰면 그 문제는 오답으로 센다. 이미 고른 문제는 건너뛸 수 없다.
 *   결과의 전체 문제 수에는 건너뛴 문제도 포함된다.
 * Android 에 의존하지 않는 순수 Kotlin 이라 단위 테스트로 검증한다.
 */
class QuizSession(val questions: List<QuizQuestion>) {

    private val picks: MutableList<Int?> = MutableList(questions.size) { null }

    /** 지금 푸는 문제의 위치 (0부터) */
    var index: Int = 0
        private set

    /** 모든 문제를 지나 결과를 낼 수 있는 상태. 문제가 없으면 처음부터 true */
    var finished: Boolean = questions.isEmpty()
        private set

    val total: Int get() = questions.size

    val isLast: Boolean get() = index == total - 1

    val current: QuizQuestion? get() = questions.getOrNull(index)

    /** 지금 문제에서 고른 선택지. 고르지 않았으면 null */
    val selected: Int? get() = picks.getOrNull(index)

    /** 지금 문제에서 선택지를 골라 정오답이 드러난 상태 */
    val answered: Boolean get() = selected != null

    /** 지금 문제의 정오답. 아직 고르지 않았으면 null */
    val currentCorrect: Boolean? get() = selected?.let { it == current?.answerIndex }

    val canAdvance: Boolean get() = !finished && answered

    /** 선택지를 고른다. 이미 고른 문제는 바꿀 수 없다 */
    fun select(choice: Int) {
        val question = current ?: return
        if (finished || answered || choice !in question.choices.indices) return
        picks[index] = choice
    }

    /** 다음 문제로 간다. 고르지 않았으면 아무 일도 하지 않는다 */
    fun next() {
        if (canAdvance) advance()
    }

    /** 지금 문제를 건너뛴다 (오답 처리). 이미 고른 문제는 건너뛸 수 없다 */
    fun skip() {
        if (finished || answered) return
        advance()
    }

    fun result(): QuizResult = QuizResult(
        correct = questions.indices.count { picks[it] == questions[it].answerIndex },
        total = total
    )

    private fun advance() {
        if (isLast) finished = true else index++
    }
}
