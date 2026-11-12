package com.roadstudy.automotive.ui.quiz

/** 선택지 한 칸의 표시 상태 */
enum class ChoiceMark {
    /** 아직 고르기 전 */
    NONE,

    /** 정답 (고른 뒤에 초록으로 보여준다) */
    CORRECT,

    /** 내가 고른 오답 */
    WRONG,

    /** 고른 뒤의 나머지 칸 (흐리게) */
    DIMMED
}

data class QuizChoiceUi(val text: String, val mark: ChoiceMark)

/** 고른 직후에 보여줄 정오답 안내 */
enum class QuizFeedback { NONE, CORRECT, WRONG }

/** 퀴즈 한 페이지(한 문제)에 그대로 뿌릴 값 */
data class QuizUiState(
    /** 몇 번째 문제인지 (1부터) */
    val pageNumber: Int,
    val total: Int,
    val sectionTitle: String,
    val question: String,
    val hint: String,
    val choices: List<QuizChoiceUi>,
    /** 고른 직후의 정오답. 고르기 전에는 NONE 이고 단서(hint)를 보여준다 */
    val feedback: QuizFeedback,
    /** 정답 글자. 틀렸을 때 "정답은 ○○" 안내에 쓴다 */
    val answerText: String,
    /** 선택지를 골라서 `다음 문제`로 갈 수 있는 상태 */
    val canAdvance: Boolean,
    /** 아직 고르지 않아 `건너뛰기`를 보여줄 수 있는 상태 */
    val canSkip: Boolean,
    /** 마지막 문제라서 버튼이 `결과 보기`가 되어야 하는 상태 */
    val isLast: Boolean
)

/** 퀴즈 결과 화면에 그대로 뿌릴 값 */
data class QuizResultUiState(
    val correct: Int,
    val total: Int,
    /** 이번 주 확인 퀴즈 정답률(%) */
    val weeklyAccuracyPercent: Int,
    val streakDays: Int
)
