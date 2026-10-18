package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyPlanReviewIntegrationTest {
    @Test
    fun dailyPlanIncludesDueWordsInWeakestFirstOrderAndHonorsLimit() {
        val words = listOf(
            WordProgress(word = "strong", correctCount = 9, mistakeCount = 0),
            WordProgress(word = "weak", correctCount = 1, mistakeCount = 4),
            WordProgress(word = "middle", correctCount = 3, mistakeCount = 2)
        )
        val plan = DailyLearningPlanner.create(
            preferredLevel = "A1",
            completedLessonIds = emptySet(),
            availableMinutes = 20,
            wordProgress = words,
            maxReviewWords = 2
        )
        assertEquals(listOf("weak", "middle"), plan.reviewWords.map { it.word })
    }

    @Test
    fun dailyPlanCanDisableVocabularyReviewQueue() {
        val plan = DailyLearningPlanner.create(
            preferredLevel = "A1",
            completedLessonIds = emptySet(),
            wordProgress = listOf(WordProgress(word = "test")),
            maxReviewWords = 0
        )
        assertEquals(emptyList<String>(), plan.reviewWords.map { it.word })
    }
}
