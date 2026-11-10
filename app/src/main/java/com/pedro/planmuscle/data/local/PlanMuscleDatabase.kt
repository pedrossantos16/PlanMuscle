package com.pedro.planmuscle.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pedro.planmuscle.data.local.converter.Converters
import com.pedro.planmuscle.data.local.dao.*
import com.pedro.planmuscle.data.local.entity.*
import com.pedro.planmuscle.data.local.migration.MIGRATION_1_2

@Database(
    entities = [
        TrainingProgramEntity::class,
        NutritionTargetEntity::class,
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutTemplateExerciseEntity::class,
        PlannedWorkoutEntity::class,
        WorkoutSessionEntity::class,
        SessionExercisePrescriptionEntity::class,
        ExerciseSetLogEntity::class,
        DailyCheckInEntity::class,
        BodyMeasurementEntity::class,
        WaterEntryEntity::class,
        MealCheckEntity::class,
        SupplementIntakeEntity::class,
        BiweeklyReviewEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PlanMuscleDatabase : RoomDatabase() {

    abstract fun trainingProgramDao(): TrainingProgramDao
    abstract fun nutritionTargetDao(): NutritionTargetDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun plannedWorkoutDao(): PlannedWorkoutDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun mealCheckDao(): MealCheckDao
    abstract fun supplementIntakeDao(): SupplementIntakeDao
    abstract fun biweeklyReviewDao(): BiweeklyReviewDao

    companion object {
        @Volatile
        private var INSTANCE: PlanMuscleDatabase? = null

        fun getDatabase(context: Context): PlanMuscleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanMuscleDatabase::class.java,
                    "plan_muscle_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
