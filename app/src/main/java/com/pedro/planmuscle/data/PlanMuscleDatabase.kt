package com.pedro.planmuscle.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pedro.planmuscle.data.dao.BodyMeasurementDao
import com.pedro.planmuscle.data.dao.DailyCheckInDao
import com.pedro.planmuscle.data.dao.NutritionDao
import com.pedro.planmuscle.data.dao.ProtocolDao
import com.pedro.planmuscle.data.dao.WaterEntryDao
import com.pedro.planmuscle.data.dao.WorkoutDao
import com.pedro.planmuscle.data.model.BodyMeasurementEntity
import com.pedro.planmuscle.data.model.DailyCheckInEntity
import com.pedro.planmuscle.data.model.ExerciseSetLogEntity
import com.pedro.planmuscle.data.model.MealCheckEntity
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.model.SupplementIntakeEntity
import com.pedro.planmuscle.data.model.WaterEntryEntity
import com.pedro.planmuscle.data.model.WorkoutSessionEntity

@Database(
    entities = [
        ProtocolEntity::class,
        DailyCheckInEntity::class,
        BodyMeasurementEntity::class,
        WaterEntryEntity::class,
        WorkoutSessionEntity::class,
        ExerciseSetLogEntity::class,
        MealCheckEntity::class,
        SupplementIntakeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PlanMuscleDatabase : RoomDatabase() {
    abstract fun protocolDao(): ProtocolDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao

    companion object {
        @Volatile
        private var INSTANCE: PlanMuscleDatabase? = null

        fun getDatabase(context: Context): PlanMuscleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlanMuscleDatabase::class.java,
                    "plan_muscle_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
