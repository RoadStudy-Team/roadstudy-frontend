package com.roadstudy.automotive.ui.parked

import com.roadstudy.automotive.ui.home.HomeUiMapper
import com.roadstudy.shared.model.ConceptStatus

/**
 * [ParkedInput] 을 주차 후 복습 화면용 값([ParkedUiState])으로 바꾼다.
 * 순수 Kotlin 이라 단위 테스트로 검증한다.
 */
class ParkedUiMapper {

    fun map(input: ParkedInput): ParkedUiState {
        // TODO: "중요한 개념"의 기준이 정해지면 고르는 방식을 바꾼다. 지금은 이번에 들은 개념의 앞 3개
        val shown = input.heardConcepts.take(MAX_CONCEPTS)

        return ParkedUiState(
            tripType = input.tripType,
            tripMinutes = input.tripMinutes,
            conceptTotal = input.conceptTotal,
            sectionTitle = input.sectionTitle,
            fileName = input.fileName,
            knownCount = input.sectionConceptIds.count { input.statuses[it] == ConceptStatus.LEARNED },
            relistenCount = input.reviewQueue.count { it in input.sectionConceptIds },
            streakDays = input.streakDays,
            concepts = shown.map { concept ->
                ParkedConceptUi(
                    conceptId = concept.id,
                    title = concept.title,
                    summary = concept.answer,
                    choice = input.choices[concept.id]
                )
            },
            quizCount = shown.size
        )
    }

    companion object {
        const val MAX_CONCEPTS = 3

        /** 이동 시간(초)을 화면에 보여줄 분으로 바꾼다 */
        fun tripMinutes(etaSec: Int?): Int? = etaSec?.let { HomeUiMapper.ceilMinutes(it) }
    }
}
