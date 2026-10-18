package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AviationCourseCatalogTest {
    @Test
    fun aviationTrackHasSubstantialLessonsAndValidPrerequisites() {
        val aviation = WordMasterCourseCatalog.lessons.filter { it.level == "Mesleki" }
        assertTrue("Aviation track should contain at least eight lessons", aviation.size >= 8)
        val ids = WordMasterCourseCatalog.lessons.map { it.id }.toSet()
        assertTrue(aviation.all { it.prerequisiteIds.all(ids::contains) })
        assertEquals(aviation.size, aviation.map { it.id }.toSet().size)
    }

    @Test
    fun flightControlLessonIncludesCoreEnglishTerms() {
        val lesson = WordMasterCourseCatalog.find("aviation-flight-controls")
        assertTrue(lesson != null)
        assertTrue(lesson!!.objectives.any { it.contains("aileron", ignoreCase = true) })
    }
}
