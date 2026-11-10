package com.pedro.planmuscle.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_programs",
    indices = [Index(value = ["status"])]
)
data class TrainingProgramEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startDateEpochDay: Long,
    val durationWeeks: Int = 8,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED", "ARCHIVED"
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "nutrition_targets",
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingProgramId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["trainingProgramId"]), Index(value = ["validFromEpochDay"])]
)
data class NutritionTargetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingProgramId: Long? = null,
    val validFromEpochDay: Long,
    val validUntilEpochDay: Long? = null,
    val calorieTarget: Int = 2550,
    val proteinTargetGrams: Int = 150,
    val carbTargetGrams: Int = 330,
    val fatTargetGrams: Int = 70,
    val waterTargetMl: Int = 2600,
    val adjustmentReason: String = "Metas iniciais do protocolo",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercises",
    indices = [Index(value = ["active"])]
)
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val primaryMuscleGroup: String,
    val secondaryMuscleGroups: String = "",
    val equipment: String = "",
    val isCompound: Boolean = true,
    val isUnilateral: Boolean = false,
    val defaultLoadIncrementKg: Float = 2.0f,
    val active: Boolean = true
)

@Entity(
    tableName = "workout_templates",
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingProgramId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trainingProgramId"]), Index(value = ["workoutType"])]
)
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingProgramId: Long,
    val workoutType: String, // "A" ou "B"
    val title: String,
    val version: Int = 1,
    val active: Boolean = true
)

@Entity(
    tableName = "workout_template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutTemplateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutTemplateId"]), Index(value = ["exerciseId"])]
)
data class WorkoutTemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutTemplateId: Long,
    val exerciseId: String,
    val orderIndex: Int,
    val prescribedSets: Int,
    val minReps: Int,
    val maxReps: Int,
    val restSeconds: Int,
    val warmupStrategy: String = "",
    val supersetGroupId: String? = null,
    val notes: String = ""
)

@Entity(
    tableName = "planned_workouts",
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingProgramId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trainingProgramId"]), Index(value = ["plannedDateEpochDay"])]
)
data class PlannedWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingProgramId: Long,
    val plannedDateEpochDay: Long,
    val workoutType: String, // "A" ou "B"
    val status: String = "PLANNED", // "PLANNED", "COMPLETED", "SKIPPED", "RESCHEDULED"
    val completedSessionId: Long? = null
)

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingProgramId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trainingProgramId"]), Index(value = ["status"]), Index(value = ["startedAtEpochMillis"])]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingProgramId: Long,
    val plannedWorkoutId: Long? = null,
    val workoutType: String, // "A" ou "B"
    val protocolWeek: Int,
    val startedAtEpochMillis: Long,
    val finishedAtEpochMillis: Long? = null,
    val status: String = "IN_PROGRESS", // "IN_PROGRESS", "COMPLETED", "ABANDONED"
    val notes: String = ""
)

@Entity(
    tableName = "session_exercise_prescriptions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"]), Index(value = ["exerciseId"])]
)
data class SessionExercisePrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val warmupGuidanceSnapshot: String = "",
    val supersetGroupId: String? = null
)

@Entity(
    tableName = "exercise_set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SessionExercisePrescriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionExercisePrescriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["sessionExercisePrescriptionId"]),
        Index(value = ["sessionExercisePrescriptionId", "setIndex"], unique = true)
    ]
)
data class ExerciseSetLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val sessionExercisePrescriptionId: Long,
    val setIndex: Int,
    val setType: String = "WORK", // "WARMUP", "WORK"
    val weightKg: Float,
    val reps: Int,
    val rir: Int? = null,
    val techniqueOk: Boolean = true,
    val completedAtEpochMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "daily_checkins")
data class DailyCheckInEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val weightKg: Float? = null,
    val readinessLevel: Int? = null, // 1 a 5
    val sleepMinutes: Int? = null,
    val sleepQuality: Int? = null, // 1 a 5
    val unusualPain: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val waistCm: Float? = null,
    val armCm: Float? = null,
    val thighCm: Float? = null,
    val chestCm: Float? = null,
    val measurementNotes: String = ""
)

@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["dateEpochDay"])]
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val timestampEpochMillis: Long,
    val amountMl: Int
)

@Entity(
    tableName = "meal_checks",
    primaryKeys = ["dateEpochDay", "mealId"],
    indices = [Index(value = ["dateEpochDay"])]
)
data class MealCheckEntity(
    val dateEpochDay: Long,
    val mealId: String, // "BREAKFAST", "MORNING_SNACK", "LUNCH", "PRE_WORKOUT", "SENAI_SNACK", "DINNER"
    val status: String = "COMPLETED_AS_PLANNED", // "PLANNED", "COMPLETED_AS_PLANNED", "PARTIAL", "SKIPPED"
    val notes: String = ""
)

@Entity(
    tableName = "supplement_intakes",
    indices = [Index(value = ["dateEpochDay", "supplementId"], unique = true)]
)
data class SupplementIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val supplementId: String, // "CREATINE", "WHEY", "OMEGA3"
    val timestampEpochMillis: Long,
    val amount: String
)

@Entity(
    tableName = "biweekly_reviews",
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["trainingProgramId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trainingProgramId"])]
)
data class BiweeklyReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trainingProgramId: Long,
    val periodStartEpochDay: Long,
    val periodEndEpochDay: Long,
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED", "NOT_ENOUGH_DATA"
    val averageWeightFirstWeek: Float? = null,
    val averageWeightSecondWeek: Float? = null,
    val weeklyWeightChangeKg: Float? = null,
    val waistChangeCm: Float? = null,
    val averageReadiness: Float? = null,
    val scheduledWorkouts: Int = 6,
    val completedWorkouts: Int = 0,
    val trainingAdherence: Float = 0.0f,
    val mealPlanAdherence: Float? = null,
    val progressionSummary: String = "",
    val recommendationType: String = "MAINTAIN",
    val calorieAdjustment: Int = 0,
    val rationale: String = "",
    val decidedAtEpochMillis: Long? = null
)
