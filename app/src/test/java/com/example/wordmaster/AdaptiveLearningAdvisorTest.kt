package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class AdaptiveLearningAdvisorTest {
    private val today = LocalDate.now()

    @Test
    fun prioritizesRepeatedlyDifficultWords() {
        val words = listOf(
            WordProgress(word = "easy", correctCount = 8, mistakeCount = 1, dueDate = today.toString()),
            WordProgress(word = "hard", correctCount = 1, mistakeCount = 4, dueDate = today.toString())
        )
        val advice = AdaptiveLearningAdvisor.recommend(words)

        assertEquals("Önce zorlandığın kelimeler", advice.title)
        assertEquals(listOf("hard"), advice.focusWords)
    }

    @Test
    fun recommendsNewLessonWhenNoWordsAreDue() {
        val advice = AdaptiveLearningAdvisor.recommend(
            listOf(WordProgress(word = "future", dueDate = today.plusDays(3).toString()))
        )

        assertEquals("Yeni konulara hazırsın", advice.title)
        assertTrue(advice.focusWords.isEmpty())
    }

    @Test
    fun suggestsReinforcementWhenMistakesExistButAccuracyIsNotVeryLow() {
        val advice = AdaptiveLearningAdvisor.recommend(
            listOf(WordProgress(word = "mixed", correctCount = 2, mistakeCount = 1, dueDate = today.toString()))
        )

        assertEquals("Tekrarla ve pekiştir", advice.title)
        assertEquals(listOf("mixed"), advice.focusWords)
    }
}
