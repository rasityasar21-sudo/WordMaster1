package com.example.wordmaster

/**
 * Selects distinct quiz options while avoiding a full shuffle of the large dictionary.
 * The caller runs this on Dispatchers.Default.
 */
object QuizChoiceSelector {
    private val whitespaceRegex = Regex("\\s+")
    private val trailingPunctuationRegex = Regex("[.!?;,:]+$")
    fun select(target: Word, words: List<Word>, count: Int = 4): List<Word> {
        if (count <= 0) return emptyList()

        val targetKey = normalize(target.en)
        val targetMeaning = normalize(target.tr)
        val sameLevel = mutableListOf<Word>()
        val otherLevels = mutableListOf<Word>()

        // A linear scan is predictable and cheap for ~27k records; random sampling
        // could miss valid options and make tests/results flaky.
        for (candidate in words) {
            val key = normalize(candidate.en)
            val meaning = normalize(candidate.tr)
            if (key.isBlank() || meaning.isBlank()) continue
            if (DictionaryEntryFilter.shouldExclude(candidate.en, candidate.tr)) continue
            if (key == targetKey || meaning == targetMeaning) continue
            if (candidate.level.equals(target.level, ignoreCase = true)) {
                sameLevel.add(candidate)
            } else {
                otherLevels.add(candidate)
            }
        }

        val result = mutableListOf(target)
        val usedWords = mutableSetOf(targetKey)
        val usedMeanings = mutableSetOf(targetMeaning)

        fun addUnique(candidates: List<Word>) {
            // Select only the few needed options instead of shuffling the entire pool.
            val remaining = candidates.toMutableList()
            while (remaining.isNotEmpty() && result.size < count) {
                val candidate = remaining.removeAt(kotlin.random.Random.nextInt(remaining.size))
                val key = normalize(candidate.en)
                val meaning = normalize(candidate.tr)
                if (key.isBlank() || meaning.isBlank()) continue
                if (key in usedWords || meaning in usedMeanings) continue
                result.add(candidate)
                usedWords.add(key)
                usedMeanings.add(meaning)
            }
        }

        addUnique(sameLevel.distinctBy { normalize(it.en) })
        if (result.size < count) addUnique(otherLevels.distinctBy { normalize(it.en) })
        return result.shuffled()
    }

    private fun normalize(value: String): String =
        value.trim().lowercase()
            .replace(whitespaceRegex, " ")
            .replace(trailingPunctuationRegex, "")
}
