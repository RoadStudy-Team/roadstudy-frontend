package com.roadstudy.shared.model

/**
 * 확인 퀴즈 한 문제. 개념 하나(conceptId)에 대응하는 4지선다다.
 * TODO: 데이터 형식(선택지를 Concept 에 넣을지, 별도 데이터로 둘지)은 팀에서 정한다.
 *       지금은 PT 시연용 샘플(SampleQuizzes)에서만 쓴다.
 */
data class QuizQuestion(
    val conceptId: String,
    val question: String,
    /** 정답을 알려주지 않는 짧은 단서 */
    val hint: String,
    val choices: List<String>,
    /** choices 에서 정답의 위치 (0부터) */
    val answerIndex: Int
) {
    init {
        require(answerIndex in choices.indices) { "answerIndex $answerIndex is out of range" }
    }
}
