package com.pedro.planmuscle.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.entity.MealCheckEntity
import com.pedro.planmuscle.data.local.entity.NutritionTargetEntity
import com.pedro.planmuscle.data.local.entity.SupplementIntakeEntity
import com.pedro.planmuscle.data.local.entity.WorkoutSessionEntity
import com.pedro.planmuscle.domain.engine.HydrationEngine
import com.pedro.planmuscle.domain.engine.WorkoutScheduler
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val todayDateString: String = "",
    val todayEpochDay: Long = 0L,
    val activeNutritionTarget: NutritionTargetEntity? = null,
    val weekNumber: Int = 1,
    val todayWorkoutType: String = "A",
    val scheduleTitle: String = "",
    val scheduleSubtitle: String = "",
    val isWorkoutDay: Boolean = true,
    val workoutSession: WorkoutSessionEntity? = null,
    val totalWaterMl: Int = 0,
    val hydrationMessage: String = "",
    val mealChecks: List<MealCheckEntity> = emptyList(),
    val supplementsLogged: List<SupplementIntakeEntity> = emptyList()
)

class DashboardViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayEpochDay = repository.getTodayEpochDay()
    val todayDateString = repository.getTodayDateString()

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getActiveProgram(),
        repository.getActiveNutritionTarget(),
        repository.getTotalWaterForDate(todayEpochDay),
        repository.getMealChecksForDate(todayEpochDay),
        repository.getSupplementIntakesForDate(todayEpochDay)
    ) { program, target, totalWater, meals, supplements ->
        val startDay = program?.startDateEpochDay ?: todayEpochDay
        val week = WorkoutScheduler.getProtocolWeek(startDay, todayEpochDay)
        val schedule = WorkoutScheduler.getScheduleStatus(startDay, todayEpochDay, null)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val activeTarget = target ?: NutritionTargetEntity(validFromEpochDay = todayEpochDay)
        val waterMl = totalWater ?: 0
        val hydMessage = HydrationEngine.getHydrationStatusMessage(waterMl, hour, minute, activeTarget.waterTargetMl)

        DashboardUiState(
            todayDateString = todayDateString,
            todayEpochDay = todayEpochDay,
            activeNutritionTarget = activeTarget,
            weekNumber = week,
            todayWorkoutType = schedule.plannedWorkoutType,
            scheduleTitle = schedule.scheduleTitle,
            scheduleSubtitle = schedule.scheduleSubtitle,
            isWorkoutDay = schedule.isWorkoutDay,
            workoutSession = null,
            totalWaterMl = waterMl,
            hydrationMessage = hydMessage,
            mealChecks = meals,
            supplementsLogged = supplements
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(todayDateString = todayDateString, todayEpochDay = todayEpochDay)
    )

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(todayEpochDay, amountMl)
        }
    }

    fun deleteLastWaterEntry() {
        viewModelScope.launch {
            repository.deleteLastWaterEntry(todayEpochDay)
        }
    }

    fun updateMealCheck(mealId: String, status: String) {
        viewModelScope.launch {
            repository.updateMealCheck(todayEpochDay, mealId, status)
        }
    }

    fun toggleSupplement(supplementId: String, amount: String) {
        viewModelScope.launch {
            repository.toggleSupplementIntake(todayEpochDay, supplementId, amount)
        }
    }

    suspend fun startOrResumeWorkoutSession(): Long {
        return repository.startOrResumeWorkoutSession(todayEpochDay)
    }
}
