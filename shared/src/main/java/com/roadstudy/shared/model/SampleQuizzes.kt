package com.roadstudy.shared.model

/**
 * PT 시연용 샘플 퀴즈. SampleStudySections 의 개념 하나마다 한 문제씩 있다.
 * TODO: 실제 퀴즈는 업로드한 자료에서 만들어지므로, 데이터 형식이 정해지면 교체한다.
 */
object SampleQuizzes {

    private val all: List<QuizQuestion> = listOf(
        // 프로세스와 스레드 소개 (p05 는 주차 화면 전용 개념이라 퀴즈가 없다)
        quiz("p01", "CPU 코어 하나로 여러 프로그램을 동시에 실행하는 것처럼 보이게 하는 기술은?", "아주 짧게 번갈아 실행해요",
            listOf("멀티태스킹", "멀티프로세싱", "컨텍스트 스위칭", "스케줄링"), 0),
        quiz("p02", "실행 시간을 잘게 나눠 프로그램을 번갈아 실행하는 기법은?", "시간을 나눠 쓴다는 뜻이에요",
            listOf("병렬 처리", "시분할", "스케줄링 큐", "멀티프로세싱"), 1),
        quiz("p03", "둘 이상의 CPU 코어로 여러 작업을 진짜 동시에 처리하는 것은?", "하드웨어 관점의 개념이에요",
            listOf("멀티태스킹", "시분할", "멀티프로세싱", "멀티스레드"), 2),
        quiz("p04", "운영체제 안에서 실행 중인 프로그램을 부르는 말은?", "프로그램의 인스턴스예요",
            listOf("스레드", "코어", "스택", "프로세스"), 3),
        quiz("p06", "같은 프로세스의 스레드들이 공유하지 않고 각자 갖는 메모리는?", "메서드 호출 때 지역 변수가 쌓여요",
            listOf("스택", "힙", "코드 섹션", "데이터 섹션"), 0),
        quiz("p07", "프로세스 안에 반드시 있어야 하는 스레드 수는?", "그래야 코드가 실행돼요",
            listOf("0개", "하나 이상", "코어 수만큼", "두 개 이상"), 1),
        quiz("p08", "CPU에서 실제로 실행되는 단위는?", "프로세스는 컨테이너 역할이에요",
            listOf("프로세스", "프로그램", "스레드", "파일"), 2),
        quiz("p09", "스레드를 바꿀 때 상태를 저장하고 다시 불러오는 과정은?", "이 과정에 비용이 들어요",
            listOf("스케줄링", "시분할", "멀티프로세싱", "컨텍스트 스위칭"), 3),
        quiz("p10", "CPU 바운드 작업에 알맞은 스레드 수는?", "코어를 거의 100% 쓰는 작업이에요",
            listOf("CPU 코어 수 + 1개", "CPU 코어 수의 10배", "1개", "제한 없음"), 0),
        // 오픽 답변 영어 표현
        quiz("e01", "답이 바로 떠오르지 않을 때 시간을 버는 표현은?", "침묵 대신 쓰는 말이에요",
            listOf("Let me think for a second.", "That's about it.", "It's located in...", "Compared to the past, ..."), 0),
        quiz("e02", "과거 경험 이야기를 시작하는 표현은?", "기억을 꺼내는 말이에요",
            listOf("It's located in...", "I still remember the time when...", "That's about it.", "I'm calling to ask about..."), 1),
        quiz("e03", "장소 묘사 질문에서 위치부터 말하는 표현은?", "집이나 동네를 설명할 때 써요",
            listOf("Compared to the past, ...", "Let me think for a second.", "It's located in...", "That's about it."), 2),
        quiz("e04", "예전과 비교해 달라진 점을 말하는 표현은?", "과거와 현재를 견줘요",
            listOf("I'm calling to ask about...", "It's located in...", "That's about it.", "Compared to the past, ..."), 3),
        quiz("e05", "롤플레이에서 전화 문의를 시작하는 표현은?", "전화를 건 이유부터 말해요",
            listOf("I'm calling to ask about...", "I still remember the time when...", "Compared to the past, ...", "Let me think for a second."), 0),
        quiz("e06", "답변을 자연스럽게 마무리하는 표현은?", "이야기를 끝낼 때 써요",
            listOf("It's located in...", "Anyway, that's about it.", "Let me think for a second.", "I still remember the time when..."), 1)
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
