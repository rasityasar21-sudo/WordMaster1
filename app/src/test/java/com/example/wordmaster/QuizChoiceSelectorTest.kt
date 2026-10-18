package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizChoiceSelectorTest {
    @Test
    fun prefersSameLevelAndReturnsDifferentEnglishWordsAndMeanings() {
        val target = Word("apple", "elma", "A1")
        val words = listOf(
            target,
            Word("fruit", "elma", "A1"),
            Word("pear", "armut", "A1"),
            Word("bread", "ekmek", "A1"),
            Word("train", "tren", "B1"),
            Word("apple", "başka karşılık", "A1")
        )

        val choices = QuizChoiceSelector.select(target, words, 4)

        assertEquals(4, choices.size)
        assertTrue(choices.any { it.en == "apple" })
        assertEquals(choices.size, choices.map { it.en.lowercase() }.distinct().size)
        assertEquals(choices.size, choices.map { it.tr.trim().lowercase() }.distinct().size)
        assertTrue(choices.any { it.en == "pear" })
        assertTrue(choices.any { it.en == "bread" })
        assertTrue(choices.none { it.en == "fruit" })
    }

    @Test
    fun fallsBackToOtherLevelsWhenSameLevelHasTooFewDistinctMeanings() {
        val target = Word("run", "koşmak", "A1")
        val words = listOf(
            target,
            Word("jog", "koşmak", "A1"),
            Word("walk", "yürümek", "B1"),
            Word("speak", "konuşmak", "A2")
        )

        val choices = QuizChoiceSelector.select(target, words, 4)

        assertEquals(3, choices.size)
        assertTrue(choices.any { it.en == "walk" })
        assertTrue(choices.any { it.en == "speak" })
    }

    @Test
    fun invalidCountReturnsEmptyList() {
        assertTrue(QuizChoiceSelector.select(Word("book", "kitap", "A1"), emptyList(), 0).isEmpty())
    }
}
