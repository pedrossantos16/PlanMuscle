package com.pedro.planmuscle.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pedro.planmuscle.data.local.PlanMuscleDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PlanMuscleDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2PreservesAllHistoricalDataAndSetLogs() {
        // Create V1 database
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Insert V1 protocol
            execSQL(
                """
                INSERT INTO protocols (id, startDate, durationWeeks, calorieTarget, proteinTargetGrams, carbTargetGrams, fatTargetGrams, waterTargetMl, isActive)
                VALUES (1, '2026-10-01', 8, 2550, 150, 330, 70, 2600, 1)
                """.trimIndent()
            )

            // Insert 10 V1 check-ins
            for (i in 1..10) {
                val dayStr = String.format(java.util.Locale.US, "2026-10-%02d", i)
                execSQL("INSERT INTO daily_checkins (date, weightKg, readinessLevel, sleepHours, sleepQuality, notes) VALUES ('$dayStr', ${76.0 + i * 0.1}, 4, 7.5, 4, 'Note $i')")
            }

            // Insert 2 V1 measurements
            execSQL("INSERT INTO body_measurements (date, waistCm, armCm, thighCm, chestCm) VALUES ('2026-10-01', 82.0, 37.5, 58.0, 102.0)")
            execSQL("INSERT INTO body_measurements (date, waistCm, armCm, thighCm, chestCm) VALUES ('2026-10-08', 82.2, 37.8, 58.3, 102.5)")

            // Insert water entries, meals, supplements
            execSQL("INSERT INTO water_entries (id, date, timestamp, amountMl) VALUES (1, '2026-10-01', 1700000000000, 500)")
            execSQL("INSERT INTO meal_checks (date, mealId, completed) VALUES ('2026-10-01', 'LUNCH', 1)")
            execSQL("INSERT INTO supplement_intakes (id, date, timestamp, supplementId, amount) VALUES (1, '2026-10-01', 1700000000000, 'CREATINE', '5g')")

            // Insert V1 session A (completed) with decimal weight 12.5 kg & WARMUP/WORK sets
            execSQL("INSERT INTO workout_sessions (id, date, weekNumber, workoutType, startedAtMillis, finishedAtMillis, status) VALUES (1, '2026-10-01', 1, 'A', 1700000000000, 1700003000000, 'COMPLETED')")
            execSQL("INSERT INTO exercise_set_logs (id, sessionId, exerciseId, exerciseName, setNumber, setType, weightKg, reps, rir) VALUES (1, 1, 'supino_reto', 'Supino reto com barra', 1, 'WARMUP', 20.0, 12, NULL)")
            execSQL("INSERT INTO exercise_set_logs (id, sessionId, exerciseId, exerciseName, setNumber, setType, weightKg, reps, rir) VALUES (2, 1, 'supino_reto', 'Supino reto com barra', 2, 'WORK', 42.5, 10, 2)")

            // Insert V1 session B (in progress)
            execSQL("INSERT INTO workout_sessions (id, date, weekNumber, workoutType, startedAtMillis, finishedAtMillis, status) VALUES (2, '2026-10-03', 1, 'B', 1700005000000, NULL, 'IN_PROGRESS')")

            close()
        }

        // Run Migration 1 -> 2
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Validate V2 data via raw queries
        val checkInCursor = db.query("SELECT COUNT(*) FROM daily_checkins")
        checkInCursor.moveToFirst()
        assertEquals(10, checkInCursor.getInt(0))
        checkInCursor.close()

        val measurementCursor = db.query("SELECT COUNT(*) FROM body_measurements")
        measurementCursor.moveToFirst()
        assertEquals(2, measurementCursor.getInt(0))
        measurementCursor.close()

        val sessionCursor = db.query("SELECT COUNT(*) FROM workout_sessions")
        sessionCursor.moveToFirst()
        assertEquals(2, sessionCursor.getInt(0))
        sessionCursor.close()

        val prescriptionCursor = db.query("SELECT COUNT(*) FROM session_exercise_prescriptions")
        prescriptionCursor.moveToFirst()
        assertTrue(prescriptionCursor.getInt(0) >= 1)
        prescriptionCursor.close()

        val setLogsCursor = db.query("SELECT weightKg, reps, rir FROM exercise_set_logs WHERE setType = 'WORK'")
        setLogsCursor.moveToFirst()
        assertEquals(42.5f, setLogsCursor.getFloat(0), 0.01f)
        assertEquals(10, setLogsCursor.getInt(1))
        assertEquals(2, setLogsCursor.getInt(2))
        setLogsCursor.close()
    }
}
