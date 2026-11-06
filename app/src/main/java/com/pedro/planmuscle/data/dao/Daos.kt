package com.pedro.planmuscle.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pedro.planmuscle.data.model.BodyMeasurementEntity
import com.pedro.planmuscle.data.model.DailyCheckInEntity
import com.pedro.planmuscle.data.model.ExerciseSetLogEntity
import com.pedro.planmuscle.data.model.MealCheckEntity
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.model.SupplementIntakeEntity
import com.pedro.planmuscle.data.model.WaterEntryEntity
import com.pedro.planmuscle.data.model.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {
    @Query("SELECT * FROM protocols WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActiveProtocol(): Flow<ProtocolEntity?>

    @Query("SELECT * FROM protocols ORDER BY id DESC")
    fun getAllProtocols(): Flow<List<ProtocolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProtocol(protocol: ProtocolEntity): Long

    @Query("UPDATE protocols SET isActive = 0 WHERE id != :activeId")
    suspend fun deactivateOthers(activeId: Long)
}

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_checkins WHERE date = :date")
    fun getCheckIn(date: String): Flow<DailyCheckInEntity?>

    @Query("SELECT * FROM daily_checkins ORDER BY date DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCheckIn(checkIn: DailyCheckInEntity)
}

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements WHERE date = :date")
    fun getMeasurement(date: String): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMeasurement(measurement: BodyMeasurementEntity)
}

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getWaterEntriesForDate(date: String): Flow<List<WaterEntryEntity>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE date = :date")
    fun getTotalWaterForDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntryEntity)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteWaterEntry(id: Long)
}

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sessions WHERE date = :date ORDER BY id DESC LIMIT 1")
    fun getSessionForDate(date: String): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY date DESC")
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("UPDATE workout_sessions SET status = :status, finishedAtMillis = :finishedAt WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: String, finishedAt: Long)

    @Query("SELECT * FROM exercise_set_logs WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getSetLogsForSession(sessionId: Long): Flow<List<ExerciseSetLogEntity>>

    @Query("SELECT * FROM exercise_set_logs WHERE exerciseId = :exerciseId AND setType = 'WORK' ORDER BY id DESC LIMIT 10")
    fun getRecentSetLogsForExercise(exerciseId: String): Flow<List<ExerciseSetLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(log: ExerciseSetLogEntity)

    @Query("DELETE FROM exercise_set_logs WHERE id = :id")
    suspend fun deleteSetLog(id: Long)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM meal_checks WHERE date = :date")
    fun getMealChecksForDate(date: String): Flow<List<MealCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMealCheck(check: MealCheckEntity)

    @Query("SELECT * FROM supplement_intakes WHERE date = :date")
    fun getSupplementIntakesForDate(date: String): Flow<List<SupplementIntakeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementIntake(intake: SupplementIntakeEntity)
}
