package com.roadstudy.automotive.ui.parked

import com.roadstudy.automotive.session.SelfCheck
import com.roadstudy.automotive.session.TripType
import com.roadstudy.shared.model.Concept
import com.roadstudy.shared.model.ConceptStatus

/** 주차 후 복습 화면을 그리는 데 필요한 원재료 */
data class ParkedInput(
    val tripType: TripType,
    /** 이번 이동 시간(분). 경로를 몰랐다면 null */
    val tripMinutes: Int?,
    /** 이번 세션에서 들은 개념 수 */
    val conceptTotal: Int,
    val sectionTitle: String,
    val fileName: String?,
    /** 섹션에 속한 모든 개념의 id. 진도 집계에 쓴다 */
    val sectionConceptIds: Set<String>,
    /** 이번 세션에서 들은 개념 (들은 순서) */
    val heardConcepts: List<Concept>,
    val statuses: Map<String, ConceptStatus>,
    val reviewQueue: List<String>,
    /** 이 화면에서 사용자가 고른 선택 (고르지 않은 개념은 없다) */
    val choices: Map<String, SelfCheck>,
    val streakDays: Int
)

/** "오늘 학습한 중요한 개념" 카드 하나 */
data class ParkedConceptUi(
    val conceptId: String,
    val title: String,
    /** 카드에 보여줄 핵심 한 줄 (정답) */
    val summary: String,
    /** 사용자가 고른 선택. 고르지 않았으면 null */
    val choice: SelfCheck?
)

/** 주차 후 복습 화면에 그대로 뿌릴 값. 문장 조립은 strings.xml 이 한다 */
data class ParkedUiState(
    val tripType: TripType,
    val tripMinutes: Int?,
    val conceptTotal: Int,
    val sectionTitle: String,
    val fileName: String?,
    /** 알고 있는 개념 수 (섹션 전체) */
    val knownCount: Int,
    /** 다시 들을 개념 수 (섹션 전체, 다음 이동 때 앞쪽에 나온다) */
    val relistenCount: Int,
    val streakDays: Int,
    val concepts: List<ParkedConceptUi>,
    /** 확인 퀴즈 문제 수 */
    val quizCount: Int
) {
    val hasConcepts: Boolean get() = concepts.isNotEmpty()
}
