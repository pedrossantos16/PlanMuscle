package com.pedro.planmuscle.ui.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.model.MealCheckEntity
import com.pedro.planmuscle.data.model.ProtocolEntity
import com.pedro.planmuscle.data.repository.PlanMuscleRepository
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
    val completed: Boolean
)

data class NutritionUiState(
    val activeProtocol: ProtocolEntity? = null,
    val meals: List<NutritionMealSlot> = emptyList(),
    val totalProteinConsumedGrams: Int = 0
)

class NutritionViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayDate = repository.getTodayDateString()

    val uiState: StateFlow<NutritionUiState> = combine(
        repository.getActiveProtocol(),
        repository.getMealChecksForDate(todayDate)
    ) { protocol, checks ->
        val activeProto = protocol ?: ProtocolEntity(startDate = todayDate)
        val checkMap = checks.associate { it.mealId to it.completed }

        val mealsList = listOf(
            NutritionMealSlot(
                id = "BREAKFAST",
                timeLabel = "06:45",
                title = "Café da Manhã Proteico",
                description = "200ml suco de laranja + 2 ovos inteiros + 2 fatias pão integral + 3–5g Creatina",
                proteinMeta = "18–22 g Proteína",
                completed = checkMap["BREAKFAST"] == true
            ),
            NutritionMealSlot(
                id = "MORNING_SNACK",
                timeLabel = "09:00",
                title = "Lanche da Manhã (Trabalho)",
                description = "1 iogurte proteico + 1 banana + 20g aveia",
                proteinMeta = "15–20 g Proteína",
                completed = checkMap["MORNING_SNACK"] == true
            ),
            NutritionMealSlot(
                id = "LUNCH",
                timeLabel = "12:15",
                title = "Almoço Completo",
                description = "150g arroz + 100g feijão + 150g proteína animal + alface/vegetais à vontade + 1 fruta",
                proteinMeta = "40–50 g Proteína",
                completed = checkMap["LUNCH"] == true
            ),
            NutritionMealSlot(
                id = "PRE_WORKOUT",
                timeLabel = "15:30",
                title = "Lanche Pré-Treino (Trabalho)",
                description = "2 fatias pão integral + 20g pasta de amendoim zero açúcar + 1 dose Whey Protein",
                proteinMeta = "32–38 g Proteína",
                completed = checkMap["PRE_WORKOUT"] == true
            ),
            NutritionMealSlot(
                id = "SENAI_SNACK",
                timeLabel = "20:10",
                title = "Lanche Pós-Treino (SENAI)",
                description = "Sanduíche natural com 100g frango desfiado + 2 fatias pão integral + salada + 1 fruta",
                proteinMeta = "30–35 g Proteína",
                completed = checkMap["SENAI_SNACK"] == true
            ),
            NutritionMealSlot(
                id = "DINNER",
                timeLabel = "22:30",
                title = "Jantar em Casa",
                description = "120g frango ou peixe + 150g batata/arroz + vegetais/salada + azeite de oliva",
                proteinMeta = "28–32 g Proteína",
                completed = checkMap["DINNER"] == true
            )
        )

        NutritionUiState(
            activeProtocol = activeProto,
            meals = mealsList,
            totalProteinConsumedGrams = mealsList.filter { it.completed }.sumOf { 25 } // Estimativa
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NutritionUiState()
    )

    fun toggleMeal(mealId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleMealCheck(todayDate, mealId, completed)
        }
    }
}
