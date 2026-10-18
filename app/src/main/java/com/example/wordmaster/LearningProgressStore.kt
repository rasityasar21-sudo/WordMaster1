package com.example.wordmaster

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate

/**
 * Persistent storage for WordMaster word progress.
 * Uses private app SharedPreferences and requires no account or network access.
 */
class LearningProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "wordmaster_learning_progress",
        Context.MODE_PRIVATE
    )

    @Synchronized
    fun all(): List<WordProgress> =
        WordMasterLearningEngine.decode(prefs.getString(KEY_WORDS, "[]"))

    @Synchronized
    fun find(word: String): WordProgress? =
        all().firstOrNull { it.word.equals(word.trim(), ignoreCase = true) }

    @Synchronized
    fun recordAnswer(word: String, correct: Boolean): WordProgress {
        val normalized = word.trim()
        require(normalized.isNotEmpty()) { "Word cannot be blank" }
        val current = all().toMutableList()
        val index = current.indexOfFirst { it.word.equals(normalized, ignoreCase = true) }
        val old = if (index >= 0) current[index] else WordProgress(word = normalized)
        // Quiz answers feed the same spaced-repetition schedule as explicit recall ratings.
        // A correct answer is a "good" recall (quality 4); a miss resets the streak (quality 1).
        val updated = old.reviewed(quality = if (correct) 4 else 1)
        if (index >= 0) current[index] = updated else current.add(updated)
        save(current)
        return updated
    }

    @Synchronized
    fun rateRecall(word: String, quality: Int): WordProgress {
        val normalized = word.trim()
        require(normalized.isNotEmpty()) { "Word cannot be blank" }
        val current = all().toMutableList()
        val index = current.indexOfFirst { it.word.equals(normalized, ignoreCase = true) }
        val old = if (index >= 0) current[index] else WordProgress(word = normalized)
        val updated = old.reviewed(quality)
        if (index >= 0) current[index] = updated else current.add(updated)
        save(current)
        return updated
    }

    fun dueForReview(): List<WordProgress> =
        WordMasterLearningEngine.dueForReview(all())

    fun summary(): LearningSummary {
        val streak = prefs.getInt(KEY_STREAK, 0)
        return WordMasterLearningEngine.summary(all(), streak)
    }

    @Synchronized
    fun recordStudyDay(date: LocalDate = LocalDate.now()) {
        val last = prefs.getString(KEY_LAST_STUDY_DAY, "").orEmpty()
        val yesterday = date.minusDays(1).toString()
        val current = prefs.getInt(KEY_STREAK, 0)
        val next = when {
            last == date.toString() -> current
            last == yesterday -> current + 1
            else -> 1
        }
        prefs.edit()
            .putString(KEY_LAST_STUDY_DAY, date.toString())
            .putInt(KEY_STREAK, next)
            .apply()
    }

    @Synchronized
    fun clearAll() {
        prefs.edit().remove(KEY_WORDS).remove(KEY_STREAK)
            .remove(KEY_LAST_STUDY_DAY).apply()
    }

    private fun save(words: List<WordProgress>) {
        prefs.edit().putString(KEY_WORDS, WordMasterLearningEngine.encode(words)).apply()
    }

    companion object {
        private const val KEY_WORDS = "word_progress_json"
        private const val KEY_STREAK = "study_streak"
        private const val KEY_LAST_STUDY_DAY = "last_study_day"
    }
}
