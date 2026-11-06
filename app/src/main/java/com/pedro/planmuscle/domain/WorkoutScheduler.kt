package com.pedro.planmuscle.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WorkoutScheduler {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getCurrentWeekNumber(startDateStr: String, currentDateStr: String): Int {
        return try {
            val start = dateFormat.parse(startDateStr)
            val current = dateFormat.parse(currentDateStr)
            if (start != null && current != null) {
                val diffInMillis = current.time - start.time
                val daysBetween = diffInMillis / (1000 * 60 * 60 * 24)
                val week = (daysBetween / 7).toInt() + 1
                week.coerceIn(1, 8)
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Alterna os treinos A/B conforme a semana:
     * Semana 1: Seg A, Qua B, Sex A
     * Semana 2: Seg B, Qua A, Sex B
     */
    fun getWorkoutTypeForDate(startDateStr: String, targetDateStr: String): String {
        val weekNumber = getCurrentWeekNumber(startDateStr, targetDateStr)
        return try {
            val targetDate = dateFormat.parse(targetDateStr)
            val calendar = Calendar.getInstance()
            if (targetDate != null) {
                calendar.time = targetDate
            }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Calendar.MONDAY = 2, WEDNESDAY = 4, FRIDAY = 6
            val isOddWeek = weekNumber % 2 != 0

            when (dayOfWeek) {
                Calendar.MONDAY -> if (isOddWeek) "A" else "B"
                Calendar.WEDNESDAY -> if (isOddWeek) "B" else "A"
                Calendar.FRIDAY -> if (isOddWeek) "A" else "B"
                else -> if (isOddWeek) "A" else "B"
            }
        } catch (e: Exception) {
            if (weekNumber % 2 != 0) "A" else "B"
        }
    }
}
