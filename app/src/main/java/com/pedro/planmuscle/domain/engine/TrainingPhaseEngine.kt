package com.pedro.planmuscle.domain.engine

import com.pedro.planmuscle.data.local.entity.ExerciseEntity

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

data class PhaseInfo(
    val weekNumber: Int,
    val workSetsOverride: Int?, // 2 nas semanas 1-2; null (usa padrão) nas semanas 3-8
    val targetRirMin: Int,
    val targetRirMax: Int,
    val title: String,
    val description: String
)

object TrainingPhaseEngine {

    private val customOverrides = mutableMapOf<String, ExerciseDefinition>()

    fun updateExerciseDefinition(exercise: ExerciseDefinition) {
        customOverrides[exercise.id] = exercise
    }

    fun getPhaseForWeek(weekNumber: Int): PhaseInfo {
        val clampedWeek = weekNumber.coerceIn(1, 8)
        return if (clampedWeek <= 2) {
            PhaseInfo(
                weekNumber = clampedWeek,
                workSetsOverride = 2,
                targetRirMin = 3,
                targetRirMax = 3,
                title = "Fase 1: Readaptação e Controle",
                description = "Semanas 1–2: 2 séries de trabalho por exercício e RIR 3. Foque na técnica e controle de execução."
            )
        } else {
            PhaseInfo(
                weekNumber = clampedWeek,
                workSetsOverride = null,
                targetRirMin = 1,
                targetRirMax = 2,
                title = "Fase 2: Progressão de Volume e Carga",
                description = "Semanas 3–8: Séries completas do protocolo e RIR 1–2. Busque progressão de cargas mantendo a boa técnica."
            )
        }
    }

