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
}
