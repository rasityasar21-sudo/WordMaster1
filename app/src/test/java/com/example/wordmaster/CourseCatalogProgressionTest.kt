package com.example.wordmaster

import org.junit.Assert.assertTrue
import org.junit.Test

class CourseCatalogProgressionTest {
    @Test
    fun everyCoreLevelHasLessonsWithObjectivesAndReasonableDurations() {
        listOf("A1", "A2", "B1", "B2", "C1").forEach { level ->
            val lessons = WordMasterCourseCatalog.forLevel(level)
            assertTrue("$level should contain multiple lessons", lessons.size >= 4)
            assertTrue("$level lessons should have objectives", lessons.all { it.objectives.isNotEmpty() })
            assertTrue("$level lessons should have reasonable durations", lessons.all { it.estimatedMinutes in 5..60 })
        }
    }

    @Test
    fun allPrerequisiteReferencesExist() {
        val ids = WordMasterCourseCatalog.lessons.map { it.id }.toSet()
        assertTrue(
            "Every prerequisite should reference an existing lesson",
            WordMasterCourseCatalog.lessons.all { lesson ->
                lesson.prerequisiteIds.all { it in ids }
            }
        )
    }

    @Test
    fun lessonIdsAreUnique() {
        val ids = WordMasterCourseCatalog.lessons.map { it.id }
        assertTrue("Course lesson IDs must be unique", ids.size == ids.toSet().size)
    }
}
