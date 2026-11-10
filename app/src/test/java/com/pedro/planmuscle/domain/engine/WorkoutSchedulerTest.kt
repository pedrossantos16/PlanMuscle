package com.pedro.planmuscle.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSchedulerTest {

    @Test
    fun `protocol week calculation returns correct week number`() {
        val startDay = 20000L
        val week1Day = startDay + 3L
        val week2Day = startDay + 8L

        assertEquals(1, WorkoutScheduler.getProtocolWeek(startDay, week1Day))
        assertEquals(2, WorkoutScheduler.getProtocolWeek(startDay, week2Day))
    }

    @Test
    fun `isTrainingDay returns true for Monday Wednesday Friday`() {
        // Epoch day 20001 = Monday (2024-10-07)
        val mondayEpochDay = 20003L // Monday
        val tuesdayEpochDay = 20004L // Tuesday

        assertTrue(WorkoutScheduler.isTrainingDay(mondayEpochDay))
        assertFalse(WorkoutScheduler.isTrainingDay(tuesdayEpochDay))
    }

    @Test
    fun `determineNextWorkoutType alternates strictly based on last completed workout`() {
        assertEquals("B", WorkoutScheduler.determineNextWorkoutType("A"))
        assertEquals("A", WorkoutScheduler.determineNextWorkoutType("B"))
        assertEquals("A", WorkoutScheduler.determineNextWorkoutType(null))
    }
}
