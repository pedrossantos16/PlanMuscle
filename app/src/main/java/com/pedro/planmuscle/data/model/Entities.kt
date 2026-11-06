package com.pedro.planmuscle.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocols")
data class ProtocolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: String,
    val durationWeeks: Int = 8,
    val calorieTarget: Int = 2550,
    val proteinTargetGrams: Int = 150,
    val carbTargetGrams: Int = 330,
    val fatTargetGrams: Int = 70,
    val waterTargetMl: Int = 2600,
    val isActive: Boolean = true
)

@Entity(tableName = "daily_checkins", primaryKeys = ["date"])
data class DailyCheckInEntity(
    val date: String, // yyyy-MM-dd
    val weightKg: Float? = null,
    val readinessLevel: Int? = null, // 1 (cansado) a 5 (ótima disposição)
    val sleepHours: Float? = null,
    val sleepQuality: Int? = null, // 1 a 5
    val notes: String = ""
)

@Entity(tableName = "body_measurements", primaryKeys = ["date"])
data class BodyMeasurementEntity(
    val date: String, // yyyy-MM-dd
    val waistCm: Float? = null,
    val armCm: Float? = null,
    val thighCm: Float? = null,
    val chestCm: Float? = null
)

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val timestamp: Long,
    val amountMl: Int
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val weekNumber: Int,
    val workoutType: String, // "A" ou "B"
    val startedAtMillis: Long,
    val finishedAtMillis: Long? = null,
    val status: String = "IN_PROGRESS" // "IN_PROGRESS", "COMPLETED"
)

@Entity(tableName = "exercise_set_logs")
data class ExerciseSetLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val setNumber: Int,
    val setType: String = "WORK", // "WARMUP", "WORK"
    val weightKg: Float,
    val reps: Int,
    val rir: Int? = null
)

@Entity(tableName = "meal_checks", primaryKeys = ["date", "mealId"])
data class MealCheckEntity(
    val date: String, // yyyy-MM-dd
    val mealId: String, // "BREAKFAST", "MORNING_SNACK", "LUNCH", "PRE_WORKOUT", "SENAI_SNACK", "DINNER"
    val completed: Boolean = false
)

@Entity(tableName = "supplement_intakes")
data class SupplementIntakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // yyyy-MM-dd
    val timestamp: Long,
    val supplementId: String, // "CREATINE", "WHEY", "OMEGA3"
    val amount: String
)
