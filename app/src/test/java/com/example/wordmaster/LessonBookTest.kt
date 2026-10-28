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
    fun b1ListeningLessonCoversParaphraseCorrectionsAndEvidence() {
        val chapter = LessonBook.chapter("Dinlemede ayrıntı yakalama")

        assertTrue(chapter.explanation.contains("farklı kelimelerle"))
        assertTrue(chapter.examples.size >= 4)
        assertTrue(chapter.commonMistakes.any { it.contains("olumsuzlukları") })
        assertTrue(chapter.checkpoints.size >= 4)
    }

    @Test
    fun b1NarrativeLessonDistinguishesBackgroundAndMainEvents() {
        val chapter = LessonBook.chapter("Olay anlatımı ve bağlaçlar")

        assertTrue(chapter.explanation.contains("Past Simple"))
        assertTrue(chapter.explanation.contains("Past Continuous"))
        assertTrue(chapter.formation.contains("when"))
        assertTrue(chapter.examples.size >= 4)
        assertTrue(chapter.checkpoints.size >= 5)
    }

    @Test
    fun b1ProblemSolvingLessonSeparatesEvidenceFromAssumptions() {
        val chapter = LessonBook.chapter("Sorun açıklama ve çözüm önerme")

        assertTrue(chapter.purpose.contains("sorun", ignoreCase = true))
        assertTrue(chapter.explanation.length > 100)
        assertTrue(chapter.formation.contains("evidence", ignoreCase = true))
        assertTrue(chapter.examples.any { it.first.contains("cannot confirm the cause") })
        assertTrue(chapter.commonMistakes.any { it.contains("kanıt yoksa") })
        assertTrue(chapter.checkpoints.size >= 5)
    }

    @Test
    fun b2ArgumentComparisonLessonUsesEvidenceAndBalancedConclusion() {
        val chapter = LessonBook.chapter("Görüşleri karşılaştırma")

        assertTrue(chapter.purpose.contains("Karşıt görüşleri"))
        assertTrue(chapter.explanation.contains("varsayımlarını"))
        assertTrue(chapter.formation.contains("Claim A"))
        assertTrue(chapter.examples.any { it.first.contains("Whereas") })
        assertTrue(chapter.commonMistakes.any { it.contains("Karşıt görüşü") })
        assertTrue(chapter.checkpoints.size >= 5)
    }

    @Test
    fun b2ReadingListeningReportingAndNegotiationLessonsHaveFocusedContent() {
        val reading = LessonBook.chapter("Eleştirel okuma ve çıkarım")
        val listening = LessonBook.chapter("Uzun konuşmalarda ana fikir")
        val reporting = LessonBook.chapter("Veri ve sonuçları raporlama")
        val negotiation = LessonBook.chapter("Müzakere ve uzlaşma")
        val writing = LessonBook.chapter("Akademik paragraf ve argüman")

        assertTrue(reading.explanation.contains("iddiasını"))
        assertTrue(listening.formation.contains("speaker's purpose"))
        assertTrue(reporting.explanation.contains("verinin neyi gösterdiğini"))
        assertTrue(negotiation.examples.any { it.first.contains("Could we") })
        assertTrue(writing.formation.contains("evidence"))
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


    @Test
    fun b2LessonsCoverHedgingMixedConditionalsCohesionAndParaphrasing() {
        val hedging = LessonBook.chapter("Akademik dilde ihtiyat ve kesinlik")
        val conditionals = LessonBook.chapter("İleri koşul cümleleri ve varsayımlar")
        val cohesion = LessonBook.chapter("Tutarlılık ve akademik bağlaçlar")
        val paraphrasing = LessonBook.chapter("Parafraz ve kaynakları doğru aktarma")

        assertTrue(hedging.explanation.contains("kanıtın gücü"))
        assertTrue(hedging.formation.contains("may/might/could"))
        assertTrue(hedging.examples.any { it.first.contains("may be related") })
        assertTrue(hedging.commonMistakes.any { it.contains("Suggests") })
        assertTrue(hedging.checkpoints.size >= 4)

        assertTrue(conditionals.formation.contains("past perfect"))
        assertTrue(conditionals.examples.any { it.first.contains("would live in London now") })
        assertTrue(conditionals.commonMistakes.any { it.contains("V3") })
        assertTrue(conditionals.checkpoints.size >= 4)

        assertTrue(cohesion.explanation.contains("mantıksal ilişki"))
        assertTrue(cohesion.formation.contains("therefore"))
        assertTrue(cohesion.examples.any { it.first.contains("whereas") })
        assertTrue(cohesion.checkpoints.size >= 4)

        assertTrue(paraphrasing.explanation.contains("kendi sözlerinle"))
        assertTrue(paraphrasing.formation.contains("new structure"))
        assertTrue(paraphrasing.examples.size >= 2)
        assertTrue(paraphrasing.commonMistakes.any { it.contains("kaynağın iddiasını") })
        assertTrue(paraphrasing.checkpoints.size >= 5)
    }

    @Test
    fun b2CatalogHasBroadCoverageAndEveryLessonHasSpecificBookContent() {
        val b2Lessons = WordMasterCourseCatalog.forLevel("B2")

        assertTrue("Expected at least 12 B2 lessons", b2Lessons.size >= 12)
        b2Lessons.forEach { lesson ->
            val chapter = LessonBook.chapter(lesson.title)
            assertTrue("Missing B2 chapter for: ${lesson.title}",
                chapter.purpose != "Konuyu adım adım öğren")
            assertTrue("Missing B2 examples for: ${lesson.title}", chapter.examples.size >= 2)
            assertTrue("Missing B2 checkpoints for: ${lesson.title}", chapter.checkpoints.isNotEmpty())
        }
    }

}
