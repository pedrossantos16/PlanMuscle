package com.pedro.planmuscle.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.repository.PlanMuscleRepository
import com.pedro.planmuscle.domain.ExerciseDefinition
import com.pedro.planmuscle.domain.TrainingPhaseEngine
import com.pedro.planmuscle.domain.WorkoutDefinition
import com.pedro.planmuscle.ui.theme.AppThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeProtocol: ProtocolEntity? = null,
    val selectedTheme: AppThemeOption = AppThemeOption.NEON_FITNESS,
    val workoutA: WorkoutDefinition = TrainingPhaseEngine.getWorkoutDefinition("A"),
    val workoutB: WorkoutDefinition = TrainingPhaseEngine.getWorkoutDefinition("B")
)

class SettingsViewModel(
    private val repository: PlanMuscleRepository,
    context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("plan_muscle_prefs", Context.MODE_PRIVATE)

    private val _selectedTheme = MutableStateFlow(
        try {
            val themeName = prefs.getString("selected_theme", AppThemeOption.NEON_FITNESS.name)
            AppThemeOption.valueOf(themeName ?: AppThemeOption.NEON_FITNESS.name)
        } catch (e: Exception) {
            AppThemeOption.NEON_FITNESS
        }
    )

    private val _exerciseTrigger = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getActiveProtocol(),
        _selectedTheme,
        _exerciseTrigger
    ) { protocol, theme, _ ->
        SettingsUiState(
            activeProtocol = protocol ?: ProtocolEntity(startDate = repository.getTodayDateString()),
            selectedTheme = theme,
            workoutA = TrainingPhaseEngine.getWorkoutDefinition("A"),
            workoutB = TrainingPhaseEngine.getWorkoutDefinition("B")
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun selectTheme(theme: AppThemeOption) {
        _selectedTheme.value = theme
        prefs.edit().putString("selected_theme", theme.name).apply()
    }

    fun updateProtocolGoals(
        calories: Int,
        protein: Int,
        carbs: Int,
        fats: Int,
        waterMl: Int
    ) {
        viewModelScope.launch {
            repository.createNewProtocol(
                calorieTarget = calories,
                proteinTarget = protein,
                carbTarget = carbs,
                fatTarget = fats
            )
        }
    }

    fun saveCustomExercise(
        exerciseId: String,
        name: String,
        workSets: Int,
        minReps: Int,
        maxReps: Int,
        restSeconds: Int,
        warmupGuidance: String
    ) {
        val customEx = ExerciseDefinition(
            id = exerciseId,
            name = name,
            targetRepsMin = minReps,
            targetRepsMax = maxReps,
            defaultWorkSets = workSets,
            restTimeSeconds = restSeconds,
            warmupGuidance = warmupGuidance
        )
        TrainingPhaseEngine.updateExerciseDefinition(customEx)
        _exerciseTrigger.value = System.currentTimeMillis()
    }
}
