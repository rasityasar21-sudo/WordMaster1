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
        val definition = "A specialized device designed to measure display and record changes in air pressure during a long period of continuous operation"
        assertTrue(DictionaryEntryFilter.shouldExclude("barometer", definition))
    }
    @Test
    fun rejectsSourcePlaceholdersAndRepeatedFiller() {
        listOf("null", "undefined", "n/a", "aaaaa", ".....", "???").forEach { entry ->
            assertTrue("Expected to reject $entry", DictionaryEntryFilter.shouldExclude(entry, "anlam"))
        }
        listOf("null", "undefined", "n/a", "-", "?").forEach { translation ->
            assertTrue("Expected to reject translation $translation", DictionaryEntryFilter.shouldExclude("engine", translation))
        }
    }

    @Test
    fun keepsTechnicalIdentifiersAndUsefulQuickPracticeVocabulary() {
        assertFalse(DictionaryEntryFilter.shouldExclude("B737", "Boeing 737"))
        assertFalse(DictionaryEntryFilter.shouldExclude("N1", "motor fan devri"))
        assertTrue(DictionaryEntryFilter.isQuickPracticeEntry("rudder", "dümen"))
        assertTrue(DictionaryEntryFilter.isQuickPracticeEntry("take off", "havalanmak"))
    }

    @Test
    fun quickPracticeKeepsShortEntriesAndRejectsLongPhrasesAndDefinitions() {
        assertTrue(DictionaryEntryFilter.isQuickPracticeEntry("engine", "motor"))
        assertTrue(DictionaryEntryFilter.isQuickPracticeEntry("take off", "havalanmak"))
        assertFalse(DictionaryEntryFilter.isQuickPracticeEntry("15 minutes of fame", "sabunköpüğü"))
        assertFalse(DictionaryEntryFilter.isQuickPracticeEntry("pistil", "n. 1. şekillendirme — The action of the verb to shape."))
        assertFalse(DictionaryEntryFilter.isQuickPracticeEntry("a very long technical phrase", "uzun anlam"))
    }

}
