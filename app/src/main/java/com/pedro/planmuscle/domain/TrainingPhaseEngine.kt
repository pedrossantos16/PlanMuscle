package com.pedro.planmuscle.domain

data class ExerciseDefinition(
    val id: String,
    val name: String,
    val targetRepsMin: Int,
    val targetRepsMax: Int,
    val defaultWorkSets: Int,
    val restTimeSeconds: Int,
    val warmupGuidance: String,
    val isSuperSet: Boolean = false
)

data class WorkoutDefinition(
    val type: String, // "A" ou "B"
    val title: String,
    val targetMuscles: String,
    val exercises: List<ExerciseDefinition>
)

data class TrainingPhase(
    val weekNumber: Int,
    val workSetsOverride: Int?, // 2 nas semanas 1-2; null (usa padrão) nas semanas 3-8
    val targetRirMin: Int,
    val targetRirMax: Int,
    val description: String
)

object TrainingPhaseEngine {

    private val customOverrides = mutableMapOf<String, ExerciseDefinition>()

    fun updateExerciseDefinition(exercise: ExerciseDefinition) {
        customOverrides[exercise.id] = exercise
    }

    val WorkoutA = WorkoutDefinition(
        type = "A",
        title = "Treino A — Ênfase Peitoral, Quadríceps e Costas",
        targetMuscles = "Peitoral, Quadríceps, Dorsais, Flexores de Coxa, Ombros, Bíceps",
        exercises = listOf(
            ExerciseDefinition(
                id = "supino_reto",
                name = "Supino reto com barra",
                targetRepsMin = 6,
                targetRepsMax = 10,
                defaultWorkSets = 3,
                restTimeSeconds = 150,
                warmupGuidance = "2–3 séries progressivas (Ex: barra livre x12, 60% x6, 80% x3)"
            ),
            ExerciseDefinition(
                id = "agachamento_livre",
                name = "Agachamento livre",
                targetRepsMin = 8,
                targetRepsMax = 10,
                defaultWorkSets = 3,
                restTimeSeconds = 150,
                warmupGuidance = "2 séries progressivas de aproximação"
            ),
            ExerciseDefinition(
                id = "puxada_alta",
                name = "Puxada alta na polia",
                targetRepsMin = 8,
                targetRepsMax = 12,
                defaultWorkSets = 3,
                restTimeSeconds = 120,
                warmupGuidance = "1 série leve de adaptação"
            ),
            ExerciseDefinition(
                id = "mesa_flexora",
                name = "Mesa flexora",
                targetRepsMin = 10,
                targetRepsMax = 15,
                defaultWorkSets = 2,
                restTimeSeconds = 90,
                warmupGuidance = "1 série leve"
            ),
            ExerciseDefinition(
                id = "elevacao_lateral",
                name = "Elevação lateral com halteres",
                targetRepsMin = 12,
                targetRepsMax = 15,
                defaultWorkSets = 2,
                restTimeSeconds = 90,
                warmupGuidance = "1 série leve"
            ),
            ExerciseDefinition(
                id = "rosca_direta",
                name = "Rosca direta com barra/halteres",
                targetRepsMin = 10,
                targetRepsMax = 12,
                defaultWorkSets = 2,
                restTimeSeconds = 90,
                warmupGuidance = "1 série leve"
            )
        )
    )

    val WorkoutB = WorkoutDefinition(
        type = "B",
        title = "Treino B — Ênfase Posteriores, Ombros e Braços",
        targetMuscles = "Quadríceps, Dorsais, Peitoral Inclinado, Posteriores, Ombros, Braços",
        exercises = listOf(
            ExerciseDefinition(
                id = "leg_press",
                name = "Leg press 45°",
                targetRepsMin = 10,
                targetRepsMax = 12,
                defaultWorkSets = 3,
                restTimeSeconds = 150,
                warmupGuidance = "2 séries progressivas"
            ),
            ExerciseDefinition(
                id = "remada_baixa",
                name = "Remada baixa com triângulo/barra",
                targetRepsMin = 8,
                targetRepsMax = 12,
                defaultWorkSets = 3,
                restTimeSeconds = 120,
                warmupGuidance = "1–2 séries leves"
            ),
            ExerciseDefinition(
                id = "supino_inclinado",
                name = "Supino inclinado com halteres",
                targetRepsMin = 8,
                targetRepsMax = 12,
                defaultWorkSets = 3,
                restTimeSeconds = 120,
                warmupGuidance = "1–2 séries leves"
            ),
            ExerciseDefinition(
                id = "stiff_halteres",
                name = "Stiff com halteres",
                targetRepsMin = 10,
                targetRepsMax = 12,
                defaultWorkSets = 2,
                restTimeSeconds = 90,
                warmupGuidance = "1–2 séries leves"
            ),
            ExerciseDefinition(
                id = "desenvolvimento_halteres",
                name = "Desenvolvimento com halteres",
                targetRepsMin = 8,
                targetRepsMax = 12,
                defaultWorkSets = 2,
                restTimeSeconds = 90,
                warmupGuidance = "1 série leve"
            ),
            ExerciseDefinition(
                id = "bi_set_bracos",
                name = "Supersérie: Rosca martelo + Tríceps na polia",
                targetRepsMin = 10,
                targetRepsMax = 12,
                defaultWorkSets = 2,
                restTimeSeconds = 90,
                warmupGuidance = "1 série leve de cada exercício",
                isSuperSet = true
            )
        )
    )

    fun getPhaseForWeek(weekNumber: Int): TrainingPhase {
        return if (weekNumber <= 2) {
            TrainingPhase(
                weekNumber = weekNumber,
                workSetsOverride = 2,
                targetRirMin = 3,
                targetRirMax = 3,
                description = "Fase 1 (Semanas 1–2): Readaptação e controle de carga. 2 séries de trabalho por exercício e RIR 3."
            )
        } else {
            TrainingPhase(
                weekNumber = weekNumber,
                workSetsOverride = null,
                targetRirMin = 1,
                targetRirMax = 2,
                description = "Fase 2 (Semanas 3–8): Progressão de volume e carga. Séries completas do protocolo e RIR 1–2."
            )
        }
    }

    fun getWorkoutDefinition(type: String): WorkoutDefinition {
        val baseDef = if (type.uppercase() == "B") WorkoutB else WorkoutA
        val updatedExercises = baseDef.exercises.map { baseEx ->
            customOverrides[baseEx.id] ?: baseEx
        }
        return baseDef.copy(exercises = updatedExercises)
    }

    fun getSuggestedProgression(
        lastWeightKg: Float?,
        lastReps: Int?,
        targetRepsMax: Int
    ): String {
        if (lastWeightKg == null || lastReps == null) {
            return "Primeira execução do exercício"
        }
        return if (lastReps >= targetRepsMax) {
            val suggested = lastWeightKg + 2.0f
            "Atingiu o topo da meta ($lastReps/$targetRepsMax reps). Sugestão: Aumentar para ${suggested.toInt()} kg!"
        } else {
            "Manter ${lastWeightKg.toInt()} kg e buscar mais repetições (Último: $lastReps reps)"
        }
    }
}
