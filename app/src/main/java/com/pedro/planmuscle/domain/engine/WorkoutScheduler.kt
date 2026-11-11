package com.pedro.planmuscle.domain.engine

import java.util.Calendar
import java.util.TimeZone

data class WorkoutScheduleStatus(
    val dateEpochDay: Long,
    val isWorkoutDay: Boolean,
    val plannedWorkoutType: String, // "A" ou "B"
    val scheduleTitle: String,
    val scheduleSubtitle: String,
    val timeWindowLabel: String,
    val isProgramCompleted: Boolean = false
)

object WorkoutScheduler {

    fun getProtocolWeek(startDateEpochDay: Long, targetEpochDay: Long): Int {
        val daysBetween = (targetEpochDay - startDateEpochDay).coerceAtLeast(0)
        val week = (daysBetween / 7).toInt() + 1
        return week.coerceIn(1, 8)
    }

    fun isProgramCompleted(startDateEpochDay: Long, targetEpochDay: Long): Boolean {
        val daysBetween = targetEpochDay - startDateEpochDay
        return daysBetween >= 56 // 8 semanas completadas
    }

    fun isTrainingDay(epochDay: Long): Boolean {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = epochDay * 86_400_000L
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Segunda (2), Quarta (4), Sexta (6)
        return dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.WEDNESDAY || dayOfWeek == Calendar.FRIDAY
    }

    fun determineNextWorkoutType(lastCompletedWorkoutType: String?): String {
        return when (lastCompletedWorkoutType?.uppercase()) {
            "A" -> "B"
            "B" -> "A"
            else -> "A" // Inicia com A
        }
    }

    fun getScheduleStatus(
        startDateEpochDay: Long,
        targetEpochDay: Long,
        lastCompletedWorkoutType: String?
    ): WorkoutScheduleStatus {
        val completed = isProgramCompleted(startDateEpochDay, targetEpochDay)
        val isWorkout = isTrainingDay(targetEpochDay)
        val nextType = determineNextWorkoutType(lastCompletedWorkoutType)

        if (completed) {
            return WorkoutScheduleStatus(
                dateEpochDay = targetEpochDay,
                isWorkoutDay = false,
                plannedWorkoutType = nextType,
                scheduleTitle = "Protocolo de 8 Semanas Concluído 🎉",
                scheduleSubtitle = "Parabéns por completar seu protocolo de hipertrofia! Você pode iniciar um novo ciclo a qualquer momento.",
                timeWindowLabel = "Ciclo Concluído",
                isProgramCompleted = true
            )
        }

        return if (isWorkout) {
            WorkoutScheduleStatus(
                dateEpochDay = targetEpochDay,
                isWorkoutDay = true,
                plannedWorkoutType = nextType,
                scheduleTitle = "Treino $nextType Agendado Hoje",
                scheduleSubtitle = if (nextType == "A")
                    "Foco: Supino reto, Agachamento, Puxada alta, Mesa flexora, Elevação lateral, Rosca direta"
                else
                    "Foco: Leg press, Remada baixa, Supino inclinado, Stiff, Desenvolvimento, Rosca martelo + Tríceps",
                timeWindowLabel = "17:30 – 18:15 na GrowFit Unidade 2",
                isProgramCompleted = false
            )
        } else {
            WorkoutScheduleStatus(
                dateEpochDay = targetEpochDay,
                isWorkoutDay = false,
                plannedWorkoutType = nextType,
                scheduleTitle = "Dia de Recuperação e Descanso",
                scheduleSubtitle = "Próximo treino planejado: Treino $nextType",
                timeWindowLabel = "Recuperação muscular ativa • Próxima sessão na GrowFit",
                isProgramCompleted = false
            )
        }
    }
}
