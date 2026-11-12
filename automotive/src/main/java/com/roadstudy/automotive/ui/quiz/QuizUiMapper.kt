package com.roadstudy.automotive.ui.quiz

/**
 * [QuizSession] 의 진행 상태를 화면용 값으로 바꾼다.
 * 순수 Kotlin 이라 단위 테스트로 검증한다.
 */
class QuizUiMapper {

    /** 지금 푸는 문제의 화면 값. 문제가 없으면 null */
    fun map(session: QuizSession, sectionTitle: String): QuizUiState? {
        val question = session.current ?: return null
        val selected = session.selected
        val answered = session.answered

        return QuizUiState(
            pageNumber = session.index + 1,
            total = session.total,
            sectionTitle = sectionTitle,
            question = question.question,
            hint = question.hint,
            choices = question.choices.mapIndexed { index, text ->
                QuizChoiceUi(text, markOf(index, selected, question.answerIndex))
            },
            feedback = when (session.currentCorrect) {
                null -> QuizFeedback.NONE
                true -> QuizFeedback.CORRECT
                false -> QuizFeedback.WRONG
            },
            answerText = question.choices[question.answerIndex],
            canAdvance = session.canAdvance,
            canSkip = !answered,
            isLast = session.isLast
        )
    }

    fun mapResult(result: QuizResult): QuizResultUiState = QuizResultUiState(
        correct = result.correct,
        total = result.total,
        // TODO: 저장소(Room)가 생기면 실제 주간 정답률과 연속 학습일을 쓴다
        weeklyAccuracyPercent = DEMO_WEEKLY_ACCURACY_PERCENT,
        streakDays = DEMO_STREAK_DAYS
    )

    /** 고른 뒤에는 정답 칸은 CORRECT, 내가 고른 오답은 WRONG, 나머지는 DIMMED */
    private fun markOf(index: Int, selected: Int?, answerIndex: Int): ChoiceMark = when {
        selected == null -> ChoiceMark.NONE
        index == answerIndex -> ChoiceMark.CORRECT
        index == selected -> ChoiceMark.WRONG
        else -> ChoiceMark.DIMMED
    }

    companion object {
        const val DEMO_WEEKLY_ACCURACY_PERCENT = 78
        const val DEMO_STREAK_DAYS = 12
    }
}
