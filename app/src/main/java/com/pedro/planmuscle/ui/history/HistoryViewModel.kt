package com.pedro.planmuscle.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.converter.DateUtils
import com.pedro.planmuscle.data.local.entity.BiweeklyReviewEntity
import com.pedro.planmuscle.data.local.entity.BodyMeasurementEntity
import com.pedro.planmuscle.data.local.entity.NutritionTargetEntity
import com.pedro.planmuscle.domain.engine.WorkoutScheduler
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = repository.getActiveProgram().flatMapLatest { program ->
        val programId = program?.id ?: 0L
        val startDay = program?.startDateEpochDay ?: todayEpochDay
        val week = WorkoutScheduler.getProtocolWeek(startDay, todayEpochDay)

        combine(
            repository.getActiveNutritionTarget(),
            repository.getAllCheckIns(),
            repository.getAllMeasurements(),
            if (programId > 0) repository.getPendingReview(programId) else flowOf(null)
        ) { target, checkIns, measurements, pendingReview ->
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
                pendingReview = pendingReview
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    init {
        viewModelScope.launch {
            val active = repository.getActiveProgram().firstOrNull()
            if (active != null) {
                repository.generateBiweeklyReviewIfDue(active.id)
            }
        }
    }

    fun acceptReview(review: BiweeklyReviewEntity) {
        viewModelScope.launch {
            repository.acceptReviewAndApplyNutritionAdjustment(review.id)
        }
    }

    fun rejectReview(reviewId: Long) {
        viewModelScope.launch {
            repository.rejectReview(reviewId)
        }
    }
}
