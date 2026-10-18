package com.example.wordmaster

/**
 * Calculates a bounded whole-minute duration for a completed lesson session.
 * Very short sessions count as one minute; implausibly long sessions are capped.
 */
object StudySessionClock {
    fun elapsedWholeMinutes(startedAtMillis: Long, endedAtMillis: Long): Int {
        val elapsedMillis = (endedAtMillis - startedAtMillis).coerceAtLeast(0L)
        val roundedMinutes = ((elapsedMillis + 59_999L) / 60_000L).toInt()
        return roundedMinutes.coerceIn(1, 180)
    }
}
