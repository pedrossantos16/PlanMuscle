package com.pedro.planmuscle.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.entity.NutritionTargetEntity
import com.pedro.planmuscle.data.preferences.UserPreferencesDataStore
import com.pedro.planmuscle.domain.engine.ExerciseDefinition
import com.pedro.planmuscle.domain.engine.TrainingPhaseEngine
import com.pedro.planmuscle.domain.engine.WorkoutDefinition
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import com.pedro.planmuscle.ui.theme.AppThemeOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val activeProtocol: NutritionTargetEntity? = null,
    val selectedTheme: AppThemeOption = AppThemeOption.NEON_FITNESS,
    val workoutA: WorkoutDefinition = TrainingPhaseEngine.getWorkoutDefinition("A"),
    val workoutB: WorkoutDefinition = TrainingPhaseEngine.getWorkoutDefinition("B")
)

class SettingsViewModel(
    private val repository: PlanMuscleRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _exerciseTrigger = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<SettingsUiState> = combine(
        repository.getActiveNutritionTarget(),
        prefsDataStore.userPreferencesFlow,
        _exerciseTrigger
    ) { target, userPrefs, _ ->
        SettingsUiState(
            activeProtocol = target,
            selectedTheme = userPrefs.themeOption,
            workoutA = TrainingPhaseEngine.getWorkoutDefinition("A"),
            workoutB = TrainingPhaseEngine.getWorkoutDefinition("B")
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun selectTheme(theme: AppThemeOption) {
        viewModelScope.launch {
            prefsDataStore.setThemeOption(theme)
        }
    }

    fun updateProtocolGoals(
        calories: Int,
        protein: Int,
        carbs: Int,
        fats: Int,
        waterMl: Int
    ) {
        viewModelScope.launch {
            repository.updateNutritionTarget(
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                waterMl = waterMl,
                reason = "Alterado nas Configurações"
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
        viewModelScope.launch {
            repository.saveCustomExerciseAndTemplate(
                workoutTemplateId = 1L,
                exerciseId = exerciseId,
                name = name,
                workSets = workSets,
                minReps = minReps,
                maxReps = maxReps,
                restSeconds = restSeconds,
                warmupGuidance = warmupGuidance,
                loadIncrementKg = 2.0f
            )
            _exerciseTrigger.value = System.currentTimeMillis()
        }
    }
}
