package com.roadstudy.automotive.ui.quiz

import com.roadstudy.shared.model.QuizQuestion
import com.roadstudy.shared.model.SampleQuizzes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizUiMapperTest {

    private val mapper = QuizUiMapper()

    /** 정답은 첫 문제 1번(나), 둘째 2번(다), 셋째 3번(라) */
    private fun session() = QuizSession(
        listOf(
            QuizQuestion("a", "첫 문제", "첫 단서", listOf("가", "나", "다", "라"), 1),
            QuizQuestion("b", "둘째 문제", "둘째 단서", listOf("가", "나", "다", "라"), 2),
            QuizQuestion("c", "셋째 문제", "셋째 단서", listOf("가", "나", "다", "라"), 3)
        )
    )

    @Test
    fun `첫 페이지는 1 대 3이고 고르기 전이라 단서만 보인다`() {
        val state = mapper.map(session(), "DB 정규화 정리")!!

        assertEquals(1, state.pageNumber)
        assertEquals(3, state.total)
        assertEquals("DB 정규화 정리", state.sectionTitle)
        assertEquals("첫 문제", state.question)
        assertEquals("첫 단서", state.hint)
        assertEquals(listOf("가", "나", "다", "라"), state.choices.map { it.text })
        assertTrue(state.choices.all { it.mark == ChoiceMark.NONE })
        assertEquals(QuizFeedback.NONE, state.feedback)
        assertFalse(state.canAdvance)
        assertTrue(state.canSkip)
        assertFalse(state.isLast)
    }

    @Test
    fun `정답을 고르면 정답 칸만 초록이고 나머지는 흐려진다`() {
        val quiz = session()
        quiz.select(1)

        val state = mapper.map(quiz, "")!!

        assertEquals(
            listOf(ChoiceMark.DIMMED, ChoiceMark.CORRECT, ChoiceMark.DIMMED, ChoiceMark.DIMMED),
            state.choices.map { it.mark }
        )
        assertEquals(QuizFeedback.CORRECT, state.feedback)
        assertTrue(state.canAdvance)
        assertFalse(state.canSkip)
    }

    @Test
    fun `오답을 고르면 내가 고른 칸은 빨강, 정답 칸은 초록으로 알려준다`() {
        val quiz = session()
        quiz.select(3)

        val state = mapper.map(quiz, "")!!

        assertEquals(
            listOf(ChoiceMark.DIMMED, ChoiceMark.CORRECT, ChoiceMark.DIMMED, ChoiceMark.WRONG),
            state.choices.map { it.mark }
        )
        assertEquals(QuizFeedback.WRONG, state.feedback)
        assertEquals("나", state.answerText)
    }

    @Test
    fun `마지막 페이지는 3 대 3이고 결과 보기 상태다`() {
        val quiz = session()
        repeat(2) { quiz.select(0); quiz.next() }

        val state = mapper.map(quiz, "")!!

        assertEquals(3, state.pageNumber)
        assertTrue(state.isLast)
    }

    @Test
    fun `문제가 없으면 화면 값도 없다`() {
        assertNull(mapper.map(QuizSession(emptyList()), ""))
    }

    @Test
    fun `결과 화면 값에 맞힌 수와 전체 문제 수가 담긴다`() {
        val state = mapper.mapResult(QuizResult(correct = 2, total = 3))

        assertEquals(2, state.correct)
        assertEquals(3, state.total)
        assertEquals(QuizUiMapper.DEMO_WEEKLY_ACCURACY_PERCENT, state.weeklyAccuracyPercent)
        assertEquals(QuizUiMapper.DEMO_STREAK_DAYS, state.streakDays)
    }

    @Test
    fun `샘플 퀴즈는 개념 순서대로 나오고 없는 개념은 건너뛴다`() {
        val questions = SampleQuizzes.forConcepts(listOf("c07", "x99", "c02", "c05"))

        assertEquals(listOf("c07", "c02", "c05"), questions.map { it.conceptId })
    }

    @Test
    fun `샘플 퀴즈는 모두 선택지 4개이고 정답이 선택지 안에 있다`() {
        val ids = (1..7).map { "c0$it" } + (1..6).map { "e0$it" }

        val questions = SampleQuizzes.forConcepts(ids)

        assertEquals(13, questions.size)
        assertTrue(questions.all { it.choices.size == 4 })
        assertTrue(questions.all { it.answerIndex in it.choices.indices })
        assertTrue(questions.all { q -> q.choices.toSet().size == 4 })
    }
}
