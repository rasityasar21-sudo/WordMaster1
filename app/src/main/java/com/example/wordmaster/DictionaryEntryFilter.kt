package com.example.wordmaster

/**
 * Filters source-dictionary artifacts that are not useful standalone vocabulary.
 * Kept Android-free so the rules can be regression-tested with local JVM tests.
 */
internal object DictionaryEntryFilter {
    fun shouldExclude(english: String, turkish: String): Boolean {
        val en = english.trim()
        val tr = turkish.trim()
        if (en.isBlank() || tr.isBlank()) return true

        val isMorphologyFragment = en.startsWith("-") ||
            tr.contains("Suf.", ignoreCase = true) ||
            tr.contains("(morpheme)", ignoreCase = true)

        val looksLikeEnglishDefinition = tr.length > 65 &&
            tr.split(' ').size > 8 &&
            !tr.contains("ç") && !tr.contains("ğ") &&
            !tr.contains("ı") && !tr.contains("ö") &&
            !tr.contains("ş") && !tr.contains("ü") &&
            !tr.contains("/") && !tr.contains(",")

        val isOrdinalNoise = en.matches(Regex("(?i)(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:st|nd|rd|th)")) ||
            en.matches(Regex("\\d+(?:st|nd|rd|th)?"))

        // Definitions accidentally stored in the translation field pollute quizzes and matching games.
        val isDefinitionInTranslation = tr.contains("—") || tr.contains("–") ||
            Regex("(?i)^\\s*(?:n\\.|v\\.|adj\\.|adv\\.|pron\\.|prep\\.|conj\\.|interj\\.)").containsMatchIn(tr) ||
            Regex("(?i)^\\s*(?:the action of|to be |to do |a person who|an act of|the state of|used to describe)").containsMatchIn(tr)

        return isMorphologyFragment || looksLikeEnglishDefinition || isOrdinalNoise || isDefinitionInTranslation
    }

    /** Short, learner-friendly entries suitable for rapid matching and pronunciation drills. */
    fun isQuickPracticeEntry(english: String, turkish: String): Boolean {
        val en = english.trim()
        val tr = turkish.trim()
        if (shouldExclude(en, tr)) return false
        val wordCount = en.split(Regex("\\s+")).size
        if (en.length > 20 || wordCount > 2) return false
        if (!en.matches(Regex("(?i)[a-z]+(?:['’-][a-z]+)?(?:\\s+[a-z]+(?:['’-][a-z]+)?){0,1}"))) return false
        if (tr.length > 36 || tr.contains(';') || tr.contains('|')) return false
        if (tr.count { it == ',' } > 1) return false
        if (Regex("(?i)\\b(the action of|a person who|the state of|used to describe|a type of|a kind of)\\b").containsMatchIn(tr)) return false
        return true
    }
}
