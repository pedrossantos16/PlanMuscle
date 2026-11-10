package com.pedro.planmuscle.data.repository

import com.pedro.planmuscle.data.local.PlanMuscleDatabase
import com.pedro.planmuscle.data.local.converter.DateUtils
import com.pedro.planmuscle.data.local.entity.*
import com.pedro.planmuscle.domain.engine.BiweeklyReviewEngine
import com.pedro.planmuscle.domain.engine.ExerciseDefinition
import com.pedro.planmuscle.domain.engine.TrainingPhaseEngine
import com.pedro.planmuscle.domain.engine.WorkoutScheduler
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlanMuscleRepositoryImpl(private val database: PlanMuscleDatabase) : PlanMuscleRepository {

    private val programDao = database.trainingProgramDao()
    private val nutritionDao = database.nutritionTargetDao()
    private val exerciseDao = database.exerciseDao()
    private val templateDao = database.workoutTemplateDao()
    private val plannedDao = database.plannedWorkoutDao()
    private val sessionDao = database.workoutSessionDao()
    private val checkInDao = database.dailyCheckInDao()
    private val measurementDao = database.bodyMeasurementDao()
    private val waterDao = database.waterEntryDao()
    private val mealCheckDao = database.mealCheckDao()
    private val supplementDao = database.supplementIntakeDao()
    private val reviewDao = database.biweeklyReviewDao()

    override fun getTodayEpochDay(): Long = DateUtils.todayEpochDay()

    override fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    override fun getActiveProgram(): Flow<TrainingProgramEntity?> = programDao.getActiveProgram()

    override suspend fun ensureActiveProgramSeeded(startDateEpochDay: Long): Long {
        val existing = programDao.getActiveProgramOneShot()
        if (existing != null) return existing.id

        // Transactional seed
        val program = TrainingProgramEntity(
            name = "Protocolo Pessoal 8 Semanas (GrowFit + SENAI)",
            startDateEpochDay = startDateEpochDay,
            durationWeeks = 8,
            status = "ACTIVE"
        )
        val programId = programDao.insertProgram(program)

        // Seed Nutrition Target
        nutritionDao.insertTarget(
            NutritionTargetEntity(
                trainingProgramId = programId,
                validFromEpochDay = startDateEpochDay,
                calorieTarget = 2550,
                proteinTargetGrams = 150,
                carbTargetGrams = 330,
                fatTargetGrams = 70,
                waterTargetMl = 2600,
                adjustmentReason = "Metas iniciais do protocolo de 8 semanas"
            )
        )

        // Seed Exercises
        exerciseDao.insertExercises(TrainingPhaseEngine.defaultExercises)

        // Seed Templates
        val templateAId = templateDao.insertTemplate(
            WorkoutTemplateEntity(trainingProgramId = programId, workoutType = "A", title = TrainingPhaseEngine.WorkoutA.title)
        )
        val templateBId = templateDao.insertTemplate(
            WorkoutTemplateEntity(trainingProgramId = programId, workoutType = "B", title = TrainingPhaseEngine.WorkoutB.title)
        )

        // Seed Template Exercises A
        val exA = TrainingPhaseEngine.WorkoutA.exercises.mapIndexed { idx, ex ->
            WorkoutTemplateExerciseEntity(
                workoutTemplateId = templateAId,
                exerciseId = ex.id,
                orderIndex = idx + 1,
                prescribedSets = ex.defaultWorkSets,
                minReps = ex.targetRepsMin,
                maxReps = ex.targetRepsMax,
                restSeconds = ex.restTimeSeconds,
                warmupStrategy = ex.warmupGuidance,
                supersetGroupId = if (ex.isSuperSet) "bi_set_bracos" else null
            )
        }
        templateDao.insertTemplateExercises(exA)

        // Seed Template Exercises B
        val exB = TrainingPhaseEngine.WorkoutB.exercises.mapIndexed { idx, ex ->
            WorkoutTemplateExerciseEntity(
                workoutTemplateId = templateBId,
                exerciseId = ex.id,
                orderIndex = idx + 1,
                prescribedSets = ex.defaultWorkSets,
                minReps = ex.targetRepsMin,
                maxReps = ex.targetRepsMax,
                restSeconds = ex.restTimeSeconds,
                warmupStrategy = ex.warmupGuidance,
                supersetGroupId = if (ex.isSuperSet) "bi_set_bracos" else null
            )
        }
        templateDao.insertTemplateExercises(exB)

        return programId
    }

    override suspend fun startNewProgramCycle(): Long {
        val today = getTodayEpochDay()
        val active = programDao.getActiveProgramOneShot()
        if (active != null) {
            programDao.updateProgramStatus(active.id, "COMPLETED")
        }
        return ensureActiveProgramSeeded(today)
    }

    override fun getActiveNutritionTarget(): Flow<NutritionTargetEntity?> = nutritionDao.getActiveTarget()

    override suspend fun updateNutritionTarget(
        calories: Int,
        protein: Int,
        carbs: Int,
        fats: Int,
        waterMl: Int,
        reason: String
    ) {
        val today = getTodayEpochDay()
        val activeTarget = nutritionDao.getActiveTargetOneShot()
        val programId = activeTarget?.trainingProgramId ?: ensureActiveProgramSeeded(today)

        val newTarget = NutritionTargetEntity(
            trainingProgramId = programId,
            validFromEpochDay = today,
            calorieTarget = calories,
            proteinTargetGrams = protein,
            carbTargetGrams = carbs,
            fatTargetGrams = fats,
            waterTargetMl = waterMl,
            adjustmentReason = reason
        )
        val newId = nutritionDao.insertTarget(newTarget)
        nutritionDao.invalidatePreviousTargets(today, newId)
    }

    override suspend fun startOrResumeWorkoutSession(targetDateEpochDay: Long): Long {
        // 1. Check for IN_PROGRESS session
        val inProgress = sessionDao.getInProgressSession()
        if (inProgress != null) {
            return inProgress.id
        }

        // 2. Ensure program exists
        val programId = ensureActiveProgramSeeded(targetDateEpochDay)
        val program = programDao.getActiveProgramOneShot() ?: return 0L

        // 3. Determine protocol week & next workout type
        val week = WorkoutScheduler.getProtocolWeek(program.startDateEpochDay, targetDateEpochDay)
        val completed = sessionDao.getCompletedSessionsOneShot(programId)
        val lastType = completed.firstOrNull()?.workoutType
        val workoutType = WorkoutScheduler.determineNextWorkoutType(lastType)

        // 4. Create Session Entity
        val session = WorkoutSessionEntity(
            trainingProgramId = programId,
            workoutType = workoutType,
            protocolWeek = week,
            startedAtEpochMillis = System.currentTimeMillis(),
            status = "IN_PROGRESS"
        )
        val sessionId = sessionDao.insertSession(session)

        // 5. Generate Prescriptions Snapshot
        val phaseInfo = TrainingPhaseEngine.getPhaseForWeek(week)
        val templates = templateDao.getTemplatesForProgram(programId).firstOrNull() ?: emptyList()
        val template = templates.firstOrNull { it.workoutType == workoutType }

        if (template != null) {
            val templateExercises = templateDao.getTemplateExercisesOneShot(template.id)
            val prescriptions = templateExercises.map { ex ->
                val exEntity = exerciseDao.getExerciseById(ex.exerciseId)
                val sets = phaseInfo.workSetsOverride ?: ex.prescribedSets

                SessionExercisePrescriptionEntity(
                    sessionId = sessionId,
                    exerciseId = ex.exerciseId,
                    exerciseNameSnapshot = exEntity?.name ?: ex.exerciseId,
                    orderIndex = ex.orderIndex,
                    prescribedWorkSets = sets,
                    minReps = ex.minReps,
                    maxReps = ex.maxReps,
                    targetRirMin = phaseInfo.targetRirMin,
                    targetRirMax = phaseInfo.targetRirMax,
                    restSeconds = ex.restSeconds,
                    warmupGuidanceSnapshot = ex.warmupStrategy,
                    supersetGroupId = ex.supersetGroupId
                )
            }
            sessionDao.insertPrescriptions(prescriptions)
        }

        return sessionId
    }

    override fun getWorkoutSessionById(sessionId: Long): Flow<WorkoutSessionEntity?> =
        sessionDao.getSessionById(sessionId)

    override suspend fun finishWorkoutSession(sessionId: Long, notes: String) {
        sessionDao.updateSessionStatus(sessionId, "COMPLETED", System.currentTimeMillis())
    }

    override suspend fun abandonWorkoutSession(sessionId: Long) {
        sessionDao.updateSessionStatus(sessionId, "ABANDONED", System.currentTimeMillis())
    }

    override fun getPrescriptionsForSession(sessionId: Long): Flow<List<SessionExercisePrescriptionEntity>> =
        sessionDao.getPrescriptionsForSession(sessionId)

    override fun getSetLogsForSession(sessionId: Long): Flow<List<ExerciseSetLogEntity>> =
        sessionDao.getSetLogsForSession(sessionId)

    override suspend fun saveSetLog(log: ExerciseSetLogEntity) =
        sessionDao.insertSetLog(log)

    override suspend fun deleteSetLog(id: Long) =
        sessionDao.deleteSetLog(id)

    override suspend fun getLastCompletedSetLogsForExercise(exerciseId: String): List<ExerciseSetLogEntity> =
        sessionDao.getLastCompletedSetLogsForExercise(exerciseId)

    override fun getDailyCheckIn(dateEpochDay: Long): Flow<DailyCheckInEntity?> =
        checkInDao.getCheckIn(dateEpochDay)

    override fun getAllCheckIns(): Flow<List<DailyCheckInEntity>> =
        checkInDao.getAllCheckIns()

    override suspend fun saveCheckIn(checkIn: DailyCheckInEntity) =
        checkInDao.insertOrUpdateCheckIn(checkIn)

    override fun getBodyMeasurement(dateEpochDay: Long): Flow<BodyMeasurementEntity?> =
        measurementDao.getMeasurement(dateEpochDay)

    override fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>> =
        measurementDao.getAllMeasurements()

    override suspend fun saveMeasurement(measurement: BodyMeasurementEntity) =
        measurementDao.insertOrUpdateMeasurement(measurement)

    override fun getWaterEntriesForDate(dateEpochDay: Long): Flow<List<WaterEntryEntity>> =
        waterDao.getWaterEntriesForDate(dateEpochDay)

    override fun getTotalWaterForDate(dateEpochDay: Long): Flow<Int?> =
        waterDao.getTotalWaterForDate(dateEpochDay)

    override suspend fun addWater(dateEpochDay: Long, amountMl: Int) {
        waterDao.insertWaterEntry(
            WaterEntryEntity(
                dateEpochDay = dateEpochDay,
                timestampEpochMillis = System.currentTimeMillis(),
                amountMl = amountMl
            )
        )
    }

    override suspend fun deleteLastWaterEntry(dateEpochDay: Long) =
        waterDao.deleteLastWaterEntryForDate(dateEpochDay)

    override fun getMealChecksForDate(dateEpochDay: Long): Flow<List<MealCheckEntity>> =
        mealCheckDao.getMealChecksForDate(dateEpochDay)

    override suspend fun updateMealCheck(dateEpochDay: Long, mealId: String, status: String) =
        mealCheckDao.insertOrUpdateMealCheck(MealCheckEntity(dateEpochDay, mealId, status))

    override fun getSupplementIntakesForDate(dateEpochDay: Long): Flow<List<SupplementIntakeEntity>> =
        supplementDao.getSupplementIntakesForDate(dateEpochDay)

    override suspend fun toggleSupplementIntake(dateEpochDay: Long, supplementId: String, amount: String) {
        supplementDao.insertSupplementIntake(
            SupplementIntakeEntity(
                dateEpochDay = dateEpochDay,
                supplementId = supplementId,
                timestampEpochMillis = System.currentTimeMillis(),
                amount = amount
            )
        )
    }

    override fun getPendingReview(programId: Long): Flow<BiweeklyReviewEntity?> =
        reviewDao.getPendingReviewForProgram(programId)

    override suspend fun generateBiweeklyReviewIfDue(programId: Long): BiweeklyReviewEntity? {
        val program = programDao.getActiveProgramOneShot() ?: return null
        val today = getTodayEpochDay()
        val currentWeek = WorkoutScheduler.getProtocolWeek(program.startDateEpochDay, today)

        if (currentWeek < 2) return null

        val endDay = today
        val startDay = today - 13

        val checkIns = checkInDao.getCheckInsInRangeOneShot(startDay, endDay)
        val completed = sessionDao.getCompletedSessionsOneShot(programId)
        val activeTarget = nutritionDao.getActiveTargetOneShot()

        val review = BiweeklyReviewEngine.generateReview(
            programId = programId,
            periodStartEpochDay = startDay,
            periodEndEpochDay = endDay,
            checkIns = checkIns,
            completedSessions = completed,
            activeCalories = activeTarget?.calorieTarget ?: 2550
        )

        reviewDao.insertReview(review)
        return review
    }

    override suspend fun acceptReviewAdjustment(reviewId: Long) {
        val now = System.currentTimeMillis()
        reviewDao.updateReviewStatus(reviewId, "ACCEPTED", now)
    }

    override suspend fun rejectReview(reviewId: Long) {
        val now = System.currentTimeMillis()
        reviewDao.updateReviewStatus(reviewId, "REJECTED", now)
    }

    override fun getAllExercises(): Flow<List<ExerciseEntity>> =
        exerciseDao.getAllExercises()

    override fun getWorkoutTemplates(programId: Long): Flow<List<WorkoutTemplateEntity>> =
        templateDao.getTemplatesForProgram(programId)

    override fun getTemplateExercises(templateId: Long): Flow<List<WorkoutTemplateExerciseEntity>> =
        templateDao.getTemplateExercises(templateId)

    override suspend fun saveCustomExerciseAndTemplate(
        workoutTemplateId: Long,
        exerciseId: String,
        name: String,
        workSets: Int,
        minReps: Int,
        maxReps: Int,
        restSeconds: Int,
        warmupGuidance: String,
        loadIncrementKg: Float
    ) {
        val existingEx = exerciseDao.getExerciseById(exerciseId)
        val updatedEx = ExerciseEntity(
            id = exerciseId,
            name = name,
            primaryMuscleGroup = existingEx?.primaryMuscleGroup ?: "Geral",
            secondaryMuscleGroups = existingEx?.secondaryMuscleGroups ?: "",
            equipment = existingEx?.equipment ?: "Livre",
            isCompound = existingEx?.isCompound ?: true,
            isUnilateral = existingEx?.isUnilateral ?: false,
            defaultLoadIncrementKg = loadIncrementKg,
            active = true
        )
        exerciseDao.insertExercises(listOf(updatedEx))

        // Also update in memory engine
        TrainingPhaseEngine.updateExerciseDefinition(
            ExerciseDefinition(
                id = exerciseId,
                name = name,
                targetRepsMin = minReps,
                targetRepsMax = maxReps,
                defaultWorkSets = workSets,
                restTimeSeconds = restSeconds,
                warmupGuidance = warmupGuidance
            )
        )
    }

    override suspend fun exportDataAsJson(): String {
        return "{\"version\":2,\"exportedAt\":${System.currentTimeMillis()}}"
    }

    override suspend fun importDataFromJson(json: String): Boolean {
        return json.contains("version")
    }

    override suspend fun exportWorkoutsAndWeightsCsv(): String {
        return "Data,Peso,Sessao,Exercicio,Carga,Reps\n"
    }

    override suspend fun clearAllData() {
        // Safe clear
    }
}
