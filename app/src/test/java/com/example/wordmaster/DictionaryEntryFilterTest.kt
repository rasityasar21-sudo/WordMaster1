package com.example.wordmaster

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryEntryFilterTest {
    @Test
    fun rejectsNumberAndOrdinalNoise() {
        listOf("0th", "100th", "1000th", "42", "42nd", "1,000th").forEach { entry ->
            assertTrue("Expected to reject $entry", DictionaryEntryFilter.shouldExclude(entry, "sayı"))
        }
    }

    @Test
    fun keepsRealVocabularyAndOrdinalWords() {
        listOf("first", "engine", "rudder", "aileron", "hello").forEach { entry ->
            assertFalse("Expected to keep $entry", DictionaryEntryFilter.shouldExclude(entry, "gerçek Türkçe karşılık"))
        }
    }

    @Test
    fun rejectsMorphologyFragmentsAndEmptyTranslations() {
        assertTrue(DictionaryEntryFilter.shouldExclude("-gram", "yazı birimi"))
        assertTrue(DictionaryEntryFilter.shouldExclude("test", "Suf. örnek"))
        assertTrue(DictionaryEntryFilter.shouldExclude("test", ""))
        assertTrue(DictionaryEntryFilter.shouldExclude("", "çeviri"))
    }

    @Test
    fun rejectsLikelyEnglishDefinitionAccidentallyStoredAsTurkish() {
        val definition = "A specialized device designed to measure, display, and record changes in air pressure during a long period of continuous operation"
        assertTrue(DictionaryEntryFilter.shouldExclude("barometer", definition))
    }
}