    val WorkoutA = WorkoutDefinition(
        type = "A",
        title = "Treino A — Ênfase Peitoral, Quadríceps e Costas",
        targetMuscles = "Peitoral, Quadríceps, Dorsais, Flexores de Coxa, Ombros, Bíceps",
        exercises = listOf(
            ExerciseDefinition(id = "supino_reto", name = "Supino reto com barra", targetRepsMin = 6, targetRepsMax = 10, defaultWorkSets = 3, restTimeSeconds = 150, warmupGuidance = "2–3 séries progressivas"),
            ExerciseDefinition(id = "agachamento_livre", name = "Agachamento livre", targetRepsMin = 8, targetRepsMax = 10, defaultWorkSets = 3, restTimeSeconds = 150, warmupGuidance = "2 séries progressivas"),
            ExerciseDefinition(id = "puxada_alta", name = "Puxada alta na polia", targetRepsMin = 8, targetRepsMax = 12, defaultWorkSets = 3, restTimeSeconds = 120, warmupGuidance = "1 série leve de adaptação"),
            ExerciseDefinition(id = "mesa_flexora", name = "Mesa flexora", targetRepsMin = 10, targetRepsMax = 15, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1 série leve"),
            ExerciseDefinition(id = "elevacao_lateral", name = "Elevação lateral com halteres", targetRepsMin = 12, targetRepsMax = 15, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1 série leve"),
            ExerciseDefinition(id = "rosca_direta", name = "Rosca direta", targetRepsMin = 10, targetRepsMax = 12, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1 série leve")
        )
    )

    val WorkoutB = WorkoutDefinition(
        type = "B",
        title = "Treino B — Ênfase Posteriores, Ombros e Braços",
        targetMuscles = "Quadríceps, Dorsais, Peitoral Inclinado, Posteriores, Ombros, Braços",
        exercises = listOf(
            ExerciseDefinition(id = "leg_press", name = "Leg press 45°", targetRepsMin = 10, targetRepsMax = 12, defaultWorkSets = 3, restTimeSeconds = 150, warmupGuidance = "2 séries progressivas"),
            ExerciseDefinition(id = "remada_baixa", name = "Remada baixa com triângulo", targetRepsMin = 8, targetRepsMax = 12, defaultWorkSets = 3, restTimeSeconds = 120, warmupGuidance = "1–2 séries leves"),
            ExerciseDefinition(id = "supino_inclinado", name = "Supino inclinado com halteres", targetRepsMin = 8, targetRepsMax = 12, defaultWorkSets = 3, restTimeSeconds = 120, warmupGuidance = "1–2 séries leves"),
            ExerciseDefinition(id = "stiff_halteres", name = "Stiff com halteres", targetRepsMin = 10, targetRepsMax = 12, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1–2 séries leves"),
            ExerciseDefinition(id = "desenvolvimento_halteres", name = "Desenvolvimento com halteres", targetRepsMin = 8, targetRepsMax = 12, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1 série leve"),
            ExerciseDefinition(id = "rosca_martelo", name = "Rosca martelo", targetRepsMin = 10, targetRepsMax = 12, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1 série leve", isSuperSet = true),
            ExerciseDefinition(id = "triceps_polia", name = "Tríceps na polia", targetRepsMin = 10, targetRepsMax = 12, defaultWorkSets = 2, restTimeSeconds = 90, warmupGuidance = "1 série leve", isSuperSet = true)
        )
    )

    fun getWorkoutDefinition(type: String): WorkoutDefinition {
        val baseDef = if (type.uppercase() == "B") WorkoutB else WorkoutA
        val updatedExercises = baseDef.exercises.map { baseEx ->
            customOverrides[baseEx.id] ?: baseEx
        }
        return baseDef.copy(exercises = updatedExercises)
    }

    val defaultExercises = listOf(
        ExerciseEntity(id = "supino_reto", name = "Supino reto com barra", primaryMuscleGroup = "Peitoral", secondaryMuscleGroups = "Tríceps, Ombro", equipment = "Barra", isCompound = true, defaultLoadIncrementKg = 2.0f),
        ExerciseEntity(id = "agachamento_livre", name = "Agachamento livre", primaryMuscleGroup = "Quadríceps", secondaryMuscleGroups = "Glúteo, Core", equipment = "Barra", isCompound = true, defaultLoadIncrementKg = 2.5f),
        ExerciseEntity(id = "puxada_alta", name = "Puxada alta na polia", primaryMuscleGroup = "Dorsais", secondaryMuscleGroups = "Bíceps", equipment = "Polia", isCompound = true, defaultLoadIncrementKg = 2.5f),
        ExerciseEntity(id = "mesa_flexora", name = "Mesa flexora", primaryMuscleGroup = "Posteriores", secondaryMuscleGroups = "", equipment = "Máquina", isCompound = false, defaultLoadIncrementKg = 2.5f),
        ExerciseEntity(id = "elevacao_lateral", name = "Elevação lateral com halteres", primaryMuscleGroup = "Deltoide Lateral", secondaryMuscleGroups = "", equipment = "Halteres", isCompound = false, defaultLoadIncrementKg = 1.0f),
        ExerciseEntity(id = "rosca_direta", name = "Rosca direta", primaryMuscleGroup = "Bíceps", secondaryMuscleGroups = "", equipment = "Barra W / Halteres", isCompound = false, defaultLoadIncrementKg = 1.0f),

        ExerciseEntity(id = "leg_press", name = "Leg press 45°", primaryMuscleGroup = "Quadríceps", secondaryMuscleGroups = "Glúteo", equipment = "Máquina", isCompound = true, defaultLoadIncrementKg = 5.0f),
        ExerciseEntity(id = "remada_baixa", name = "Remada baixa com triângulo", primaryMuscleGroup = "Dorsais", secondaryMuscleGroups = "Bíceps", equipment = "Polia", isCompound = true, defaultLoadIncrementKg = 2.5f),
        ExerciseEntity(id = "supino_inclinado", name = "Supino inclinado com halteres", primaryMuscleGroup = "Peitoral Superior", secondaryMuscleGroups = "Ombro, Tríceps", equipment = "Halteres", isCompound = true, defaultLoadIncrementKg = 2.0f),
        ExerciseEntity(id = "stiff_halteres", name = "Stiff com halteres", primaryMuscleGroup = "Posteriores", secondaryMuscleGroups = "Glúteo", equipment = "Halteres", isCompound = true, defaultLoadIncrementKg = 2.0f),
        ExerciseEntity(id = "desenvolvimento_halteres", name = "Desenvolvimento com halteres", primaryMuscleGroup = "Deltoide Anterior", secondaryMuscleGroups = "Tríceps", equipment = "Halteres", isCompound = true, defaultLoadIncrementKg = 2.0f),
        ExerciseEntity(id = "rosca_martelo", name = "Rosca martelo", primaryMuscleGroup = "Bíceps/Braquial", secondaryMuscleGroups = "", equipment = "Halteres", isCompound = false, defaultLoadIncrementKg = 1.0f),
        ExerciseEntity(id = "triceps_polia", name = "Tríceps na polia", primaryMuscleGroup = "Tríceps", secondaryMuscleGroups = "", equipment = "Polia", isCompound = false, defaultLoadIncrementKg = 2.5f)
    )
}
