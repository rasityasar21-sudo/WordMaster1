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
    fun pastSimpleLessonCoversTimeMarkersRegularSpellingAndIrregularVerbs() {
        val chapter = LessonBook.chapter("Geçmiş zaman: düzenli ve düzensiz fiiller")

        assertTrue(chapter.explanation.contains("Yesterday"))
        assertTrue(chapter.explanation.contains("study → studied"))
        assertTrue(chapter.explanation.contains("go → went"))
        assertTrue(chapter.formation.contains("didn't + V1"))
        assertTrue(chapter.examples.any { it.first.contains("studied") })
        assertTrue(chapter.commonMistakes.any { it.contains("studyed") })
    }

    @Test
    fun futurePlansLessonCoversIntentNegativesQuestionsAndContext() {
        val chapter = LessonBook.chapter("Gelecek planları: going to / will")

        assertTrue(chapter.explanation.contains("önceden yapılmış planlarda"))
        assertTrue(chapter.formation.contains("won't + V1"))
        assertTrue(chapter.formation.contains("Am/Is/Are"))
        assertTrue(chapter.examples.any { it.first.contains("going to visit") })
        assertTrue(chapter.examples.any { it.first.contains("won't forget") })
        assertTrue(chapter.commonMistakes.any { it.contains("won't to go") })
        assertTrue(chapter.checkpoints.size >= 4)
    }

    @Test
    fun everyA2CatalogLessonHasTopicSpecificBookContent() {
        val a2Lessons = WordMasterCourseCatalog.forLevel("A2")

        assertTrue("Expected A2 lessons in the catalog", a2Lessons.isNotEmpty())
        a2Lessons.forEach { lesson ->
            val chapter = LessonBook.chapter(lesson.title)
            assertTrue("Missing topic-specific A2 chapter for: ${lesson.title}",
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
