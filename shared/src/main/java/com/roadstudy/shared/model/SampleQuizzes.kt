package com.roadstudy.shared.model

/**
 * PT 시연용 샘플 퀴즈. SampleStudySections 의 개념 하나마다 한 문제씩 있다.
 * TODO: 실제 퀴즈는 업로드한 자료에서 만들어지므로, 데이터 형식이 정해지면 교체한다.
 */
object SampleQuizzes {

    private val all: List<QuizQuestion> = listOf(
        // DB 정규화 정리
        quiz("c01", "어떤 속성의 값이 정해지면 다른 속성의 값이 하나로 정해지는 관계는?", "속성 사이의 결정 관계예요",
            listOf("함수 종속", "이행 종속", "부분 종속", "후보키"), 0),
        quiz("c02", "정규화되지 않은 테이블에서 삽입, 삭제, 갱신할 때 생기는 문제는?", "세 가지 작업에서 생겨요",
            listOf("조인 현상", "이상 현상", "병합 현상", "동시성 현상"), 1),
        quiz("c03", "모든 속성이 더 쪼갤 수 없는 값만 가지는 정규형은?", "가장 먼저 만족해야 하는 단계예요",
            listOf("제2정규형", "BCNF", "제1정규형", "제3정규형"), 2),
        quiz("c04", "부분 함수 종속을 제거한 정규형은?", "기본키의 일부에만 종속되는 경우예요",
            listOf("제1정규형", "제3정규형", "BCNF", "제2정규형"), 3),
        quiz("c05", "이행 함수 종속을 제거한 정규형은?", "기본키가 아닌 속성끼리 이어지는 종속이에요",
            listOf("제1정규형", "제2정규형", "제3정규형", "BCNF"), 2),
        quiz("c06", "모든 결정자가 후보키인 정규형은?", "제3정규형보다 한 단계 엄격해요",
            listOf("BCNF", "제1정규형", "제2정규형", "제3정규형"), 0),
        quiz("c07", "성능을 위해 일부러 정규화를 되돌려 중복을 허용하는 것은?", "조회를 빠르게 하려는 선택이에요",
            listOf("정규화", "분해", "반정규화", "조인"), 2),
        // 회의·메일 영어 표현
        quiz("e01", "이전 메일에 이어서 확인할 때 쓰는 표현은?", "이어서 묻는 상황이에요",
            listOf("Follow up", "Looking forward to", "Heads-up", "On the same page"), 0),
        quiz("e02", "나중에 다시 다루자고 할 때 쓰는 표현은?", "지금은 넘기고 다음에 얘기해요",
            listOf("Heads-up", "Circle back", "Looking forward to", "On the same page"), 1),
        quiz("e03", "미리 알려줄 때 쓰는 표현은?", "갑작스러운 변경을 먼저 전할 때 써요",
            listOf("Touch base", "Circle back", "Heads-up", "Looking forward to"), 2),
        quiz("e04", "서로 같은 이해를 하고 있다는 뜻의 표현은?", "생각이 어긋나지 않았는지 확인해요",
            listOf("Heads-up", "Follow up", "Touch base", "On the same page"), 3),
        quiz("e05", "짧게 연락해서 상황을 공유할 때 쓰는 표현은?", "가볍게 안부와 진행 상황을 나눠요",
            listOf("Touch base", "Heads-up", "Looking forward to", "Circle back"), 0),
        quiz("e06", "메일 끝에 기대하는 마음을 덧붙이는 표현은?", "답장을 기다린다는 인사예요",
            listOf("Heads-up", "Touch base", "Looking forward to", "On the same page"), 2)
    )

    /** [conceptIds] 순서대로, 퀴즈가 있는 개념의 문제만 돌려준다 */
    fun forConcepts(conceptIds: List<String>): List<QuizQuestion> =
        conceptIds.mapNotNull { id -> all.firstOrNull { it.conceptId == id } }

    private fun quiz(
        conceptId: String,
        question: String,
        hint: String,
        choices: List<String>,
        answerIndex: Int
    ) = QuizQuestion(conceptId, question, hint, choices, answerIndex)
}
