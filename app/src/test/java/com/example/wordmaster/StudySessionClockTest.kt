package com.example.wordmaster

import org.junit.Assert.assertEquals
import org.junit.Test

class StudySessionClockTest {
    @Test
    fun shortSessionCountsAsOneMinute() {
        assertEquals(1, StudySessionClock.elapsedWholeMinutes(1_000L, 5_000L))
    }

    @Test
    fun roundsPartialMinutesUp() {
        assertEquals(2, StudySessionClock.elapsedWholeMinutes(0L, 60_001L))
    }

    @Test
    fun negativeElapsedTimeStillCountsAsOneMinute() {
        assertEquals(1, StudySessionClock.elapsedWholeMinutes(10_000L, 2_000L))
    }

    @Test
    fun longSessionIsCappedAtThreeHours() {
        assertEquals(180, StudySessionClock.elapsedWholeMinutes(0L, 10L * 60L * 60L * 1_000L))
    }
}
