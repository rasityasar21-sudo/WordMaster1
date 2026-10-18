package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CourseCatalogTest {
    @Test
    fun courseCatalogHasUniqueIdsAndResolvablePrerequisites() {
        val lessons = WordMasterCourseCatalog.lessons
        val ids = lessons.map { it.id }

        assertEquals("Course lesson IDs must be unique", ids.size, ids.toSet().size)
        lessons.forEach { lesson ->
            lesson.prerequisiteIds.forEach { prerequisiteId ->
                assertNotNull(
                    "Missing prerequisite $prerequisiteId for ${lesson.id}",
                    WordMasterCourseCatalog.find(prerequisiteId)
                )
                assertFalse(
                    "A lesson cannot require itself: ${lesson.id}",
                    prerequisiteId == lesson.id
                )
            }
        }
    }

    @Test
    fun lessonLookupAndLevelFilteringAreCaseInsensitive() {
        assertNotNull(WordMasterCourseCatalog.find("a1-greetings"))
        assertNull(WordMasterCourseCatalog.find("missing-lesson"))
        assertEquals(
            WordMasterCourseCatalog.forLevel("A1"),
            WordMasterCourseCatalog.forLevel("a1")
        )
    }

    @Test
    fun lessonsWithPrerequisitesStayLockedUntilPrerequisiteIsCompleted() {
        val locked = WordMasterCourseCatalog.available(emptySet(), "A1")
        assertFalse(locked.any { it.id == "a1-questions" })

        val unlocked = WordMasterCourseCatalog.available(setOf("a1-pronouns"), "A1")
        assertTrue(unlocked.any { it.id == "a1-questions" })
    }

    @Test
    fun completedLessonsAreNotRecommendedAgain() {
        val available = WordMasterCourseCatalog.available(setOf("a1-greetings"), "A1")
        assertFalse(available.any { it.id == "a1-greetings" })
        assertEquals("a1-pronouns", WordMasterCourseCatalog.nextRecommended(setOf("a1-greetings"), "A1")?.id)
    }

    @Test
    fun dailyPlanRespectsTargetAndLimitsReviewQueue() {
        val due = (1..15).map { WordProgress(word = "word$it") }
        val plan = DailyStudyPlan.create(
            level = "A1",
            completedLessonIds = emptySet(),
            dueWords = due,
            targetMinutes = 15,
            date = "2026-10-18"
        )

        assertEquals("2026-10-18", plan.date)
        assertEquals(15, plan.targetMinutes)
        assertTrue(plan.estimatedMinutes >= plan.targetMinutes)
        assertEquals(10, plan.reviewWords.size)
    }

    @Test
    fun higherLevelPlanSurfacesMissingLowerLevelPrerequisitesFirst() {
        val plan = DailyStudyPlan.create(
            level = "A2",
            completedLessonIds = emptySet(),
            dueWords = emptyList(),
            targetMinutes = 15,
            date = "2026-10-18"
        )

        assertTrue(plan.lessonIds.contains("a1-pronouns"))
        assertTrue(plan.lessonIds.contains("a2-past"))
        assertTrue(
            plan.lessonIds.indexOf("a1-pronouns") < plan.lessonIds.indexOf("a2-past")
        )
    }

    @Test
    fun dailyPlanPlacesEveryPrerequisiteBeforeItsDependentLesson() {
        listOf("A1", "A2", "B1", "B2").forEach { level ->
            val plan = DailyStudyPlan.create(
                level = level,
                completedLessonIds = emptySet(),
                dueWords = emptyList(),
                targetMinutes = 30,
                date = "2026-10-18"
            )
            plan.lessonIds.forEachIndexed { index, lessonId ->
                val lesson = WordMasterCourseCatalog.find(lessonId)!!
                lesson.prerequisiteIds.forEach { prerequisiteId ->
                    if (prerequisiteId !in emptySet<String>()) {
                        val prerequisiteIndex = plan.lessonIds.indexOf(prerequisiteId)
                        assertTrue(
                            "Level $level: $prerequisiteId must precede $lessonId",
                            prerequisiteIndex >= 0 && prerequisiteIndex < index
                        )
                    }
                }
            }
        }
    }

    @Test
    fun beginnerPlanStartsWithAvailableLessonsRatherThanRepeatingPrerequisites() {
        val plan = DailyStudyPlan.create(
            level = "A1",
            completedLessonIds = emptySet(),
            dueWords = emptyList(),
            targetMinutes = 5
        )

        assertEquals("a1-greetings", plan.lessonIds.first())
        assertFalse(plan.lessonIds.first() == "a1-questions")
    }

    @Test
    fun dailyPlanClampsTargetToSupportedRange() {
        val tooShort = DailyStudyPlan.create("A1", emptySet(), emptyList(), targetMinutes = 1)
        val tooLong = DailyStudyPlan.create("A1", emptySet(), emptyList(), targetMinutes = 500)

        assertEquals(5, tooShort.targetMinutes)
        assertEquals(120, tooLong.targetMinutes)
    }
}
