package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SpacedRepetitionScheduleTest {
    private val today = LocalDate.of(2026, 10, 18)

    @Test
    fun correctQuizAnswerSchedulesNextReviewAndBuildsStreak() {
        val updated = WordProgress(word = "aircraft").reviewed(quality = 4, today = today)
        assertEquals(1, updated.correctCount)
        assertEquals(0, updated.mistakeCount)
        assertEquals(1, updated.streak)
        assertEquals(today.plusDays(1).toString(), updated.dueDate)
    }

    @Test
    fun incorrectQuizAnswerResetsStreakAndSchedulesSoonReview() {
        val old = WordProgress(word = "rudder", correctCount = 2, streak = 3, intervalDays = 10)
        val updated = old.reviewed(quality = 1, today = today)
        assertEquals(2, updated.correctCount)
        assertEquals(1, updated.mistakeCount)
        assertEquals(0, updated.streak)
        assertEquals(1, updated.intervalDays)
        assertEquals(today.toString(), updated.dueDate)
    }

    @Test
    fun repeatedSuccessfulRecallGraduallyIncreasesInterval() {
        val first = WordProgress(word = "fuselage").reviewed(4, today)
        val second = first.reviewed(4, today.plusDays(1))
        val third = second.reviewed(4, today.plusDays(4))
        assertEquals(1, first.intervalDays)
        assertEquals(3, second.intervalDays)
        assertTrue(third.intervalDays >= second.intervalDays)
    }

    @Test
    fun dueReviewPrioritizesWordsWithLowerAccuracy() {
        val weak = WordProgress(word = "weak", correctCount = 1, mistakeCount = 3, dueDate = today.toString())
        val strong = WordProgress(word = "strong", correctCount = 8, mistakeCount = 0, dueDate = today.toString())
        val result = WordMasterLearningEngine.dueForReview(listOf(strong, weak))
        assertEquals(listOf("weak", "strong"), result.map { it.word })
    }
}
