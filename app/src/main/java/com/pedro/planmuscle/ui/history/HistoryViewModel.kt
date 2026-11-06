package com.pedro.planmuscle.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.model.BodyMeasurementEntity
import com.pedro.planmuscle.data.model.DailyCheckInEntity
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.repository.PlanMuscleRepository
import com.pedro.planmuscle.domain.WorkoutScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeightDataPoint(
    val date: String,
    val dailyWeight: Float?,
    val movingAverage7Days: Float?
)

data class BiWeeklyReviewCard(
    val reviewWeekNumber: Int,
    val avgWeightGainPerWeekKg: Float,
    val weightGainStatus: String, // "No alvo (~100-250g/sem)", "Lento", "Muito rápido"
    val waistChangeCm: Float,
    val avgReadinessScore: Float,
    val isAdjustmentRecommended: Boolean,
    val recommendationMessage: String,
    val suggestedCalorieChange: Int
)

data class HistoryUiState(
    val activeProtocol: ProtocolEntity? = null,
    val currentWeekNumber: Int = 1,
    val weightDataPoints: List<WeightDataPoint> = emptyList(),
    val measurementsHistory: List<BodyMeasurementEntity> = emptyList(),
    val reviewCard: BiWeeklyReviewCard? = null
)

class HistoryViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayDate = repository.getTodayDateString()

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.getActiveProtocol(),
        repository.getAllCheckIns(),
        repository.getAllMeasurements()
    ) { protocol, checkIns, measurements ->
        val activeProto = protocol ?: ProtocolEntity(startDate = todayDate)
        val week = WorkoutScheduler.getCurrentWeekNumber(activeProto.startDate, todayDate)

        // Compute 7-day moving average for weights
        val sortedCheckIns = checkIns.sortedBy { it.date }
        val points = sortedCheckIns.mapIndexed { index, checkIn ->
            val last7 = sortedCheckIns.subList((index - 6).coerceAtLeast(0), index + 1)
                .mapNotNull { it.weightKg }
            val avg = if (last7.isNotEmpty()) last7.average().toFloat() else null

            WeightDataPoint(
                date = checkIn.date,
                dailyWeight = checkIn.weightKg,
                movingAverage7Days = avg
            )
        }

        // Generate Bi-Weekly Review Card if enough data exists
        val review = generateReviewCard(week, sortedCheckIns, measurements)

        HistoryUiState(
            activeProtocol = activeProto,
            currentWeekNumber = week,
            weightDataPoints = points,
            measurementsHistory = measurements.sortedByDescending { it.date },
            reviewCard = review
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    private fun generateReviewCard(
        currentWeek: Int,
        checkIns: List<DailyCheckInEntity>,
        measurements: List<BodyMeasurementEntity>
    ): BiWeeklyReviewCard? {
        if (checkIns.size < 7) return null

        val weights = checkIns.mapNotNull { it.weightKg }
        val firstWeight = weights.firstOrNull() ?: 76.0f
        val lastWeight = weights.lastOrNull() ?: 76.0f
        val weeksDiff = (checkIns.size / 7).coerceAtLeast(1)
        val ratePerWeek = (lastWeight - firstWeight) / weeksDiff

        val status = when {
            ratePerWeek in 0.10f..0.25f -> "No alvo (~100–250g/semana) ✓"
            ratePerWeek < 0.10f -> "Abaixo da meta de ganho de massa"
            else -> "Acima do esperado (Risco de acúmulo de gordura)"
        }

        val firstWaist = measurements.lastOrNull()?.waistCm ?: 0.0f
        val lastWaist = measurements.firstOrNull()?.waistCm ?: 0.0f
        val waistDiff = if (firstWaist > 0 && lastWaist > 0) lastWaist - firstWaist else 0.0f

        val readinessList = checkIns.mapNotNull { it.readinessLevel }
        val avgReadiness = if (readinessList.isNotEmpty()) readinessList.average().toFloat() else 4.0f

        val isStable = ratePerWeek < 0.05f
        val recommendedCalorieChange = if (isStable) 150 else 0

        return BiWeeklyReviewCard(
            reviewWeekNumber = (currentWeek / 2) * 2,
            avgWeightGainPerWeekKg = ratePerWeek,
            weightGainStatus = status,
            waistChangeCm = waistDiff,
            avgReadinessScore = avgReadiness,
            isAdjustmentRecommended = isStable,
            recommendationMessage = if (isStable) {
                "Seu peso ficou estável nas últimas semanas. Recomendado aumentar +150 kcal para manter o superávit e acelerar a hipertrofia."
            } else {
                "Sua evolução está excelente. Mantenha os macronutrientes atuais!"
            },
            suggestedCalorieChange = recommendedCalorieChange
        )
    }

    fun applyCalorieAdjustment(newCalories: Int) {
        viewModelScope.launch {
            repository.createNewProtocol(
                calorieTarget = newCalories
            )
        }
    }
}
