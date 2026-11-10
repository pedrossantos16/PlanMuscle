package com.pedro.planmuscle.domain.engine

import com.pedro.planmuscle.data.local.entity.BiweeklyReviewEntity
import com.pedro.planmuscle.data.local.entity.DailyCheckInEntity
import com.pedro.planmuscle.data.local.entity.WorkoutSessionEntity

object BiweeklyReviewEngine {

    fun generateReview(
        programId: Long,
        periodStartEpochDay: Long,
        periodEndEpochDay: Long,
        checkIns: List<DailyCheckInEntity>,
        completedSessions: List<WorkoutSessionEntity>,
        activeCalories: Int
    ): BiweeklyReviewEntity {
        val midPointEpochDay = periodStartEpochDay + 7

        val week1CheckIns = checkIns.filter { it.dateEpochDay in periodStartEpochDay until midPointEpochDay }
        val week2CheckIns = checkIns.filter { it.dateEpochDay in midPointEpochDay..periodEndEpochDay }

        val week1Weights = week1CheckIns.mapNotNull { it.weightKg }
        val week2Weights = week2CheckIns.mapNotNull { it.weightKg }

        val hasEnoughWeighIns = week1Weights.size >= 3 && week2Weights.size >= 3
        val avgWeek1 = if (week1Weights.isNotEmpty()) week1Weights.average().toFloat() else null
        val avgWeek2 = if (week2Weights.isNotEmpty()) week2Weights.average().toFloat() else null

        val weeklyChange = if (avgWeek1 != null && avgWeek2 != null) (avgWeek2 - avgWeek1) else null

        val scheduledCount = 6 // 3 treinos x 2 semanas
        val completedCount = completedSessions.filter {
            val sessionDay = it.startedAtEpochMillis / 86_400_000L
            sessionDay in periodStartEpochDay..periodEndEpochDay
        }.size

        val trainingAdherence = completedCount.toFloat() / scheduledCount.toFloat()

        val allReadiness = checkIns.mapNotNull { it.readinessLevel }
        val avgReadiness = if (allReadiness.isNotEmpty()) allReadiness.average().toFloat() else null

        if (!hasEnoughWeighIns || trainingAdherence < 0.5f) {
            return BiweeklyReviewEntity(
                trainingProgramId = programId,
                periodStartEpochDay = periodStartEpochDay,
                periodEndEpochDay = periodEndEpochDay,
                status = "NOT_ENOUGH_DATA",
                averageWeightFirstWeek = avgWeek1,
                averageWeightSecondWeek = avgWeek2,
                weeklyWeightChangeKg = weeklyChange,
                averageReadiness = avgReadiness,
                scheduledWorkouts = scheduledCount,
                completedWorkouts = completedCount,
                trainingAdherence = trainingAdherence,
                progressionSummary = "Pesagens e treinos insuficientes nas últimas 2 semanas.",
                recommendationType = "COLLECT_MORE_DATA",
                calorieAdjustment = 0,
                rationale = "São necessárias pelo menos 3 pesagens por semana e boa frequência de treino para recomendações calóricas seguras."
            )
        }

        val rate = weeklyChange ?: 0.0f

        return when {
            rate in -0.05f..0.05f && trainingAdherence >= 0.8f -> {
                BiweeklyReviewEntity(
                    trainingProgramId = programId,
                    periodStartEpochDay = periodStartEpochDay,
                    periodEndEpochDay = periodEndEpochDay,
                    status = "PENDING",
                    averageWeightFirstWeek = avgWeek1,
                    averageWeightSecondWeek = avgWeek2,
                    weeklyWeightChangeKg = weeklyChange,
                    averageReadiness = avgReadiness,
                    scheduledWorkouts = scheduledCount,
                    completedWorkouts = completedCount,
                    trainingAdherence = trainingAdherence,
                    progressionSummary = "Peso estável e boa frequência de treino mantida.",
                    recommendationType = "INCREASE_150_KCAL",
                    calorieAdjustment = 150,
                    rationale = "Seu peso ficou estável por 2 semanas consecutivas com boa frequência. Recomendado aumentar +150 kcal para manter o superávit limpo de hipertrofia."
                )
            }

            rate in 0.08f..0.25f -> {
                BiweeklyReviewEntity(
                    trainingProgramId = programId,
                    periodStartEpochDay = periodStartEpochDay,
                    periodEndEpochDay = periodEndEpochDay,
                    status = "ACCEPTED",
                    averageWeightFirstWeek = avgWeek1,
                    averageWeightSecondWeek = avgWeek2,
                    weeklyWeightChangeKg = weeklyChange,
                    averageReadiness = avgReadiness,
                    scheduledWorkouts = scheduledCount,
                    completedWorkouts = completedCount,
                    trainingAdherence = trainingAdherence,
                    progressionSummary = "Ganho de peso perfeitamente dentro da meta (~100–250g/semana).",
                    recommendationType = "MAINTAIN",
                    calorieAdjustment = 0,
                    rationale = "A taxa de ganho está no ritmo ideal. Mantenha os macronutrientes atuais de $activeCalories kcal!"
                )
            }

            else -> {
                BiweeklyReviewEntity(
                    trainingProgramId = programId,
                    periodStartEpochDay = periodStartEpochDay,
                    periodEndEpochDay = periodEndEpochDay,
                    status = "ACCEPTED",
                    averageWeightFirstWeek = avgWeek1,
                    averageWeightSecondWeek = avgWeek2,
                    weeklyWeightChangeKg = weeklyChange,
                    averageReadiness = avgReadiness,
                    scheduledWorkouts = scheduledCount,
                    completedWorkouts = completedCount,
                    trainingAdherence = trainingAdherence,
                    progressionSummary = "Acompanhamento geral de 14 dias registrado.",
                    recommendationType = "MAINTAIN",
                    calorieAdjustment = 0,
                    rationale = "Continue com as metas atuais e acompanhe o próximo ciclo de 14 dias."
                )
            }
        }
    }
}
