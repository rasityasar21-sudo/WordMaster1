package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessionalCourseCatalogTest {
    @Test
    fun catalogContainsAircraftMaintenanceAsFirstTrack() {
        val track = ProfessionalCourseCatalog.find("aviation-maintenance")
        assertNotNull(track)
        assertEquals("Uçak Bakım İngilizcesi", track?.title)
        assertTrue(track?.premiumOnly == true)
    }

    @Test
    fun aircraftMaintenanceIncludesCoreTechnicalVocabulary() {
        val terms = ProfessionalCourseCatalog.find("aviation-maintenance")!!.sampleTerms
            .map { it.english.lowercase() }
        assertTrue("maintenance" in terms)
        assertTrue("inspection" in terms)
        assertTrue("airframe" in terms)
        assertTrue("powerplant" in terms)
    }

    @Test
    fun everyTrackHasUniqueIdAndLearningTopics() {
        val tracks = ProfessionalCourseCatalog.tracks
        assertEquals(tracks.size, tracks.map { it.id }.toSet().size)
        assertTrue(tracks.all { it.title.isNotBlank() && it.topics.isNotEmpty() })
    }
    @Test
    fun aircraftCourseHasExpandedVocabularyAndTranslatedExamples() {
        val track = ProfessionalCourseCatalog.find("aviation-maintenance")!!
        assertTrue(track.sampleTerms.size >= 30)
        assertTrue(track.sampleTerms.any { it.note.startsWith("Türkçesi:") })
        assertTrue(track.topics.size >= 10)
    }

    @Test
    fun everyProfessionHasStarterVocabularyForTheQuiz() {
        assertTrue(ProfessionalCourseCatalog.tracks.all { it.sampleTerms.size >= 4 })
    }

    @Test
    fun everyProfessionHasAtLeastOneGuidedLesson() {
        val tracks = ProfessionalCourseCatalog.tracks
        assertTrue(tracks.all { it.lessons.isNotEmpty() })
        assertTrue(tracks.all { track -> track.lessons.all { lesson ->
            lesson.id.isNotBlank() && lesson.title.isNotBlank() &&
                lesson.explanation.isNotBlank() && lesson.phrases.isNotEmpty() &&
                lesson.question.isNotBlank() && lesson.answer.isNotBlank()
        } })
    }

    @Test
    fun lessonIdsAreUniqueWithinEachProfession() {
        assertTrue(ProfessionalCourseCatalog.tracks.all { track ->
            track.lessons.map { it.id }.toSet().size == track.lessons.size
        })
    }

    @Test
    fun aircraftCourseContainsStructuredLessonsWithExamplesAndChecks() {
        val lessons = ProfessionalCourseCatalog.find("aviation-maintenance")!!.lessons
        assertTrue(lessons.size >= 7)
        assertEquals(lessons.size, lessons.map { it.id }.toSet().size)
        assertTrue(lessons.all { it.objective.isNotBlank() && it.explanation.isNotBlank() })
        assertTrue(lessons.all { it.phrases.size >= 3 && it.question.isNotBlank() && it.answer.isNotBlank() })
    }

    @Test
    fun aircraftCourseIncludesMeasurementCorrosionAndHandoverLessons() {
        val ids = ProfessionalCourseCatalog.find("aviation-maintenance")!!.lessons.map { it.id }
        assertTrue("measurement-language" in ids)
        assertTrue("corrosion-and-wear" in ids)
        assertTrue("maintenance-handover" in ids)
    }
    @Test
    fun healthcareCourseHasPracticalCommunicationLessonsAndExpandedVocabulary() {
        val track = ProfessionalCourseCatalog.find("healthcare")!!
        assertTrue(track.lessons.size >= 3)
        assertTrue(track.sampleTerms.size >= 8)
        assertTrue(track.lessons.any { it.id == "patient-history" })
        assertTrue(track.lessons.any { it.id == "patient-instructions" })
        assertTrue(track.lessons.all { lesson ->
            lesson.phrases.size >= 3 && lesson.question.isNotBlank() && lesson.answer.isNotBlank()
        })
    }

    @Test
    fun softwareCourseCoversBugReportsCodeReviewAndDocumentation() {
        val track = ProfessionalCourseCatalog.find("software-it")!!
        assertTrue(track.lessons.size >= 3)
        assertTrue(track.sampleTerms.size >= 8)
        assertTrue(track.lessons.any { it.id == "bug-reporting" })
        assertTrue(track.lessons.any { it.id == "code-review" })
        assertTrue(track.lessons.any { it.id == "technical-documentation" })
    }

}
