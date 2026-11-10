package com.pedro.planmuscle.domain.model

data class TrainingProgramDomain(
    val id: Long,
    val name: String,
    val startDateEpochDay: Long,
    val durationWeeks: Int = 8,
    val status: String
)

data class NutritionTargetDomain(
    val id: Long,
    val calorieTarget: Int,
    val proteinTargetGrams: Int,
    val carbTargetGrams: Int,
    val fatTargetGrams: Int,
    val waterTargetMl: Int,
    val adjustmentReason: String
)

data class ExerciseDomain(
    val id: String,
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: String,
    val equipment: String,
    val isCompound: Boolean,
    val isUnilateral: Boolean,
    val defaultLoadIncrementKg: Float
)

data class WorkoutExerciseDomain(
    val id: Long,
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
    val prescribedSets: Int,
    val minReps: Int,
    val maxReps: Int,
    val restSeconds: Int,
    val warmupStrategy: String,
    val supersetGroupId: String?,
    val notes: String
)

data class WorkoutTemplateDomain(
    val id: Long,
    val workoutType: String,
    val title: String,
    val exercises: List<WorkoutExerciseDomain>
)

data class PlannedWorkoutDomain(
    val id: Long,
    val plannedDateEpochDay: Long,
    val workoutType: String,
    val status: String
)

data class ExerciseSetLogDomain(
    val id: Long,
    val sessionId: Long,
    val sessionExercisePrescriptionId: Long,
    val setIndex: Int,
    val setType: String, // "WARMUP", "WORK"
    val weightKg: Float,
    val reps: Int,
    val rir: Int?,
    val techniqueOk: Boolean,
    val completedAtEpochMillis: Long,
    val notes: String
)

data class SessionExercisePrescriptionDomain(
    val id: Long,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseNameSnapshot: String,
    val orderIndex: Int,
    val prescribedWorkSets: Int,
    val minReps: Int,
    val maxReps: Int,
    val targetRirMin: Int,
    val targetRirMax: Int,
    val restSeconds: Int,
    val warmupGuidanceSnapshot: String,
    val supersetGroupId: String?,
    val setLogs: List<ExerciseSetLogDomain> = emptyList()
)

data class WorkoutSessionDomain(
    val id: Long,
    val workoutType: String,
    val protocolWeek: Int,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long?,
    val status: String,
    val notes: String,
    val prescriptions: List<SessionExercisePrescriptionDomain> = emptyList()
)

data class DailyCheckInDomain(
    val dateEpochDay: Long,
    val weightKg: Float?,
    val readinessLevel: Int?,
    val sleepMinutes: Int?,
    val sleepQuality: Int?,
    val unusualPain: Boolean,
    val notes: String
)

data class BodyMeasurementDomain(
    val dateEpochDay: Long,
    val waistCm: Float?,
    val armCm: Float?,
    val thighCm: Float?,
    val chestCm: Float?,
    val measurementNotes: String
)

data class WaterEntryDomain(
    val id: Long,
    val dateEpochDay: Long,
    val timestampEpochMillis: Long,
    val amountMl: Int
)

data class MealCheckDomain(
    val dateEpochDay: Long,
    val mealId: String,
    val status: String,
    val notes: String
)

data class SupplementIntakeDomain(
    val id: Long,
    val dateEpochDay: Long,
    val supplementId: String,
    val timestampEpochMillis: Long,
    val amount: String
)

data class BiweeklyReviewDomain(
    val id: Long,
    val trainingProgramId: Long,
    val periodStartEpochDay: Long,
    val periodEndEpochDay: Long,
    val status: String,
    val averageWeightFirstWeek: Float?,
    val averageWeightSecondWeek: Float?,
    val weeklyWeightChangeKg: Float?,
    val waistChangeCm: Float?,
    val averageReadiness: Float?,
    val scheduledWorkouts: Int,
    val completedWorkouts: Int,
    val trainingAdherence: Float,
    val mealPlanAdherence: Float?,
    val progressionSummary: String,
    val recommendationType: String,
    val calorieAdjustment: Int,
    val rationale: String,
    val decidedAtEpochMillis: Long?
)
