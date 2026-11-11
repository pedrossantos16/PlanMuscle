package com.pedro.planmuscle.data.local.dao

import androidx.room.*
import com.pedro.planmuscle.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingProgramDao {
    @Query("SELECT * FROM training_programs WHERE status = 'ACTIVE' ORDER BY id DESC LIMIT 1")
    fun getActiveProgram(): Flow<TrainingProgramEntity?>

    @Query("SELECT * FROM training_programs WHERE status = 'ACTIVE' ORDER BY id DESC LIMIT 1")
    suspend fun getActiveProgramOneShot(): TrainingProgramEntity?

    @Query("SELECT * FROM training_programs ORDER BY id DESC")
    fun getAllPrograms(): Flow<List<TrainingProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: TrainingProgramEntity): Long

    @Query("UPDATE training_programs SET status = :status WHERE id = :id")
    suspend fun updateProgramStatus(id: Long, status: String)
}

@Dao
interface NutritionTargetDao {
    @Query("SELECT * FROM nutrition_targets WHERE validUntilEpochDay IS NULL ORDER BY id DESC LIMIT 1")
    fun getActiveTarget(): Flow<NutritionTargetEntity?>

    @Query("SELECT * FROM nutrition_targets WHERE validUntilEpochDay IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getActiveTargetOneShot(): NutritionTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: NutritionTargetEntity): Long

    @Query("UPDATE nutrition_targets SET validUntilEpochDay = :untilEpochDay WHERE validUntilEpochDay IS NULL AND id != :currentId")
    suspend fun invalidatePreviousTargets(untilEpochDay: Long, currentId: Long)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE active = 1 ORDER BY name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)
}

@Dao
interface WorkoutTemplateDao {
    @Query("SELECT * FROM workout_templates WHERE trainingProgramId = :programId AND active = 1")
    fun getTemplatesForProgram(programId: Long): Flow<List<WorkoutTemplateEntity>>

    @Query("SELECT * FROM workout_templates WHERE trainingProgramId = :programId AND active = 1")
    suspend fun getTemplatesForProgramOneShot(programId: Long): List<WorkoutTemplateEntity>

    @Query("SELECT * FROM workout_template_exercises WHERE workoutTemplateId = :templateId ORDER BY orderIndex ASC")
    fun getTemplateExercises(templateId: Long): Flow<List<WorkoutTemplateExerciseEntity>>

    @Query("SELECT * FROM workout_template_exercises WHERE workoutTemplateId = :templateId ORDER BY orderIndex ASC")
    suspend fun getTemplateExercisesOneShot(templateId: Long): List<WorkoutTemplateExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(exercises: List<WorkoutTemplateExerciseEntity>)

    @Query("""
        UPDATE workout_template_exercises 
        SET prescribedSets = :workSets, minReps = :minReps, maxReps = :maxReps, restSeconds = :restSeconds, warmupStrategy = :warmupGuidance 
        WHERE exerciseId = :exerciseId AND workoutTemplateId IN (SELECT id FROM workout_templates WHERE trainingProgramId = :programId)
    """)
    suspend fun updateTemplateExercise(programId: Long, exerciseId: String, workSets: Int, minReps: Int, maxReps: Int, restSeconds: Int, warmupGuidance: String)
}

@Dao
interface PlannedWorkoutDao {
    @Query("SELECT * FROM planned_workouts WHERE trainingProgramId = :programId AND plannedDateEpochDay = :dateEpochDay LIMIT 1")
    fun getPlannedWorkoutForDate(programId: Long, dateEpochDay: Long): Flow<PlannedWorkoutEntity?>

    @Query("SELECT * FROM planned_workouts WHERE trainingProgramId = :programId AND plannedDateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getPlannedWorkoutForDateOneShot(programId: Long, dateEpochDay: Long): PlannedWorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedWorkout(planned: PlannedWorkoutEntity): Long

    @Query("UPDATE planned_workouts SET status = :status, completedSessionId = :sessionId WHERE id = :id")
    suspend fun updatePlannedWorkoutStatus(id: Long, status: String, sessionId: Long?)
}

@Dao
interface WorkoutSessionDao {
    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' ORDER BY id DESC LIMIT 1")
    suspend fun getInProgressSession(): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getSessionByIdOneShot(sessionId: Long): WorkoutSessionEntity?

