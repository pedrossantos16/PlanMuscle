package com.pedro.planmuscle.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create V2 tables
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `training_programs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `startDateEpochDay` INTEGER NOT NULL,
                `durationWeeks` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_training_programs_status` ON `training_programs` (`status`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `nutrition_targets` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `trainingProgramId` INTEGER,
                `validFromEpochDay` INTEGER NOT NULL,
                `validUntilEpochDay` INTEGER,
                `calorieTarget` INTEGER NOT NULL,
                `proteinTargetGrams` INTEGER NOT NULL,
                `carbTargetGrams` INTEGER NOT NULL,
                `fatTargetGrams` INTEGER NOT NULL,
                `waterTargetMl` INTEGER NOT NULL,
                `adjustmentReason` TEXT NOT NULL,
                `createdAtEpochMillis` INTEGER NOT NULL,
                FOREIGN KEY(`trainingProgramId`) REFERENCES `training_programs`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_nutrition_targets_trainingProgramId` ON `nutrition_targets` (`trainingProgramId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_nutrition_targets_validFromEpochDay` ON `nutrition_targets` (`validFromEpochDay`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exercises` (
                `id` TEXT PRIMARY KEY NOT NULL,
                `name` TEXT NOT NULL,
                `primaryMuscleGroup` TEXT NOT NULL,
                `secondaryMuscleGroups` TEXT NOT NULL,
                `equipment` TEXT NOT NULL,
                `isCompound` INTEGER NOT NULL,
                `isUnilateral` INTEGER NOT NULL,
                `defaultLoadIncrementKg` REAL NOT NULL,
                `active` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_active` ON `exercises` (`active`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_templates` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `trainingProgramId` INTEGER NOT NULL,
                `workoutType` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `version` INTEGER NOT NULL,
                `active` INTEGER NOT NULL,
                FOREIGN KEY(`trainingProgramId`) REFERENCES `training_programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_templates_trainingProgramId` ON `workout_templates` (`trainingProgramId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_templates_workoutType` ON `workout_templates` (`workoutType`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_template_exercises` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `workoutTemplateId` INTEGER NOT NULL,
                `exerciseId` TEXT NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                `prescribedSets` INTEGER NOT NULL,
                `minReps` INTEGER NOT NULL,
                `maxReps` INTEGER NOT NULL,
                `restSeconds` INTEGER NOT NULL,
                `warmupStrategy` TEXT NOT NULL,
                `supersetGroupId` TEXT,
                `notes` TEXT NOT NULL,
                FOREIGN KEY(`workoutTemplateId`) REFERENCES `workout_templates`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_template_exercises_workoutTemplateId` ON `workout_template_exercises` (`workoutTemplateId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_template_exercises_exerciseId` ON `workout_template_exercises` (`exerciseId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `planned_workouts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `trainingProgramId` INTEGER NOT NULL,
                `plannedDateEpochDay` INTEGER NOT NULL,
                `workoutType` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `completedSessionId` INTEGER,
                FOREIGN KEY(`trainingProgramId`) REFERENCES `training_programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_planned_workouts_trainingProgramId` ON `planned_workouts` (`trainingProgramId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_planned_workouts_plannedDateEpochDay` ON `planned_workouts` (`plannedDateEpochDay`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `workout_sessions_v2` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `trainingProgramId` INTEGER NOT NULL,
                `plannedWorkoutId` INTEGER,
                `workoutType` TEXT NOT NULL,
                `protocolWeek` INTEGER NOT NULL,
                `startedAtEpochMillis` INTEGER NOT NULL,
                `finishedAtEpochMillis` INTEGER,
                `status` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                FOREIGN KEY(`trainingProgramId`) REFERENCES `training_programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_v2_trainingProgramId` ON `workout_sessions_v2` (`trainingProgramId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_v2_status` ON `workout_sessions_v2` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_v2_startedAtEpochMillis` ON `workout_sessions_v2` (`startedAtEpochMillis`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `session_exercise_prescriptions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `sessionId` INTEGER NOT NULL,
                `exerciseId` TEXT NOT NULL,
                `exerciseNameSnapshot` TEXT NOT NULL,
                `orderIndex` INTEGER NOT NULL,
                `prescribedWorkSets` INTEGER NOT NULL,
                `minReps` INTEGER NOT NULL,
                `maxReps` INTEGER NOT NULL,
                `targetRirMin` INTEGER NOT NULL,
                `targetRirMax` INTEGER NOT NULL,
                `restSeconds` INTEGER NOT NULL,
                `warmupGuidanceSnapshot` TEXT NOT NULL,
                `supersetGroupId` TEXT,
                FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions_v2`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercise_prescriptions_sessionId` ON `session_exercise_prescriptions` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_session_exercise_prescriptions_exerciseId` ON `session_exercise_prescriptions` (`exerciseId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exercise_set_logs_v2` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `sessionId` INTEGER NOT NULL,
                `sessionExercisePrescriptionId` INTEGER NOT NULL,
                `setIndex` INTEGER NOT NULL,
                `setType` TEXT NOT NULL,
                `weightKg` REAL NOT NULL,
                `reps` INTEGER NOT NULL,
                `rir` INTEGER,
                `techniqueOk` INTEGER NOT NULL,
                `completedAtEpochMillis` INTEGER NOT NULL,
                `notes` TEXT NOT NULL,
                FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions_v2`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`sessionExercisePrescriptionId`) REFERENCES `session_exercise_prescriptions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_set_logs_v2_sessionId` ON `exercise_set_logs_v2` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercise_set_logs_v2_sessionExercisePrescriptionId` ON `exercise_set_logs_v2` (`sessionExercisePrescriptionId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_set_logs_v2_sessionExercisePrescriptionId_setIndex` ON `exercise_set_logs_v2` (`sessionExercisePrescriptionId`, `setIndex`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_checkins_v2` (
                `dateEpochDay` INTEGER PRIMARY KEY NOT NULL,
                `weightKg` REAL,
                `readinessLevel` INTEGER,
                `sleepMinutes` INTEGER,
                `sleepQuality` INTEGER,
                `unusualPain` INTEGER NOT NULL,
                `notes` TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `body_measurements_v2` (
                `dateEpochDay` INTEGER PRIMARY KEY NOT NULL,
                `waistCm` REAL,
                `armCm` REAL,
                `thighCm` REAL,
                `chestCm` REAL,
                `measurementNotes` TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `water_entries_v2` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `dateEpochDay` INTEGER NOT NULL,
                `timestampEpochMillis` INTEGER NOT NULL,
                `amountMl` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_water_entries_v2_dateEpochDay` ON `water_entries_v2` (`dateEpochDay`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `meal_checks_v2` (
                `dateEpochDay` INTEGER NOT NULL,
                `mealId` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `notes` TEXT NOT NULL,
                PRIMARY KEY(`dateEpochDay`, `mealId`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_checks_v2_dateEpochDay` ON `meal_checks_v2` (`dateEpochDay`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `supplement_intakes_v2` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `dateEpochDay` INTEGER NOT NULL,
                `supplementId` TEXT NOT NULL,
                `timestampEpochMillis` INTEGER NOT NULL,
                `amount` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_supplement_intakes_v2_dateEpochDay_supplementId` ON `supplement_intakes_v2` (`dateEpochDay`, `supplementId`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `biweekly_reviews` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `trainingProgramId` INTEGER NOT NULL,
                `periodStartEpochDay` INTEGER NOT NULL,
                `periodEndEpochDay` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `averageWeightFirstWeek` REAL,
                `averageWeightSecondWeek` REAL,
                `weeklyWeightChangeKg` REAL,
                `waistChangeCm` REAL,
                `averageReadiness` REAL,
                `scheduledWorkouts` INTEGER NOT NULL,
                `completedWorkouts` INTEGER NOT NULL,
                `trainingAdherence` REAL NOT NULL,
                `mealPlanAdherence` REAL,
                `progressionSummary` TEXT NOT NULL,
                `recommendationType` TEXT NOT NULL,
                `calorieAdjustment` INTEGER NOT NULL,
                `rationale` TEXT NOT NULL,
                `decidedAtEpochMillis` INTEGER,
                FOREIGN KEY(`trainingProgramId`) REFERENCES `training_programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_biweekly_reviews_trainingProgramId` ON `biweekly_reviews` (`trainingProgramId`)")

        // 2. Migrate protocols -> training_programs and nutrition_targets
        db.execSQL(
            """
            INSERT INTO `training_programs` (`id`, `name`, `startDateEpochDay`, `durationWeeks`, `status`, `createdAtEpochMillis`)
            SELECT `id`, 'Protocolo de Hipertrofia 8 Semanas',
                CAST(strftime('%s', `startDate`) / 86400 AS INTEGER),
                `durationWeeks`,
                CASE WHEN `isActive` = 1 THEN 'ACTIVE' ELSE 'ARCHIVED' END,
                CAST(strftime('%s', 'now') * 1000 AS INTEGER)
            FROM `protocols`
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO `nutrition_targets` (`id`, `trainingProgramId`, `validFromEpochDay`, `validUntilEpochDay`, `calorieTarget`, `proteinTargetGrams`, `carbTargetGrams`, `fatTargetGrams`, `waterTargetMl`, `adjustmentReason`, `createdAtEpochMillis`)
            SELECT `id`, `id`,
                CAST(strftime('%s', `startDate`) / 86400 AS INTEGER),
                NULL,
                `calorieTarget`, `proteinTargetGrams`, `carbTargetGrams`, `fatTargetGrams`, `waterTargetMl`,
                'Migrado da versão 1',
                CAST(strftime('%s', 'now') * 1000 AS INTEGER)
            FROM `protocols`
            """.trimIndent()
        )

        // 3. Drop old tables and rename v2 tables
        db.execSQL("DROP TABLE IF EXISTS `protocols`")
        db.execSQL("DROP TABLE IF EXISTS `daily_checkins`")
        db.execSQL("DROP TABLE IF EXISTS `body_measurements`")
        db.execSQL("DROP TABLE IF EXISTS `water_entries`")
        db.execSQL("DROP TABLE IF EXISTS `exercise_set_logs`")
        db.execSQL("DROP TABLE IF EXISTS `workout_sessions`")
        db.execSQL("DROP TABLE IF EXISTS `meal_checks`")
        db.execSQL("DROP TABLE IF EXISTS `supplement_intakes`")

        db.execSQL("ALTER TABLE `workout_sessions_v2` RENAME TO `workout_sessions`")
        db.execSQL("ALTER TABLE `exercise_set_logs_v2` RENAME TO `exercise_set_logs`")
        db.execSQL("ALTER TABLE `daily_checkins_v2` RENAME TO `daily_checkins`")
        db.execSQL("ALTER TABLE `body_measurements_v2` RENAME TO `body_measurements`")
        db.execSQL("ALTER TABLE `water_entries_v2` RENAME TO `water_entries`")
        db.execSQL("ALTER TABLE `meal_checks_v2` RENAME TO `meal_checks`")
        db.execSQL("ALTER TABLE `supplement_intakes_v2` RENAME TO `supplement_intakes`")
    }
}
