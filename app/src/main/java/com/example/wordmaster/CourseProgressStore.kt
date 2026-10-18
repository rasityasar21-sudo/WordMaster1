package com.example.wordmaster

import android.content.SharedPreferences

/**
 * Persistence helpers for course progress.
 *
 * Uses the existing "completedLessons" preference key so older WordMaster progress
 * remains compatible. Call these helpers from lesson completion and profile UI.
 */
object CourseProgressStore {
    private const val COMPLETED_KEY = "completedLessons"
    private const val DAILY_GOAL_KEY = "dailyGoalMinutes"
    private const val DAILY_MINUTES_PREFIX = "studyMinutes_"

    fun completedIds(prefs: SharedPreferences): Set<String> =
        prefs.getStringSet(COMPLETED_KEY, emptySet())?.toSet() ?: emptySet()

    fun markCompleted(prefs: SharedPreferences, lessonId: String): Set<String> {
        require(lessonId.isNotBlank()) { "lessonId must not be blank" }
        val updated = completedIds(prefs) + lessonId
        prefs.edit().putStringSet(COMPLETED_KEY, updated).apply()
        return updated
    }

    fun isCompleted(prefs: SharedPreferences, lessonId: String): Boolean =
        lessonId in completedIds(prefs)

    /** Lessons that are unlocked and not yet completed for the chosen level. */
    fun availableLessons(prefs: SharedPreferences, level: String): List<CourseLesson> =
        WordMasterCourseCatalog.available(completedIds(prefs), level)

    /** The next unlocked lesson, if one remains at this level. */
    fun nextRecommendedLesson(prefs: SharedPreferences, level: String): CourseLesson? =
        WordMasterCourseCatalog.nextRecommended(completedIds(prefs), level)

    /** Returns completed and total lesson counts for a level. */
    fun levelProgress(prefs: SharedPreferences, level: String): Pair<Int, Int> {
        val lessons = WordMasterCourseCatalog.forLevel(level)
        val done = completedIds(prefs).intersect(lessons.map { it.id }.toSet()).size
        return done to lessons.size
    }

    /** Clears course completion only; daily goal and study history are preserved. */
    fun clearCompletedLessons(prefs: SharedPreferences) {
        prefs.edit().remove(COMPLETED_KEY).apply()
    }

    fun completedCount(prefs: SharedPreferences): Int = completedIds(prefs).size

    fun setDailyGoalMinutes(prefs: SharedPreferences, minutes: Int) {
        prefs.edit().putInt(DAILY_GOAL_KEY, minutes.coerceIn(5, 120)).apply()
    }

    fun dailyGoalMinutes(prefs: SharedPreferences): Int =
        prefs.getInt(DAILY_GOAL_KEY, 15).coerceIn(5, 120)

    fun addStudyMinutes(
        prefs: SharedPreferences,
        minutes: Int,
        date: String = java.time.LocalDate.now().toString()
    ): Int {
        require(minutes >= 0) { "minutes must be non-negative" }
        val key = DAILY_MINUTES_PREFIX + date
        val current = prefs.getInt(key, 0).coerceIn(0, 24 * 60)
        // Add in Long arithmetic so a very large input cannot overflow Int
        // and accidentally turn the recorded total negative.
        val updated = (current.toLong() + minutes.toLong())
            .coerceAtMost((24 * 60).toLong())
            .toInt()
        prefs.edit().putInt(key, updated).apply()
        return updated
    }

    fun studyMinutes(
        prefs: SharedPreferences,
        date: String = java.time.LocalDate.now().toString()
    ): Int = prefs.getInt(DAILY_MINUTES_PREFIX + date, 0)

    fun isDailyGoalMet(
        prefs: SharedPreferences,
        date: String = java.time.LocalDate.now().toString()
    ): Boolean = studyMinutes(prefs, date) >= dailyGoalMinutes(prefs)
}
