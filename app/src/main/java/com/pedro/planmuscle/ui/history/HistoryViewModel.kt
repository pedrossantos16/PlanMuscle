package com.pedro.planmuscle.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.converter.DateUtils
import com.pedro.planmuscle.data.local.entity.BiweeklyReviewEntity
import com.pedro.planmuscle.data.local.entity.BodyMeasurementEntity
import com.pedro.planmuscle.data.local.entity.DailyCheckInEntity
import com.pedro.planmuscle.data.local.entity.NutritionTargetEntity
import com.pedro.planmuscle.domain.engine.WorkoutScheduler
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarWeightPoint(
    val dateEpochDay: Long,
    val dateString: String,
    val dailyWeightKg: Float?,
    val movingAverage7DaysKg: Float?
)

data class HistoryUiState(
    val activeNutritionTarget: NutritionTargetEntity? = null,
    val currentWeekNumber: Int = 1,
    val weightDataPoints: List<CalendarWeightPoint> = emptyList(),
    val measurementsHistory: List<BodyMeasurementEntity> = emptyList(),
    val pendingReview: BiweeklyReviewEntity? = null
)

class HistoryViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayEpochDay = repository.getTodayEpochDay()

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getActiveProgram(),
        repository.getActiveNutritionTarget(),
        repository.getAllCheckIns(),
        repository.getAllMeasurements()
    ) { program, target, checkIns, measurements ->
        val startDay = program?.startDateEpochDay ?: todayEpochDay
        val week = WorkoutScheduler.getProtocolWeek(startDay, todayEpochDay)

        // Compute 7-calendar-day moving average
        val sortedCheckIns = checkIns.sortedBy { it.dateEpochDay }
        val points = sortedCheckIns.map { current ->
            val window7Days = sortedCheckIns.filter {
                it.dateEpochDay in (current.dateEpochDay - 6)..current.dateEpochDay
            }.mapNotNull { it.weightKg }

            val avg = if (window7Days.isNotEmpty()) window7Days.average().toFloat() else null

            CalendarWeightPoint(
                dateEpochDay = current.dateEpochDay,
                dateString = DateUtils.epochDayToDateString(current.dateEpochDay),
                dailyWeightKg = current.weightKg,
                movingAverage7DaysKg = avg
            )
        }

        HistoryUiState(
            activeNutritionTarget = target,
            currentWeekNumber = week,
            weightDataPoints = points.sortedByDescending { it.dateEpochDay },
            measurementsHistory = measurements.sortedByDescending { it.dateEpochDay },
            pendingReview = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun generateReviewIfDue(programId: Long) {
        viewModelScope.launch {
            repository.generateBiweeklyReviewIfDue(programId)
        }
    }

    fun acceptReview(review: BiweeklyReviewEntity) {
        viewModelScope.launch {
            repository.acceptReviewAdjustment(review.id)
            if (review.calorieAdjustment != 0) {
                val currentTarget = repository.getActiveNutritionTarget()
                val activeCal = currentTarget.let { null } ?: 2550
                repository.updateNutritionTarget(
                    calories = activeCal + review.calorieAdjustment,
                    protein = 150,
                    carbs = 330,
                    fats = 70,
                    waterMl = 2600,
                    reason = "Ajuste quinzenal aceito (+${review.calorieAdjustment} kcal)"
                )
            }
        }
    }

    fun rejectReview(reviewId: Long) {
        viewModelScope.launch {
            repository.rejectReview(reviewId)
        }
    }
}
