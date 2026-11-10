package com.pedro.planmuscle.domain.engine

import com.pedro.planmuscle.data.local.entity.ExerciseSetLogEntity
import com.pedro.planmuscle.data.local.entity.SessionExercisePrescriptionEntity

enum class ProgressionDecisionType {
    INSUFFICIENT_DATA,
    INCREASE_LOAD,
    MAINTAIN_LOAD,
    REDUCE_LOAD,
    REVIEW_TECHNIQUE
}

data class ProgressionDecision(
    val type: ProgressionDecisionType,
    val suggestedWeightKg: Float?,
    val rationale: String,
    val confidence: String // "ALTA", "MÉDIA", "BAIXA"
)

object ProgressionEngine {

    fun evaluateProgression(
        prescription: SessionExercisePrescriptionEntity,
        lastCompletedWorkSetLogs: List<ExerciseSetLogEntity>,
        loadIncrementKg: Float
    ): ProgressionDecision {
        if (lastCompletedWorkSetLogs.isEmpty()) {
            return ProgressionDecision(
                type = ProgressionDecisionType.INSUFFICIENT_DATA,
                suggestedWeightKg = null,
                rationale = "Primeira execução registrada deste exercício.",
                confidence = "BAIXA"
            )
        }

        val workLogs = lastCompletedWorkSetLogs.filter { it.setType == "WORK" }
        if (workLogs.isEmpty()) {
            return ProgressionDecision(
                type = ProgressionDecisionType.INSUFFICIENT_DATA,
                suggestedWeightKg = null,
                rationale = "Sem séries de trabalho completadas na última execução.",
                confidence = "BAIXA"
            )
        }

        val lastWeight = workLogs.map { it.weightKg }.maxOrNull() ?: 0f
        val allSetsMetMaxReps = workLogs.size >= prescription.prescribedWorkSets &&
                workLogs.all { it.reps >= prescription.maxReps }
        val allTechniqueOk = workLogs.all { it.techniqueOk }
        val anyRirTooLow = workLogs.any { (it.rir ?: 2) < prescription.targetRirMin }
        val anyRepsBelowMin = workLogs.any { it.reps < prescription.minReps }

        return when {
            !allTechniqueOk -> {
                ProgressionDecision(
                    type = ProgressionDecisionType.REVIEW_TECHNIQUE,
                    suggestedWeightKg = lastWeight,
                    rationale = "Técnica foi marcada como inadequada. Mantenha ${lastWeight}kg e priorize a execução limpa.",
                    confidence = "ALTA"
                )
            }

            anyRepsBelowMin || anyRirTooLow -> {
                val reducedWeight = (lastWeight - loadIncrementKg).coerceAtLeast(0f)
                ProgressionDecision(
                    type = ProgressionDecisionType.REDUCE_LOAD,
                    suggestedWeightKg = reducedWeight,
                    rationale = "Repetições abaixo do mínimo ou RIR estourado. Redução recomendada para ${reducedWeight}kg.",
                    confidence = "MÉDIA"
                )
            }

            allSetsMetMaxReps && allTechniqueOk -> {
                val newWeight = lastWeight + loadIncrementKg
                ProgressionDecision(
                    type = ProgressionDecisionType.INCREASE_LOAD,
                    suggestedWeightKg = newWeight,
                    rationale = "Todas as séries atingiram o topo da meta (${prescription.maxReps} reps) com boa técnica! Sugestão: Subir para ${newWeight}kg.",
                    confidence = "ALTA"
                )
            }

            else -> {
                ProgressionDecision(
                    type = ProgressionDecisionType.MAINTAIN_LOAD,
                    suggestedWeightKg = lastWeight,
                    rationale = "Desempenho sólido dentro da faixa (${prescription.minReps}–${prescription.maxReps} reps). Mantenha ${lastWeight}kg até atingir o topo em todas.",
                    confidence = "ALTA"
                )
            }
        }
    }
}
