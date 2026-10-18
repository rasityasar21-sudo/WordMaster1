package com.example.wordmaster

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * WordMaster learning progress primitives.
 *
 * Review data is serialized as JSON so it can be stored in the app's existing
 * SharedPreferences layer without introducing a database dependency.
 */
data class WordProgress(
    val word: String,
    val correctCount: Int = 0,
    val mistakeCount: Int = 0,
    val streak: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val dueDate: String = LocalDate.now().toString(),
    val lastReviewed: String = ""
) {
    val accuracyPercent: Int
        get() = if (correctCount + mistakeCount == 0) 0
        else (correctCount * 100.0 / (correctCount + mistakeCount)).toInt()

    val isDue: Boolean
        get() = runCatching {
            dueDate.isBlank() || !LocalDate.parse(dueDate).isAfter(LocalDate.now())
        }.getOrDefault(true)

    /** Updates spaced-repetition scheduling after a 0–5 quality rating. */
    fun reviewed(quality: Int, today: LocalDate = LocalDate.now()): WordProgress {
        val q = quality.coerceIn(0, 5)
        val nextStreak = if (q >= 3) streak + 1 else 0
        val nextEase = max(
            1.3,
            easeFactor + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        )
        val nextInterval = when {
            q < 3 -> 1
            nextStreak == 1 -> 1
            nextStreak == 2 -> 3
            else -> max(1, (max(1, intervalDays) * nextEase).toInt())
        }
        return copy(
            correctCount = correctCount + if (q >= 3) 1 else 0,
            mistakeCount = mistakeCount + if (q < 3) 1 else 0,
            streak = nextStreak,
            intervalDays = nextInterval,
            easeFactor = nextEase,
            dueDate = today.plusDays(nextInterval.toLong()).toString(),
            lastReviewed = today.toString()
        )
    }

    fun toJson(): JSONObject = JSONObject()
        .put("word", word)
        .put("correctCount", correctCount)
        .put("mistakeCount", mistakeCount)
        .put("streak", streak)
        .put("intervalDays", intervalDays)
        .put("easeFactor", easeFactor)
        .put("dueDate", dueDate)
        .put("lastReviewed", lastReviewed)

    companion object {
        fun fromJson(json: JSONObject): WordProgress = WordProgress(
            word = json.optString("word").trim(),
            correctCount = json.optInt("correctCount").coerceAtLeast(0),
            mistakeCount = json.optInt("mistakeCount").coerceAtLeast(0),
            streak = json.optInt("streak").coerceAtLeast(0),
            intervalDays = json.optInt("intervalDays").coerceAtLeast(0),
            easeFactor = json.optDouble("easeFactor", 2.5)
                .let { if (it.isFinite()) it.coerceAtLeast(1.3) else 2.5 },
            dueDate = json.optString("dueDate", LocalDate.now().toString()),
            lastReviewed = json.optString("lastReviewed")
        )
    }
}

data class LearningSummary(
    val totalWords: Int,
    val masteredWords: Int,
    val dueWords: Int,
    val totalCorrect: Int,
    val totalMistakes: Int,
    val accuracyPercent: Int,
    val currentStreak: Int
)

/** Small, deterministic learning engine; persistence remains the UI layer's job. */
object WordMasterLearningEngine {
    fun decode(json: String?): List<WordProgress> = runCatching {
        val array = JSONArray(json ?: "[]")
        (0 until array.length()).mapNotNull { index ->
            runCatching { WordProgress.fromJson(array.getJSONObject(index)) }
                .getOrNull()
                ?.takeIf { it.word.isNotBlank() }
        }
    }.getOrDefault(emptyList())

    fun encode(words: List<WordProgress>): String =
        JSONArray().apply { words.forEach { put(it.toJson()) } }.toString()

    fun dueForReview(words: List<WordProgress>): List<WordProgress> =
        words.filter { it.isDue }.sortedWith(
            compareBy<WordProgress> { it.accuracyPercent }
                .thenBy { it.dueDate }
                .thenBy { it.word.lowercase() }
        )

    fun summary(words: List<WordProgress>, currentStreak: Int = 0): LearningSummary {
        val correct = words.sumOf { it.correctCount }
        val mistakes = words.sumOf { it.mistakeCount }
        val attempts = correct + mistakes
        return LearningSummary(
            totalWords = words.size,
            masteredWords = words.count { it.streak >= 3 && it.accuracyPercent >= 80 },
            dueWords = words.count { it.isDue },
            totalCorrect = correct,
            totalMistakes = mistakes,
            accuracyPercent = if (attempts == 0) 0 else (correct * 100.0 / attempts).toInt(),
            currentStreak = currentStreak
        )
    }
}
