package com.example.wordmaster

/**
 * Builds unambiguous multiple-choice options for the vocabulary quiz.
 * Prefers words from the same CEFR level and different Turkish meanings,
 * then falls back to other levels when the selected level is small.
 */
object QuizChoiceSelector {
    fun select(target: Word, words: List<Word>, count: Int = 4): List<Word> {
        if (count <= 0) return emptyList()
        val targetKey = normalize(target.en)
        val targetMeaning = normalize(target.tr)
        val unique = words.asSequence()
            .filter { normalize(it.en).isNotBlank() && normalize(it.en) != targetKey }
            .filter { normalize(it.tr).isNotBlank() && normalize(it.tr) != targetMeaning }
            .distinctBy { normalize(it.en) }
            .toList()

        val sameLevel = unique.filter { it.level.equals(target.level, ignoreCase = true) }.shuffled()
        val otherLevels = unique.filterNot { it.level.equals(target.level, ignoreCase = true) }.shuffled()
        val result = mutableListOf(target)
        val usedMeanings = mutableSetOf(targetMeaning)
        val usedWords = mutableSetOf(targetKey)

        for (candidate in sameLevel + otherLevels) {
            val wordKey = normalize(candidate.en)
            val meaningKey = normalize(candidate.tr)
            if (wordKey in usedWords || meaningKey.isBlank() || meaningKey in usedMeanings) continue
            result += candidate
            usedWords += wordKey
            usedMeanings += meaningKey
            if (result.size >= count) break
        }
        return result.shuffled()
    }

    private fun normalize(value: String): String =
        value.trim().lowercase()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[.!?;,:]+$"), "")
}
