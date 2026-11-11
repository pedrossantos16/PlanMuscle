package com.pedro.planmuscle.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSchedulerTest {

    @Test
    fun `protocol week calculation returns correct week number capped at 8`() {
        val startDay = 20000L
        val week1Day = startDay + 3L
        val week2Day = startDay + 8L
        val week10Day = startDay + 70L // 10 weeks

        assertEquals(1, WorkoutScheduler.getProtocolWeek(startDay, week1Day))
        assertEquals(2, WorkoutScheduler.getProtocolWeek(startDay, week2Day))
        assertEquals(8, WorkoutScheduler.getProtocolWeek(startDay, week10Day))
    }

    @Test
    fun `isProgramCompleted returns true after 56 days`() {
        val startDay = 20000L
        val day55 = startDay + 55L
        val day56 = startDay + 56L

        assertFalse(WorkoutScheduler.isProgramCompleted(startDay, day55))
        assertTrue(WorkoutScheduler.isProgramCompleted(startDay, day56))
    }

    @Test
    fun `isTrainingDay returns true for Monday Wednesday Friday`() {
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
