package com.roadstudy.automotive.ui.quiz

import com.roadstudy.shared.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizSessionTest {

    /** 정답은 항상 0번 선택지다 */
    private fun question(id: String) = QuizQuestion(id, "문제 $id", "단서 $id", listOf("정답", "오답1", "오답2", "오답3"), 0)

    private fun session(count: Int = 3) = QuizSession((1..count).map { question("q$it") })

    @Test
    fun `처음에는 첫 문제이고 아무것도 고르지 않은 상태다`() {
        val quiz = session()

        assertEquals(0, quiz.index)
        assertEquals(3, quiz.total)
        assertNull(quiz.selected)
        assertFalse(quiz.canAdvance)
        assertFalse(quiz.finished)
    }

    @Test
    fun `선택지를 고르면 다음 문제로 갈 수 있다`() {
        val quiz = session()

        quiz.select(2)

        assertEquals(2, quiz.selected)
        assertTrue(quiz.canAdvance)
    }

    @Test
    fun `한 번 고르면 정오답이 드러나므로 선택을 바꿀 수 없다`() {
        val quiz = session()
        quiz.select(1)
        quiz.select(0)

        assertEquals(1, quiz.selected)
        assertEquals(false, quiz.currentCorrect)
    }

    @Test
    fun `정답을 고르면 정답이고 오답을 고르면 오답이다`() {
        val right = session().apply { select(0) }
        val wrong = session().apply { select(3) }

        assertEquals(true, right.currentCorrect)
        assertEquals(false, wrong.currentCorrect)
        assertNull(session().currentCorrect)
    }

    @Test
    fun `이미 고른 문제는 건너뛸 수 없다`() {
        val quiz = session()
        quiz.select(0)

        quiz.skip()

        assertEquals(0, quiz.index)
        assertEquals(0, quiz.selected)
    }

    @Test
    fun `고르지 않으면 다음 문제로 넘어가지 않는다`() {
        val quiz = session()

        quiz.next()

        assertEquals(0, quiz.index)
    }

    @Test
    fun `범위를 벗어난 선택은 무시한다`() {
        val quiz = session()

        quiz.select(9)
        quiz.select(-1)

        assertNull(quiz.selected)
    }

    @Test
    fun `다음 문제로 넘어가면 새 문제는 고르지 않은 상태다`() {
        val quiz = session()
        quiz.select(0)
        quiz.next()

        assertEquals(1, quiz.index)
        assertNull(quiz.selected)
    }

    @Test
    fun `마지막 문제에서 다음을 누르면 끝난다`() {
        val quiz = session()
        repeat(3) { quiz.select(0); quiz.next() }

        assertTrue(quiz.finished)
    }

    @Test
    fun `마지막 문제인지 알 수 있다`() {
        val quiz = session()
        quiz.select(0); quiz.next()
        assertFalse(quiz.isLast)
        quiz.select(0); quiz.next()

        assertTrue(quiz.isLast)
    }

    @Test
    fun `3문제 중 2문제 맞히면 결과는 3문제 중 2문제다`() {
        val quiz = session()
        quiz.select(0); quiz.next() // 정답
        quiz.select(2); quiz.next() // 오답
        quiz.select(0); quiz.next() // 정답

        assertEquals(QuizResult(correct = 2, total = 3), quiz.result())
    }

    @Test
    fun `건너뛴 문제는 오답이고 전체 문제 수에는 포함된다`() {
        val quiz = session()
        quiz.select(0); quiz.next() // 정답
        quiz.skip()                 // 건너뜀
        quiz.select(0); quiz.next() // 정답

        assertEquals(QuizResult(correct = 2, total = 3), quiz.result())
        assertTrue(quiz.finished)
    }

    @Test
    fun `모두 건너뛰면 0문제 맞힌 결과다`() {
        val quiz = session()
        repeat(3) { quiz.skip() }

        assertEquals(QuizResult(correct = 0, total = 3), quiz.result())
    }

    @Test
    fun `마지막 문제를 건너뛰어도 끝난다`() {
        val quiz = session(count = 1)

        quiz.skip()

        assertTrue(quiz.finished)
    }

    @Test
    fun `문제가 3개 미만이면 있는 만큼만 푼다`() {
        val quiz = session(count = 2)
        repeat(2) { quiz.select(0); quiz.next() }

        assertEquals(QuizResult(correct = 2, total = 2), quiz.result())
    }

    @Test
    fun `문제가 없으면 처음부터 끝난 상태다`() {
        val quiz = QuizSession(emptyList())

        assertTrue(quiz.finished)
        assertEquals(QuizResult(0, 0), quiz.result())
        assertNull(quiz.current)
    }

    @Test
    fun `끝난 뒤에는 선택하거나 건너뛰어도 결과가 변하지 않는다`() {
        val quiz = session(count = 1)
        quiz.select(0); quiz.next()
        quiz.select(2)
        quiz.skip()

        assertEquals(QuizResult(1, 1), quiz.result())
    }
}
