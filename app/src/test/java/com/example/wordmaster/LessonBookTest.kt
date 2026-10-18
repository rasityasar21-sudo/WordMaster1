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
}
