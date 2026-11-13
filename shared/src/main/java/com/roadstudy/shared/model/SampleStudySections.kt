package com.roadstudy.shared.model

/**
 * PT 시연용 샘플 자료. (콘텐츠는 100% 사용자 업로드 기반이고, 이것만 예외다)
 *
 * - 지금은 코드 안에 둔 임시 내용이다. 나중에 JSON + 오디오 파일로 바꾼다.
 * - 오디오 주소는 아직 실제 파일이 없는 자리 표시용 값이다.
 */
object SampleStudySections {

    /** 백엔드가 mp3로 만든 실제 자료(PDF)의 내용. 설명은 운전 중에 듣기 좋게 짧게 다시 썼다 */
    val processThread = StudySection(
        sectionId = "process-thread",
        title = "프로세스와 스레드 소개",
        type = MaterialType.WORK,
        sourceFileName = "1. 프로세스와 스레드 소개.pdf",
        concepts = listOf(
            concept("pt", "p01", "멀티태스킹", "CPU 코어 하나가 여러 프로그램을 아주 짧게, 약 0.01초씩 번갈아 실행해서 동시에 실행되는 것처럼 보이게 하는 거예요.", "코어 하나로 여러 프로그램을 동시에 돌리는 것처럼 보이게 하는 기술은?", "멀티태스킹"),
            concept("pt", "p02", "시분할", "각 프로그램의 실행 시간을 잘게 나눠 돌아가며 실행하는 기법이에요. 멀티태스킹이 이 방식으로 동작해요.", "실행 시간을 잘게 나눠 번갈아 실행하는 기법은?", "시분할, 타임 셰어링"),
            concept("pt", "p03", "멀티프로세싱", "둘 이상의 CPU 코어로 여러 작업을 진짜 동시에 처리하는 거예요. 멀티프로세싱은 하드웨어 관점, 멀티태스킹은 운영체제 관점이에요.", "멀티프로세싱은 하드웨어 관점, 멀티태스킹은 어떤 관점일까요?", "운영체제, 소프트웨어 관점"),
            concept("pt", "p04", "프로세스", "운영체제 안에서 실행 중인 프로그램이에요. 프로세스마다 독립된 메모리를 가져서, 하나가 문제로 종료돼도 다른 프로세스에는 영향이 없어요.", "실행 중인 프로그램을 뭐라고 할까요?", "프로세스"),
            // 네 영역을 그림으로 보며 이해하는 내용이라 운전 중에는 들려주지 않고 주차 화면에서만 보여준다
            concept("pt", "p05", "프로세스 메모리 구성", "프로세스 메모리는 코드, 데이터, 힙, 스택 네 영역으로 나뉘어요.", "프로세스 메모리의 네 영역은?", "코드, 데이터, 힙, 스택", driveSafe = false),
            concept("pt", "p06", "스레드", "프로세스 안에서 코드를 한 줄씩 실행하는 흐름이에요. 같은 프로세스의 스레드들은 메모리를 함께 쓰고, 스택만 각자 가져요.", "스레드마다 따로 갖는 메모리 영역은?", "스택"),
            concept("pt", "p07", "멀티스레드", "한 프로세스 안에 스레드가 여러 개인 거예요. 워드가 문서를 편집하면서 자동 저장과 맞춤법 검사를 함께 하는 것처럼요.", "프로세스 안에는 스레드가 최소 몇 개 있을까요?", "하나 이상"),
            concept("pt", "p08", "스케줄링", "운영체제가 어떤 스레드를 얼마나 CPU에서 실행할지 정하는 거예요. 스레드들은 스케줄링 큐에서 차례를 기다려요.", "CPU에서 실제로 실행되는 단위는 프로세스와 스레드 중 무엇일까요?", "스레드"),
            concept("pt", "p09", "컨텍스트 스위칭", "실행할 스레드를 바꿀 때 지금 상태를 저장하고 다음 스레드의 상태를 불러오는 과정이에요. 이때 약간의 비용이 들어요.", "스레드를 바꿀 때 상태를 저장하고 다시 불러오는 과정은?", "컨텍스트 스위칭"),
            concept("pt", "p10", "CPU 바운드와 I/O 바운드", "계산이 많은 작업은 CPU 바운드, 디스크나 네트워크를 기다리는 일이 많은 작업은 I/O 바운드예요. CPU 바운드는 스레드를 코어 수보다 하나 더, I/O 바운드는 그보다 많이 둬요.", "CPU 바운드 작업에 알맞은 스레드 수는?", "CPU 코어 수 더하기 1개")
        )
    )

    val opicEnglish = StudySection(
        sectionId = "opic-english",
        title = "오픽 답변 영어 표현",
        type = MaterialType.ENGLISH,
        sourceFileName = "오픽_답변_표현.pdf",
        concepts = listOf(
            concept("en", "e01", "시간 벌기", "답이 바로 떠오르지 않을 때 침묵하지 않고 생각할 시간을 버는 표현이에요.", "답이 바로 떠오르지 않을 때 시간을 버는 표현은?", "That's a good question. Let me think for a second."),
            concept("en", "e02", "경험 말하기 시작", "과거 경험을 묻는 질문에서 이야기를 꺼낼 때 쓰는 표현이에요.", "과거 경험 이야기를 시작하는 표현은?", "I still remember the time when..."),
            concept("en", "e03", "장소 묘사", "집이나 동네를 묘사하는 질문에서 위치부터 말할 때 쓰는 표현이에요.", "장소의 위치를 설명하기 시작하는 표현은?", "It's located in..."),
            concept("en", "e04", "과거와 비교", "예전과 지금 달라진 점을 묻는 질문에서 쓰는 표현이에요.", "예전과 비교해 달라진 점을 말할 때 쓰는 표현은?", "Compared to the past, ..."),
            concept("en", "e05", "롤플레이 문의", "롤플레이 문제에서 전화로 무언가를 물어볼 때 첫마디로 쓰는 표현이에요.", "롤플레이에서 전화 문의를 시작하는 표현은?", "Hi, I'm calling to ask about..."),
            concept("en", "e06", "답변 마무리", "답변을 자연스럽게 끝낼 때 쓰는 표현이에요.", "답변을 자연스럽게 마무리하는 표현은?", "Anyway, that's about it.")
        )
    )

    val all: List<StudySection> = listOf(processThread, opicEnglish)

    private fun concept(
        prefix: String,
        id: String,
        title: String,
        explanation: String,
        question: String,
        answer: String,
        driveSafe: Boolean = true
    ) = Concept(
        id = id,
        title = title,
        explanation = explanation,
        explanationAudioUrl = "sample/$prefix/${id}_explain.mp3",
        questionAudioUrl = "sample/$prefix/${id}_question.mp3",
        answerAudioUrl = "sample/$prefix/${id}_answer.mp3",
        durationSec = 120,
        question = question,
        answer = answer,
        driveSafe = driveSafe
    )
}
