package com.roadstudy.shared.model

/**
 * PT 시연용 샘플 자료. (콘텐츠는 100% 사용자 업로드 기반이고, 이것만 예외다)
 *
 * - 지금은 코드 안에 둔 임시 내용이다. 나중에 JSON + 오디오 파일로 바꾼다.
 * - 오디오 주소는 아직 실제 파일이 없는 자리 표시용 값이다.
 */
object SampleStudySections {

    val dbNormalization = StudySection(
        sectionId = "db-normalization",
        title = "DB 정규화 정리",
        type = MaterialType.CERTIFICATE,
        sourceFileName = "DB_정규화_정리.pdf",
        concepts = listOf(
            concept("db", "c01", "함수 종속", "어떤 속성의 값이 정해지면 다른 속성의 값이 하나로 정해지는 관계예요.", "함수 종속이란?", "한 속성이 다른 속성을 결정하는 관계"),
            concept("db", "c02", "이상 현상", "정규화되지 않은 테이블에서 삽입, 삭제, 갱신할 때 생기는 문제예요.", "이상 현상 세 가지는?", "삽입, 삭제, 갱신 이상"),
            concept("db", "c03", "제1정규형", "모든 속성의 값이 더 쪼갤 수 없는 하나의 값만 가지는 상태예요.", "제1정규형의 조건은?", "모든 속성이 원자값"),
            concept("db", "c04", "제2정규형", "기본키의 일부에만 종속된 부분 함수 종속을 없앤 상태예요.", "제2정규형에서 없애는 종속은?", "부분 함수 종속"),
            concept("db", "c05", "제3정규형", "기본키가 아닌 속성이 다른 속성을 거쳐 결정되는 이행 종속을 없앤 상태예요.", "제3정규형에서 없애는 종속은?", "이행 함수 종속"),
            concept("db", "c06", "BCNF", "모든 결정자가 후보키인 상태예요.", "BCNF의 조건은?", "모든 결정자가 후보키"),
            concept("db", "c07", "반정규화", "성능을 위해 일부러 정규화를 되돌려 중복을 허용하는 거예요.", "반정규화를 하는 이유는?", "조회 성능을 높이기 위해"),
            // 직접 표를 그려 풀어야 하는 개념이라 운전 중에는 들려주지 않고 주차 화면에서만 보여준다
            concept("db", "c08", "정규화 실습", "표를 직접 나눠 보면서 정규형을 확인하는 실습이에요.", "표를 어떻게 나눌까요?", "이행 종속 속성을 별도 표로 분리", driveSafe = false)
        )
    )

    val businessEnglish = StudySection(
        sectionId = "business-english",
        title = "회의·메일 영어 표현",
        type = MaterialType.ENGLISH,
        sourceFileName = "비즈니스영어_표현.docx",
        concepts = listOf(
            concept("en", "e01", "Follow up", "이전 대화나 메일에 이어서 확인할 때 쓰는 표현이에요.", "이어서 확인할 때 쓰는 표현은?", "Just following up on my last email."),
            concept("en", "e02", "Circle back", "나중에 다시 다루자고 할 때 쓰는 표현이에요.", "나중에 다시 얘기하자는 표현은?", "Let's circle back on this next week."),
            concept("en", "e03", "Heads-up", "미리 알려줄 때 쓰는 표현이에요.", "미리 알려줄 때 쓰는 표현은?", "Just a heads-up, the deadline moved."),
            concept("en", "e04", "On the same page", "서로 같은 이해를 하고 있다는 뜻이에요.", "같은 이해를 하고 있다는 표현은?", "Let's make sure we're on the same page."),
            concept("en", "e05", "Touch base", "짧게 연락해서 상황을 공유할 때 쓰는 표현이에요.", "짧게 상황을 공유하자는 표현은?", "Can we touch base tomorrow?"),
            concept("en", "e06", "Looking forward to", "기대한다는 마음을 메일 끝에 덧붙이는 표현이에요.", "메일 끝에 쓰는 기대 표현은?", "Looking forward to hearing from you.")
        )
    )

    val all: List<StudySection> = listOf(dbNormalization, businessEnglish)

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