    @Query("SELECT * FROM workout_sessions WHERE trainingProgramId = :programId AND status = 'COMPLETED' ORDER BY startedAtEpochMillis DESC")
    fun getCompletedSessionsForProgram(programId: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE trainingProgramId = :programId AND status = 'COMPLETED' ORDER BY startedAtEpochMillis DESC")
    suspend fun getCompletedSessionsOneShot(programId: Long): List<WorkoutSessionEntity>

    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY startedAtEpochMillis DESC")
    fun getAllCompletedSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("UPDATE workout_sessions SET status = :status, finishedAtEpochMillis = :finishedAtMillis WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: String, finishedAtMillis: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptions(prescriptions: List<SessionExercisePrescriptionEntity>)

    @Query("SELECT * FROM session_exercise_prescriptions WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    fun getPrescriptionsForSession(sessionId: Long): Flow<List<SessionExercisePrescriptionEntity>>

    @Query("SELECT * FROM session_exercise_prescriptions WHERE sessionId = :sessionId ORDER BY orderIndex ASC")
    suspend fun getPrescriptionsForSessionOneShot(sessionId: Long): List<SessionExercisePrescriptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLog(log: ExerciseSetLogEntity)

    @Query("DELETE FROM exercise_set_logs WHERE id = :id")
    suspend fun deleteSetLog(id: Long)

    @Query("SELECT * FROM exercise_set_logs WHERE sessionId = :sessionId ORDER BY setIndex ASC")
    fun getSetLogsForSession(sessionId: Long): Flow<List<ExerciseSetLogEntity>>

    @Query("SELECT * FROM exercise_set_logs WHERE sessionId = :sessionId ORDER BY setIndex ASC")
    suspend fun getSetLogsForSessionOneShot(sessionId: Long): List<ExerciseSetLogEntity>

    @Query("""
        SELECT l.* FROM exercise_set_logs l 
        INNER JOIN session_exercise_prescriptions p ON l.sessionExercisePrescriptionId = p.id 
        WHERE p.exerciseId = :exerciseId 
          AND l.setType = 'WORK' 
          AND l.sessionId = (
              SELECT s.id FROM workout_sessions s 
              INNER JOIN session_exercise_prescriptions p2 ON p2.sessionId = s.id 
              WHERE s.status = 'COMPLETED' AND p2.exerciseId = :exerciseId 
              ORDER BY s.startedAtEpochMillis DESC LIMIT 1
          ) 
        ORDER BY l.setIndex ASC
    """)
    suspend fun getLastCompletedSetLogsForExercise(exerciseId: String): List<ExerciseSetLogEntity>
}

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_checkins WHERE dateEpochDay = :dateEpochDay")
    fun getCheckIn(dateEpochDay: Long): Flow<DailyCheckInEntity?>

    @Query("SELECT * FROM daily_checkins WHERE dateEpochDay = :dateEpochDay")
    suspend fun getCheckInOneShot(dateEpochDay: Long): DailyCheckInEntity?

    @Query("SELECT * FROM daily_checkins WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    suspend fun getCheckInsInRangeOneShot(startEpochDay: Long, endEpochDay: Long): List<DailyCheckInEntity>

    @Query("SELECT * FROM daily_checkins ORDER BY dateEpochDay DESC")
    fun getAllCheckIns(): Flow<List<DailyCheckInEntity>>

    @Query("SELECT * FROM daily_checkins ORDER BY dateEpochDay DESC")
    suspend fun getAllCheckInsOneShot(): List<DailyCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCheckIn(checkIn: DailyCheckInEntity)
}

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements WHERE dateEpochDay = :dateEpochDay")
    fun getMeasurement(dateEpochDay: Long): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY dateEpochDay DESC")
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY dateEpochDay DESC")
    suspend fun getAllMeasurementsOneShot(): List<BodyMeasurementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMeasurement(measurement: BodyMeasurementEntity)
}

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE dateEpochDay = :dateEpochDay ORDER BY timestampEpochMillis ASC")
    fun getWaterEntriesForDate(dateEpochDay: Long): Flow<List<WaterEntryEntity>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE dateEpochDay = :dateEpochDay")
    fun getTotalWaterForDate(dateEpochDay: Long): Flow<Int?>

    @Query("SELECT * FROM water_entries ORDER BY timestampEpochMillis DESC")
    suspend fun getAllWaterEntriesOneShot(): List<WaterEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntryEntity)

    @Query("DELETE FROM water_entries WHERE id = (SELECT id FROM water_entries WHERE dateEpochDay = :dateEpochDay ORDER BY timestampEpochMillis DESC LIMIT 1)")
    suspend fun deleteLastWaterEntryForDate(dateEpochDay: Long)
}

@Dao
interface MealCheckDao {
    @Query("SELECT * FROM meal_checks WHERE dateEpochDay = :dateEpochDay")
    fun getMealChecksForDate(dateEpochDay: Long): Flow<List<MealCheckEntity>>

    @Query("SELECT * FROM meal_checks ORDER BY dateEpochDay DESC")
    suspend fun getAllMealChecksOneShot(): List<MealCheckEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMealCheck(check: MealCheckEntity)
}

@Dao
interface SupplementIntakeDao {
    @Query("SELECT * FROM supplement_intakes WHERE dateEpochDay = :dateEpochDay")
    fun getSupplementIntakesForDate(dateEpochDay: Long): Flow<List<SupplementIntakeEntity>>

    @Query("SELECT * FROM supplement_intakes ORDER BY dateEpochDay DESC")
    suspend fun getAllSupplementIntakesOneShot(): List<SupplementIntakeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementIntake(intake: SupplementIntakeEntity)
}

@Dao
interface BiweeklyReviewDao {
    @Query("SELECT * FROM biweekly_reviews WHERE trainingProgramId = :programId ORDER BY periodEndEpochDay DESC")
    fun getReviewsForProgram(programId: Long): Flow<List<BiweeklyReviewEntity>>

    @Query("SELECT * FROM biweekly_reviews WHERE trainingProgramId = :programId AND status = 'PENDING' ORDER BY periodEndEpochDay DESC LIMIT 1")
    fun getPendingReviewForProgram(programId: Long): Flow<BiweeklyReviewEntity?>

    @Query("SELECT * FROM biweekly_reviews WHERE trainingProgramId = :programId AND status = 'PENDING' ORDER BY periodEndEpochDay DESC LIMIT 1")
    suspend fun getPendingReviewOneShot(programId: Long): BiweeklyReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: BiweeklyReviewEntity): Long

    @Query("UPDATE biweekly_reviews SET status = :status, decidedAtEpochMillis = :decidedAt WHERE id = :id")
    suspend fun updateReviewStatus(id: Long, status: String, decidedAt: Long)
}
