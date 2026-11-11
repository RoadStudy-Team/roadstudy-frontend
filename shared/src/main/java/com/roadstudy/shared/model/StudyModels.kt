package com.roadstudy.shared.model

/**
 * 업로드한 자료 하나 = 스터디 섹션 하나.
 * 백엔드와 차량 앱, 휴대폰 앱이 같은 형식으로 주고받는다.
 */
data class StudySection(
    val sectionId: String,
    val title: String,
    val type: MaterialType,
    val concepts: List<Concept>
)

enum class MaterialType { CERTIFICATE, ENGLISH, WORK, ETC }

/**
 * 학습의 기본 단위. 짧은 설명 오디오 + 확인 질문 1개.
 * driveSafe 가 false 면 주행 중에는 건너뛰고 주차 화면에서만 보여준다.
 */
data class Concept(
    val id: String,
    val title: String,
    val explanation: String,
    val explanationAudioUrl: String,
    val questionAudioUrl: String,
    val answerAudioUrl: String,
    val durationSec: Int,
    val question: String,
    val answer: String,
    val driveSafe: Boolean = true
)

enum class ConceptStatus { NOT_STARTED, LEARNED, NEEDS_REVIEW }
