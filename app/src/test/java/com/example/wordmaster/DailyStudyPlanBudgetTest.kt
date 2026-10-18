package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyStudyPlanBudgetTest {
    @Test
    fun reservesPartOfDailyGoalForDueVocabularyReviews() {
        val due = (1..10).map { WordProgress(word = "word$it") }
        val plan = DailyStudyPlan.create(
            level = "A1",
            completedLessonIds = emptySet(),
            dueWords = due,
            targetMinutes = 15,
            date = "2026-10-18"
        )

        assertEquals(5, plan.reviewWords.size)
        assertTrue(plan.lessonIds.isNotEmpty())
        assertTrue(plan.estimatedMinutes >= plan.reviewWords.size)
    }

    @Test
    fun reviewCountNeverExceedsOneThirdOfShortDailyGoal() {
        val due = (1..10).map { WordProgress(word = "word$it") }
        val plan = DailyStudyPlan.create(
            level = "A1",
            completedLessonIds = emptySet(),
            dueWords = due,
            targetMinutes = 6,
            date = "2026-10-18"
        )

        assertEquals(2, plan.reviewWords.size)
    }

    @Test
    fun planHasNoVocabularyReviewWhenNoWordsAreDue() {
        val plan = DailyStudyPlan.create(
            level = "A1",
            completedLessonIds = emptySet(),
            dueWords = emptyList(),
            targetMinutes = 15,
            date = "2026-10-18"
        )

        assertEquals(emptyList<String>(), plan.reviewWords)
    }
}
