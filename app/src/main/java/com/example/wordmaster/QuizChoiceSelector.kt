package com.example.wordmaster

/**
 * Builds distinct quiz options without shuffling/filtering the full 26k+ dictionary
 * on the UI thread. Call from a background dispatcher.
 */
object QuizChoiceSelector {
    fun select(target: Word, words: List<Word>, count: Int = 4): List<Word> {
        if (count <= 0) return emptyList()
        val targetKey = normalize(target.en)
        val targetMeaning = normalize(target.tr)
        val usedWords = mutableSetOf(targetKey)
        val usedMeanings = mutableSetOf(targetMeaning)
        val sameLevel = mutableListOf<Word>()
        val otherLevels = mutableListOf<Word>()

        // Random sampling is bounded, so a very large dictionary is never fully shuffled.
        val attempts = minOf(words.size * 2, 1800)
        repeat(attempts) {
            val candidate = words.randomOrNull() ?: return@repeat
            val key = normalize(candidate.en)
            val meaning = normalize(candidate.tr)
            if (key.isBlank() || meaning.isBlank() || key in usedWords || meaning in usedMeanings) return@repeat
            if (candidate.level.equals(target.level, ignoreCase = true)) sameLevel += candidate
            else otherLevels += candidate
        }

        val result = mutableListOf(target)
        fun addUnique(candidates: List<Word>) {
            for (candidate in candidates.shuffled()) {
                val key = normalize(candidate.en)
                val meaning = normalize(candidate.tr)
                if (key in usedWords || meaning.isBlank() || meaning in usedMeanings) continue
                result += candidate
                usedWords += key
                usedMeanings += meaning
                if (result.size >= count) return
            }
        }
        addUnique(sameLevel.distinctBy { normalize(it.en) })
        if (result.size < count) addUnique(otherLevels.distinctBy { normalize(it.en) })
        return result.shuffled()
    }

    private fun normalize(value: String): String =
        value.trim().lowercase()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[.!?;,:]+$"), "")
}
