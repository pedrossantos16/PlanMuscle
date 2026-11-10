package com.pedro.planmuscle.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.entity.NutritionTargetEntity
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NutritionMealSlot(
    val id: String,
    val timeLabel: String,
    val title: String,
    val description: String,
    val proteinMeta: String,
    val status: String // "PLANNED", "COMPLETED_AS_PLANNED", "PARTIAL", "SKIPPED"
)

data class NutritionUiState(
    val activeTarget: NutritionTargetEntity? = null,
    val meals: List<NutritionMealSlot> = emptyList(),
    val completedMealsCount: Int = 0,
    val totalMealsCount: Int = 6
)

class NutritionViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayEpochDay = repository.getTodayEpochDay()

    val uiState: StateFlow<NutritionUiState> = combine(
        repository.getActiveNutritionTarget(),
        repository.getMealChecksForDate(todayEpochDay)
    ) { target, checks ->
        val statusMap = checks.associate { it.mealId to it.status }

        val mealsList = listOf(
            NutritionMealSlot(
                id = "BREAKFAST",
                timeLabel = "06:45",
                title = "Café da Manhã Proteico",
                description = "200ml suco de laranja + 2 ovos inteiros + 2 fatias pão integral + 3–5g Creatina",
                proteinMeta = "18–22 g Proteína",
                status = statusMap["BREAKFAST"] ?: "PLANNED"
            ),
            NutritionMealSlot(
                id = "MORNING_SNACK",
                timeLabel = "09:00",
                title = "Lanche da Manhã (Trabalho)",
                description = "1 iogurte proteico + 1 banana + 20g aveia",
                proteinMeta = "15–20 g Proteína",
                status = statusMap["MORNING_SNACK"] ?: "PLANNED"
            ),
            NutritionMealSlot(
                id = "LUNCH",
                timeLabel = "12:15",
                title = "Almoço Completo",
                description = "150g arroz + 100g feijão + 150g proteína animal + alface/vegetais à vontade + 1 fruta",
                proteinMeta = "40–50 g Proteína",
                status = statusMap["LUNCH"] ?: "PLANNED"
            ),
            NutritionMealSlot(
                id = "PRE_WORKOUT",
                timeLabel = "15:30",
                title = "Lanche Pré-Treino (Trabalho)",
                description = "2 fatias pão integral + 20g pasta de amendoim zero açúcar + 1 dose Whey Protein",
                proteinMeta = "32–38 g Proteína",
                status = statusMap["PRE_WORKOUT"] ?: "PLANNED"
            ),
            NutritionMealSlot(
                id = "SENAI_SNACK",
                timeLabel = "20:10",
                title = "Lanche Pós-Treino (SENAI)",
                description = "Sanduíche natural com 100g frango desfiado + 2 fatias pão integral + salada + 1 fruta",
                proteinMeta = "30–35 g Proteína",
                status = statusMap["SENAI_SNACK"] ?: "PLANNED"
            ),
            NutritionMealSlot(
                id = "DINNER",
                timeLabel = "22:30",
                title = "Jantar em Casa",
                description = "120g frango ou peixe + 150g batata/arroz + vegetais/salada + azeite de oliva",
                proteinMeta = "28–32 g Proteína",
                status = statusMap["DINNER"] ?: "PLANNED"
            )
        )

        val completedCount = mealsList.count { it.status == "COMPLETED_AS_PLANNED" }

        NutritionUiState(
            activeTarget = target,
            meals = mealsList,
            completedMealsCount = completedCount,
            totalMealsCount = mealsList.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NutritionUiState()
    )

    fun updateMealStatus(mealId: String, status: String) {
        viewModelScope.launch {
            repository.updateMealCheck(todayEpochDay, mealId, status)
        }
    }
}
