package com.pedro.planmuscle.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrainingPhaseEngineTest {

    @Test
    fun `week 1 and 2 return phase 1 with 2 work sets override and RIR 3`() {
        val phaseWeek1 = TrainingPhaseEngine.getPhaseForWeek(1)
        assertEquals(1, phaseWeek1.weekNumber)
        assertEquals(2, phaseWeek1.workSetsOverride)
        assertEquals(3, phaseWeek1.targetRirMin)
        assertEquals(3, phaseWeek1.targetRirMax)

        val phaseWeek2 = TrainingPhaseEngine.getPhaseForWeek(2)
        assertEquals(2, phaseWeek2.weekNumber)
        assertEquals(2, phaseWeek2.workSetsOverride)
    }

    @Test
    fun `week 3 to 8 return phase 2 without sets override and RIR 1 to 2`() {
        val phaseWeek3 = TrainingPhaseEngine.getPhaseForWeek(3)
        assertEquals(3, phaseWeek3.weekNumber)
        assertNull(phaseWeek3.workSetsOverride)
        assertEquals(1, phaseWeek3.targetRirMin)
        assertEquals(2, phaseWeek3.targetRirMax)

        val phaseWeek8 = TrainingPhaseEngine.getPhaseForWeek(8)
        assertEquals(8, phaseWeek8.weekNumber)
        assertNull(phaseWeek8.workSetsOverride)
    }

    @Test
    fun `default exercises list contains 13 exercises with superset group`() {
        val exercises = TrainingPhaseEngine.defaultExercises
        assertEquals(13, exercises.size)

        val martelo = exercises.first { it.id == "rosca_martelo" }
        val triceps = exercises.first { it.id == "triceps_polia" }

        assertEquals("Bíceps/Braquial", martelo.primaryMuscleGroup)
        assertEquals("Tríceps", triceps.primaryMuscleGroup)
    }
}
