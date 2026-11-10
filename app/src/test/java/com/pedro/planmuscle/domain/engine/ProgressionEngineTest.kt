package com.pedro.planmuscle.domain.engine

import com.pedro.planmuscle.data.local.entity.ExerciseSetLogEntity
import com.pedro.planmuscle.data.local.entity.SessionExercisePrescriptionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressionEngineTest {

    private val dummyPrescription = SessionExercisePrescriptionEntity(
        id = 1L,
        sessionId = 10L,
        exerciseId = "supino_reto",
        exerciseNameSnapshot = "Supino reto",
        orderIndex = 1,
        prescribedWorkSets = 3,
        minReps = 6,
        maxReps = 10,
        targetRirMin = 1,
        targetRirMax = 2,
        restSeconds = 150
    )

    @Test
    fun `when no set logs present returns INSUFFICIENT_DATA`() {
        val decision = ProgressionEngine.evaluateProgression(dummyPrescription, emptyList(), 2.0f)
        assertEquals(ProgressionDecisionType.INSUFFICIENT_DATA, decision.type)
    }

    @Test
    fun `when all sets hit max reps with good technique returns INCREASE_LOAD`() {
        val logs = listOf(
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 1, setType = "WORK", weightKg = 40f, reps = 10, rir = 1, techniqueOk = true),
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 2, setType = "WORK", weightKg = 40f, reps = 10, rir = 1, techniqueOk = true),
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 3, setType = "WORK", weightKg = 40f, reps = 10, rir = 2, techniqueOk = true)
        )

        val decision = ProgressionEngine.evaluateProgression(dummyPrescription, logs, 2.0f)
        assertEquals(ProgressionDecisionType.INCREASE_LOAD, decision.type)
        assertEquals(42.0f, decision.suggestedWeightKg)
    }

    @Test
    fun `when reps are within range but not max returns MAINTAIN_LOAD`() {
        val logs = listOf(
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 1, setType = "WORK", weightKg = 40f, reps = 10, rir = 1, techniqueOk = true),
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 2, setType = "WORK", weightKg = 40f, reps = 8, rir = 1, techniqueOk = true),
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 3, setType = "WORK", weightKg = 40f, reps = 7, rir = 1, techniqueOk = true)
        )

        val decision = ProgressionEngine.evaluateProgression(dummyPrescription, logs, 2.0f)
        assertEquals(ProgressionDecisionType.MAINTAIN_LOAD, decision.type)
        assertEquals(40.0f, decision.suggestedWeightKg)
    }

    @Test
    fun `when technique is poor returns REVIEW_TECHNIQUE`() {
        val logs = listOf(
            ExerciseSetLogEntity(sessionId = 10L, sessionExercisePrescriptionId = 1L, setIndex = 1, setType = "WORK", weightKg = 40f, reps = 10, rir = 1, techniqueOk = false)
        )

        val decision = ProgressionEngine.evaluateProgression(dummyPrescription, logs, 2.0f)
        assertEquals(ProgressionDecisionType.REVIEW_TECHNIQUE, decision.type)
    }
}
