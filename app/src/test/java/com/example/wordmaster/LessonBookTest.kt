package com.example.wordmaster

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonBookTest {
    @Test
    fun pastLessonsProvideFormationExamplesAndMistakeGuidance() {
        val chapter = LessonBook.chapter("Geçmiş zaman: was/were")

        assertTrue(chapter.explanation.isNotBlank())
        assertTrue(chapter.formation.contains("V1"))
        assertTrue(chapter.examples.size >= 2)
        assertFalse(chapter.commonMistakes.isEmpty())
        assertFalse(chapter.checkpoints.isEmpty())
    }

    @Test
    fun conditionalLessonsExplainBothConditionalTypes() {
        val chapter = LessonBook.chapter("Birinci ve ikinci koşul cümleleri")

        assertTrue(chapter.formation.contains("First"))
        assertTrue(chapter.formation.contains("Second"))
        assertTrue(chapter.examples.any { it.first.startsWith("If ") })
    }

    @Test
    fun unrecognizedLessonStillGetsStructuredLearningGuidance() {
        val chapter = LessonBook.chapter("Teknik kelime dağarcığı")

        assertTrue(chapter.purpose.isNotBlank())
        assertTrue(chapter.explanation.isNotBlank())
        assertFalse(chapter.examples.isEmpty())
        assertFalse(chapter.checkpoints.isEmpty())
    }
    @Test
    fun articleLessonsExplainSoundBasedChoiceAndSpecificReference() {
        val chapter = LessonBook.chapter("Articles: a, an and the")
        assertTrue(chapter.explanation.contains("ses"))
        assertTrue(chapter.formation.contains("a +"))
        assertTrue(chapter.examples.size >= 3)
    }

    @Test
    fun modalLessonsCoverMeaningAndBaseVerbRule() {
        val chapter = LessonBook.chapter("Modal verbs: must and should")
        assertTrue(chapter.formation.contains("V1"))
        assertTrue(chapter.examples.any { it.first.contains("should") })
    }

    @Test
    fun writingLessonsHaveAnOrganizedWritingFramework() {
        val chapter = LessonBook.chapter("Formal email writing")
        assertTrue(chapter.formation.contains("purpose"))
        assertTrue(chapter.checkpoints.size >= 3)
    }

    @Test
    fun everyB1ToC1CatalogLessonHasTopicSpecificBookContent() {
        val advancedLessons = WordMasterCourseCatalog.lessons.filter {
            it.level in setOf("B1", "B2", "C1")
        }

        assertTrue("Expected B1-C1 lessons in the catalog", advancedLessons.isNotEmpty())
        advancedLessons.forEach { lesson ->
            val chapter = LessonBook.chapter(lesson.title)
            assertTrue("Missing topic-specific chapter for: ${lesson.title}",
                chapter.purpose != "Konuyu adım adım öğren")
            assertTrue("Missing explanation for: ${lesson.title}", chapter.explanation.isNotBlank())
            assertTrue("Missing examples for: ${lesson.title}", chapter.examples.size >= 2)
            assertTrue("Missing checkpoints for: ${lesson.title}", chapter.checkpoints.isNotEmpty())
        }
    }

    @Test
    fun aviationLessonsIncludeAnOperationalSafetyBoundary() {
        val chapter = LessonBook.chapter("Aviation English")
        assertTrue(chapter.explanation.contains("onaylı prosedürler"))
        assertTrue(chapter.commonMistakes.isNotEmpty())
    }

}
