package com.pedro.planmuscle.domain.repository

import com.pedro.planmuscle.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface PlanMuscleRepository {
    fun getTodayEpochDay(): Long
    fun getTodayDateString(): String

    // Program & Seed
    fun getActiveProgram(): Flow<TrainingProgramEntity?>
    suspend fun ensureActiveProgramSeeded(startDateEpochDay: Long = getTodayEpochDay()): Long
    suspend fun startNewProgramCycle(): Long

    // Nutrition Target
    fun getActiveNutritionTarget(): Flow<NutritionTargetEntity?>
    suspend fun updateNutritionTarget(calories: Int, protein: Int, carbs: Int, fats: Int, waterMl: Int, reason: String)

    // Workouts & Session Creation
    suspend fun startOrResumeWorkoutSession(targetDateEpochDay: Long = getTodayEpochDay()): Long
    fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionEntity?>
    suspend fun finishWorkoutSession(sessionId: Long, notes: String = "")
    suspend fun abandonWorkoutSession(sessionId: Long)

    // Prescriptions & Set Logs
    fun getPrescriptionsForSession(sessionId: Long): Flow<List<SessionExercisePrescriptionEntity>>
    fun getSetLogsForSession(sessionId: Long): Flow<List<ExerciseSetLogEntity>>
    suspend fun saveSetLog(log: ExerciseSetLogEntity)
    suspend fun deleteSetLog(id: Long)
    suspend fun getLastCompletedSetLogsForExercise(exerciseId: String): List<ExerciseSetLogEntity>

    // Check-in & Measurements
    fun getDailyCheckIn(dateEpochDay: Long): Flow<DailyCheckInEntity?>
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>
    suspend fun saveCheckIn(checkIn: DailyCheckInEntity)

    fun getBodyMeasurement(dateEpochDay: Long): Flow<BodyMeasurementEntity?>
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>
    suspend fun saveMeasurement(measurement: BodyMeasurementEntity)

    // Water
    fun getWaterEntriesForDate(dateEpochDay: Long): Flow<List<WaterEntryEntity>>
    fun getTotalWaterForDate(dateEpochDay: Long): Flow<Int?>
    suspend fun addWater(dateEpochDay: Long, amountMl: Int)
    suspend fun deleteLastWaterEntry(dateEpochDay: Long)

    // Meal Checks & Supplements
    fun getMealChecksForDate(dateEpochDay: Long): Flow<List<MealCheckEntity>>
    suspend fun updateMealCheck(dateEpochDay: Long, mealId: String, status: String)

    fun getSupplementIntakesForDate(dateEpochDay: Long): Flow<List<SupplementIntakeEntity>>
    suspend fun toggleSupplementIntake(dateEpochDay: Long, supplementId: String, amount: String)

    // Bi-weekly Reviews
    fun getPendingReview(programId: Long): Flow<BiweeklyReviewEntity?>
    suspend fun generateBiweeklyReviewIfDue(programId: Long): BiweeklyReviewEntity?
    suspend fun acceptReviewAdjustment(reviewId: Long)
    suspend fun rejectReview(reviewId: Long)

    // Custom Exercises & Templates
    fun getAllExercises(): Flow<List<ExerciseEntity>>
    fun getWorkoutTemplates(programId: Long): Flow<List<WorkoutTemplateEntity>>
    fun getTemplateExercises(templateId: Long): Flow<List<WorkoutTemplateExerciseEntity>>
    suspend fun saveCustomExerciseAndTemplate(
        workoutTemplateId: Long,
        exerciseId: String,
        name: String,
        workSets: Int,
        minReps: Int,
        maxReps: Int,
        restSeconds: Int,
        warmupGuidance: String,
        loadIncrementKg: Float
    )

    // Export & Backup
    suspend fun exportDataAsJson(): String
    suspend fun importDataFromJson(json: String): Boolean
    suspend fun exportWorkoutsAndWeightsCsv(): String
    suspend fun clearAllData()
}
