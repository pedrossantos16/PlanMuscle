package com.pedro.planmuscle.data.repository

import com.pedro.planmuscle.data.PlanMuscleDatabase
import com.pedro.planmuscle.data.model.BodyMeasurementEntity
import com.pedro.planmuscle.data.model.DailyCheckInEntity
import com.pedro.planmuscle.data.model.ExerciseSetLogEntity
import com.pedro.planmuscle.data.model.MealCheckEntity
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.model.SupplementIntakeEntity
import com.pedro.planmuscle.data.model.WaterEntryEntity
import com.pedro.planmuscle.data.model.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanMuscleRepository(private val database: PlanMuscleDatabase) {

    private val protocolDao = database.protocolDao()
    private val dailyCheckInDao = database.dailyCheckInDao()
    private val bodyMeasurementDao = database.bodyMeasurementDao()
    private val waterEntryDao = database.waterEntryDao()
    private val workoutDao = database.workoutDao()
    private val nutritionDao = database.nutritionDao()

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Protocol
    fun getActiveProtocol(): Flow<ProtocolEntity?> = protocolDao.getActiveProtocol()
    fun getAllProtocols(): Flow<List<ProtocolEntity>> = protocolDao.getAllProtocols()

    suspend fun createNewProtocol(
        calorieTarget: Int,
        proteinTarget: Int = 150,
        carbTarget: Int = 330,
        fatTarget: Int = 70
    ) {
        val newProtocol = ProtocolEntity(
            startDate = getTodayDateString(),
            durationWeeks = 8,
            calorieTarget = calorieTarget,
            proteinTargetGrams = proteinTarget,
            carbTargetGrams = carbTarget,
            fatTargetGrams = fatTarget,
            waterTargetMl = 2600,
            isActive = true
        )
        val id = protocolDao.insertProtocol(newProtocol)
        protocolDao.deactivateOthers(id)
    }

    suspend fun ensureInitialProtocol() {
        // Will create default 2550 kcal protocol if none active
    }

    // Daily Check-in
    fun getDailyCheckIn(date: String): Flow<DailyCheckInEntity?> = dailyCheckInDao.getCheckIn(date)
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>> = dailyCheckInDao.getAllCheckIns()
    suspend fun saveCheckIn(checkIn: DailyCheckInEntity) = dailyCheckInDao.insertOrUpdateCheckIn(checkIn)

    // Body Measurements
    fun getBodyMeasurement(date: String): Flow<BodyMeasurementEntity?> = bodyMeasurementDao.getMeasurement(date)
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>> = bodyMeasurementDao.getAllMeasurements()
    suspend fun saveMeasurement(measurement: BodyMeasurementEntity) = bodyMeasurementDao.insertOrUpdateMeasurement(measurement)

    // Water
    fun getWaterEntriesForDate(date: String): Flow<List<WaterEntryEntity>> = waterEntryDao.getWaterEntriesForDate(date)
    fun getTotalWaterForDate(date: String): Flow<Int?> = waterEntryDao.getTotalWaterForDate(date)

    suspend fun addWater(date: String, amountMl: Int) {
        val entry = WaterEntryEntity(
            date = date,
            timestamp = System.currentTimeMillis(),
            amountMl = amountMl
        )
        waterEntryDao.insertWaterEntry(entry)
    }

    suspend fun deleteWater(id: Long) = waterEntryDao.deleteWaterEntry(id)

    // Workout Session & Logs
    fun getWorkoutSessionForDate(date: String): Flow<WorkoutSessionEntity?> = workoutDao.getSessionForDate(date)
    fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionEntity?> = workoutDao.getSessionById(sessionId)
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>> = workoutDao.getAllCompletedSessions()

    suspend fun startWorkoutSession(date: String, weekNumber: Int, workoutType: String): Long {
        val session = WorkoutSessionEntity(
            date = date,
            weekNumber = weekNumber,
            workoutType = workoutType,
            startedAtMillis = System.currentTimeMillis(),
            status = "IN_PROGRESS"
        )
        return workoutDao.insertSession(session)
    }

    suspend fun finishWorkoutSession(sessionId: Long) {
        workoutDao.updateSessionStatus(sessionId, "COMPLETED", System.currentTimeMillis())
    }

    fun getSetLogsForSession(sessionId: Long): Flow<List<ExerciseSetLogEntity>> = workoutDao.getSetLogsForSession(sessionId)
    fun getRecentSetLogsForExercise(exerciseId: String): Flow<List<ExerciseSetLogEntity>> = workoutDao.getRecentSetLogsForExercise(exerciseId)

    suspend fun saveSetLog(log: ExerciseSetLogEntity) = workoutDao.insertSetLog(log)
    suspend fun deleteSetLog(id: Long) = workoutDao.deleteSetLog(id)

    // Nutrition & Supplements
    fun getMealChecksForDate(date: String): Flow<List<MealCheckEntity>> = nutritionDao.getMealChecksForDate(date)
    suspend fun toggleMealCheck(date: String, mealId: String, completed: Boolean) {
        nutritionDao.insertOrUpdateMealCheck(MealCheckEntity(date, mealId, completed))
    }

    fun getSupplementIntakesForDate(date: String): Flow<List<SupplementIntakeEntity>> = nutritionDao.getSupplementIntakesForDate(date)
    suspend fun logSupplement(date: String, supplementId: String, amount: String) {
        val intake = SupplementIntakeEntity(
            date = date,
            timestamp = System.currentTimeMillis(),
            supplementId = supplementId,
            amount = amount
        )
        nutritionDao.insertSupplementIntake(intake)
    }
}
