package com.pedro.planmuscle.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    fun completeOnboarding(
        startDateEpochDay: Long = repository.getTodayEpochDay(),
        calories: Int = 2550,
        protein: Int = 150,
        carbs: Int = 330,
        fats: Int = 70,
        waterMl: Int = 2600
    ) {
        viewModelScope.launch {
            repository.ensureActiveProgramSeeded(startDateEpochDay)
            repository.updateNutritionTarget(
                calories = calories,
                protein = protein,
                carbs = carbs,
                fats = fats,
                waterMl = waterMl,
                reason = "Configuração inicial do Onboarding"
            )
        }
    }
}
