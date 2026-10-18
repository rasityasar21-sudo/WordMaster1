package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyLearningPlannerTest {
    @Test
    fun planUsesRequestedLevelAndStaysWithinTimeBudgetWhenPossible() {
        val plan = DailyLearningPlanner.create("A1", emptySet(), 20)
        assertEquals("A1", plan.level)
        assertTrue(plan.lessons.isNotEmpty())
        assertTrue(plan.lessons.all { it.level == "A1" })
        assertTrue(plan.totalMinutes <= 20)
    }

    @Test
    fun completedLessonsAreNotRecommendedAgain() {
        val first = DailyLearningPlanner.create("A1", emptySet(), 20)
        val completed = first.lessons.map { it.id }.toSet()
        val next = DailyLearningPlanner.create("A1", completed, 20)
        assertTrue(next.lessons.none { it.id in completed })
    }

    @Test
    fun sameLevelPrerequisitesMustBeCompleted() {
        val catalog = listOf(
            CourseLesson("lesson-a", "A", "A1", "Kelime", 5, listOf("A")),
            CourseLesson("lesson-b", "B", "A1", "Dil bilgisi", 5, listOf("B"), listOf("lesson-a"))
        )
        val plan = DailyLearningPlanner.create("A1", emptySet(), 10, catalog)
        assertEquals(listOf("lesson-a"), plan.lessons.map { it.id })
    }

    @Test
    fun invalidLevelFallsBackToA1AndSmallBudgetStillGetsOneLesson() {
        val plan = DailyLearningPlanner.create("unknown", emptySet(), 5)
        assertEquals("A1", plan.level)
        assertTrue(plan.lessons.isNotEmpty())
    }
}
