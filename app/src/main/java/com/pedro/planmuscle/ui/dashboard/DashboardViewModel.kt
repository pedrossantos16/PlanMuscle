package com.pedro.planmuscle.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.model.MealCheckEntity
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.model.SupplementIntakeEntity
import com.pedro.planmuscle.data.model.WorkoutSessionEntity
import com.pedro.planmuscle.data.repository.PlanMuscleRepository
import com.pedro.planmuscle.domain.HydrationEngine
import com.pedro.planmuscle.domain.TrainingPhaseEngine
import com.pedro.planmuscle.domain.WorkoutScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val todayDate: String = "",
    val activeProtocol: ProtocolEntity? = null,
    val weekNumber: Int = 1,
    val todayWorkoutType: String = "A",
    val workoutSession: WorkoutSessionEntity? = null,
    val totalWaterMl: Int = 0,
    val hydrationMessage: String = "",
    val mealChecks: List<MealCheckEntity> = emptyList(),
    val supplementsLogged: List<SupplementIntakeEntity> = emptyList()
)

class DashboardViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayDate = repository.getTodayDateString()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getActiveProtocol(),
        repository.getTotalWaterForDate(todayDate),
        repository.getWorkoutSessionForDate(todayDate),
        repository.getMealChecksForDate(todayDate),
        repository.getSupplementIntakesForDate(todayDate)
    ) { protocol, totalWater, session, meals, supplements ->
        val activeProto = protocol ?: ProtocolEntity(startDate = todayDate)
        val week = WorkoutScheduler.getCurrentWeekNumber(activeProto.startDate, todayDate)
        val workoutType = WorkoutScheduler.getWorkoutTypeForDate(activeProto.startDate, todayDate)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val waterMl = totalWater ?: 0
        val hydMessage = HydrationEngine.getHydrationStatusMessage(waterMl, hour, minute)

        DashboardUiState(
            todayDate = todayDate,
            activeProtocol = activeProto,
            weekNumber = week,
            todayWorkoutType = workoutType,
            workoutSession = session,
            totalWaterMl = waterMl,
            hydrationMessage = hydMessage,
            mealChecks = meals,
            supplementsLogged = supplements
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(todayDate = todayDate)
    )

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(todayDate, amountMl)
        }
    }

    fun toggleMeal(mealId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleMealCheck(todayDate, mealId, completed)
        }
    }

    fun logSupplement(supplementId: String, amount: String) {
        viewModelScope.launch {
            repository.logSupplement(todayDate, supplementId, amount)
        }
    }

    fun startWorkoutSession(workoutType: String, weekNumber: Int) {
        viewModelScope.launch {
            repository.startWorkoutSession(todayDate, weekNumber, workoutType)
        }
    }
}
