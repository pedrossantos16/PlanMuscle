package com.pedro.planmuscle.domain.engine

import com.pedro.planmuscle.data.local.entity.DailyCheckInEntity
import com.pedro.planmuscle.data.local.entity.WorkoutSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class BiweeklyReviewEngineTest {

    @Test
    fun `when weigh-ins are insufficient returns NOT_ENOUGH_DATA`() {
        val review = BiweeklyReviewEngine.generateReview(
            programId = 1L,
            periodStartEpochDay = 20000L,
            periodEndEpochDay = 20013L,
            checkIns = emptyList(),
            completedSessions = emptyList(),
            activeCalories = 2550
        )

        assertEquals("NOT_ENOUGH_DATA", review.status)
        assertEquals("COLLECT_MORE_DATA", review.recommendationType)
    }

    @Test
    fun `when weight is stable with high training adherence recommends INCREASE_150_KCAL`() {
        val checkIns = listOf(
            DailyCheckInEntity(dateEpochDay = 20000L, weightKg = 76.0f),
            DailyCheckInEntity(dateEpochDay = 20002L, weightKg = 76.0f),
            DailyCheckInEntity(dateEpochDay = 20004L, weightKg = 76.0f),
            DailyCheckInEntity(dateEpochDay = 20008L, weightKg = 76.0f),
            DailyCheckInEntity(dateEpochDay = 20010L, weightKg = 76.0f),
            DailyCheckInEntity(dateEpochDay = 20012L, weightKg = 76.0f)
        )

        val sessions = listOf(
            WorkoutSessionEntity(id = 1, trainingProgramId = 1L, workoutType = "A", protocolWeek = 1, startedAtEpochMillis = 20000L * 86400000L, status = "COMPLETED"),
            WorkoutSessionEntity(id = 2, trainingProgramId = 1L, workoutType = "B", protocolWeek = 1, startedAtEpochMillis = 20002L * 86400000L, status = "COMPLETED"),
            WorkoutSessionEntity(id = 3, trainingProgramId = 1L, workoutType = "A", protocolWeek = 1, startedAtEpochMillis = 20004L * 86400000L, status = "COMPLETED"),
            WorkoutSessionEntity(id = 4, trainingProgramId = 1L, workoutType = "B", protocolWeek = 2, startedAtEpochMillis = 20007L * 86400000L, status = "COMPLETED"),
            WorkoutSessionEntity(id = 5, trainingProgramId = 1L, workoutType = "A", protocolWeek = 2, startedAtEpochMillis = 20009L * 86400000L, status = "COMPLETED"),
            WorkoutSessionEntity(id = 6, trainingProgramId = 1L, workoutType = "B", protocolWeek = 2, startedAtEpochMillis = 20011L * 86400000L, status = "COMPLETED")
        )

        val review = BiweeklyReviewEngine.generateReview(
            programId = 1L,
            periodStartEpochDay = 20000L,
            periodEndEpochDay = 20013L,
            checkIns = checkIns,
            completedSessions = sessions,
            activeCalories = 2550
        )

        assertEquals("PENDING", review.status)
        assertEquals("INCREASE_150_KCAL", review.recommendationType)
        assertEquals(150, review.calorieAdjustment)
    }
}
