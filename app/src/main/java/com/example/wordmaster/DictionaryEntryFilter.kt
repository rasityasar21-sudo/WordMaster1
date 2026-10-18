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

        // Dictionary-source morphemes such as "-gen" and "-gram" are not
        // standalone vocabulary for Turkish learners.
        val isMorphologyFragment = en.startsWith("-") ||
            tr.contains("Suf.", ignoreCase = true) ||
            tr.contains("(morpheme)", ignoreCase = true)

        val looksLikeEnglishDefinition = tr.length > 65 &&
            tr.split(' ').size > 8 &&
            !tr.contains("ç", true) && !tr.contains("ğ", true) &&
            !tr.contains("ı", true) && !tr.contains("ö", true) &&
            !tr.contains("ş", true) && !tr.contains("ü", true) &&
            !tr.contains("/") && !tr.contains(",")

        // Exclude low-value ordinal abbreviations and number-like dictionary noise.
        val isOrdinalNoise = en.matches(Regex("(?i)(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:st|nd|rd|th)")) ||
            en.matches(Regex("\\d+(?:st|nd|rd|th)?"))

        return isMorphologyFragment || looksLikeEnglishDefinition || isOrdinalNoise
    }
}
