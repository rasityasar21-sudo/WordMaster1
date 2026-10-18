package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LearningEngineTest {
    @Test
    fun correctRecallUsesIncreasingReviewIntervals() {
        val today = LocalDate.of(2026, 10, 18)
        val first = WordProgress(word = "apple").reviewed(5, today)
        val second = first.reviewed(5, today.plusDays(1))
        val third = second.reviewed(5, today.plusDays(4))

        assertEquals(1, first.intervalDays)
        assertEquals(3, second.intervalDays)
        assertTrue(third.intervalDays > second.intervalDays)
        assertEquals(3, third.streak)
    }

    @Test
    fun difficultRecallResetsStreakAndSchedulesNextDay() {
        val today = LocalDate.of(2026, 10, 18)
        val existing = WordProgress(word = "journey", streak = 4, intervalDays = 12)
        val updated = existing.reviewed(2, today)

        assertEquals(0, updated.streak)
        assertEquals(1, updated.intervalDays)
        assertEquals(today.plusDays(1).toString(), updated.dueDate)
        assertEquals(1, updated.mistakeCount)
        assertEquals(0, updated.correctCount)
    }

    @Test
    fun recallQualityIsBoundedToSupportedRange() {
        val today = LocalDate.of(2026, 10, 18)
        val tooLow = WordProgress(word = "low").reviewed(-10, today)
        val tooHigh = WordProgress(word = "high").reviewed(99, today)

        assertEquals(1, tooLow.intervalDays)
        assertEquals(1, tooLow.mistakeCount)
        assertEquals(1, tooHigh.intervalDays)
        assertEquals(1, tooHigh.correctCount)
    }

    @Test
    fun accuracyUsesCorrectAndIncorrectAttempts() {
        val progress = WordProgress(word = "careful", correctCount = 3, mistakeCount = 1)

        assertEquals(75, progress.accuracyPercent)
    }

    @Test
    fun dueReviewQueueIncludesTodayAndOverdueWords() {
        val today = LocalDate.now()
        val words = listOf(
            WordProgress(word = "future", dueDate = today.plusDays(2).toString()),
            WordProgress(word = "today", dueDate = today.toString()),
            WordProgress(word = "overdue", dueDate = today.minusDays(1).toString())
        )

        val due = WordMasterLearningEngine.dueForReview(words).map { it.word }

        assertTrue("today" in due)
        assertTrue("overdue" in due)
        assertTrue("future" !in due)
    }



    @Test
    fun summaryReportsAccuracyMasteryAndDueCounts() {
        val today = LocalDate.now()
        val words = listOf(
            WordProgress(word = "mastered", correctCount = 8, mistakeCount = 1, streak = 3, dueDate = today.plusDays(3).toString()),
            WordProgress(word = "review", correctCount = 1, mistakeCount = 2, streak = 0, dueDate = today.toString())
        )

        val summary = WordMasterLearningEngine.summary(words, currentStreak = 4)

        assertEquals(2, summary.totalWords)
        assertEquals(1, summary.masteredWords)
        assertEquals(1, summary.dueWords)
        assertEquals(9, summary.totalCorrect)
        assertEquals(3, summary.totalMistakes)
        assertEquals(75, summary.accuracyPercent)
        assertEquals(4, summary.currentStreak)
    }

    @Test
    fun reviewQueuePrioritizesLowerAccuracyThenOlderDueDate() {
        val today = LocalDate.now()
        val words = listOf(
            WordProgress(word = "high", correctCount = 9, mistakeCount = 1, dueDate = today.minusDays(2).toString()),
            WordProgress(word = "low-old", correctCount = 1, mistakeCount = 3, dueDate = today.minusDays(2).toString()),
            WordProgress(word = "low-new", correctCount = 1, mistakeCount = 3, dueDate = today.toString()),
            WordProgress(word = "future", dueDate = today.plusDays(1).toString())
        )

        val due = WordMasterLearningEngine.dueForReview(words).map { it.word }

        assertEquals(listOf("low-old", "low-new", "high"), due)
    }

    @Test
    fun decodingInvalidOrMissingProgressDataSafelyReturnsEmptyList() {
        assertTrue(WordMasterLearningEngine.decode(null).isEmpty())
        assertTrue(WordMasterLearningEngine.decode("not-json").isEmpty())
        assertTrue(WordMasterLearningEngine.decode("[null, 7, {}]").isEmpty())
    }
}
