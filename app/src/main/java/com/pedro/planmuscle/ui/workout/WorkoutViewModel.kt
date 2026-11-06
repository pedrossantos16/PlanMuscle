package com.pedro.planmuscle.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.model.ExerciseSetLogEntity
import com.pedro.planmuscle.data.model.WorkoutSessionEntity
import com.pedro.planmuscle.data.repository.PlanMuscleRepository
import com.pedro.planmuscle.domain.ExerciseDefinition
import com.pedro.planmuscle.domain.TrainingPhase
import com.pedro.planmuscle.domain.TrainingPhaseEngine
import com.pedro.planmuscle.domain.WorkoutDefinition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseState(
    val definition: ExerciseDefinition,
    val logs: List<ExerciseSetLogEntity> = emptyList(),
    val previousPerformance: String = "Sem registro anterior",
    val suggestedProgression: String = ""
)

data class WorkoutUiState(
    val session: WorkoutSessionEntity? = null,
    val workoutDefinition: WorkoutDefinition = TrainingPhaseEngine.WorkoutA,
    val currentPhase: TrainingPhase = TrainingPhaseEngine.getPhaseForWeek(1),
    val exerciseStates: List<ExerciseState> = emptyList(),
    val timerSecondsRemaining: Int = 0,
    val isTimerRunning: Boolean = false,
    val isCompleted: Boolean = false
)

class WorkoutViewModel(
    private val repository: PlanMuscleRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            repository.getWorkoutSessionById(sessionId).collectLatest { session ->
                if (session != null) {
                    val def = TrainingPhaseEngine.getWorkoutDefinition(session.workoutType)
                    val phase = TrainingPhaseEngine.getPhaseForWeek(session.weekNumber)

                    _uiState.update {
                        it.copy(
                            session = session,
                            workoutDefinition = def,
                            currentPhase = phase,
                            isCompleted = session.status == "COMPLETED"
                        )
                    }

                    loadSetLogs(sessionId, def)
                }
            }
        }
    }

    private fun loadSetLogs(sessionId: Long, definition: WorkoutDefinition) {
        viewModelScope.launch {
            repository.getSetLogsForSession(sessionId).collectLatest { allLogs ->
                val exerciseStates = definition.exercises.map { exerciseDef ->
                    val logsForEx = allLogs.filter { it.exerciseId == exerciseDef.id }
                    ExerciseState(
                        definition = exerciseDef,
                        logs = logsForEx,
                        previousPerformance = "Pronto para executar",
                        suggestedProgression = TrainingPhaseEngine.getSuggestedProgression(
                            lastWeightKg = logsForEx.lastOrNull { it.setType == "WORK" }?.weightKg,
                            lastReps = logsForEx.lastOrNull { it.setType == "WORK" }?.reps,
                            targetRepsMax = exerciseDef.targetRepsMax
                        )
                    )
                }
                _uiState.update { it.copy(exerciseStates = exerciseStates) }
            }
        }
    }

    fun saveSetLog(
        exerciseId: String,
        exerciseName: String,
        setType: String,
        weightKg: Float,
        reps: Int,
        rir: Int?
    ) {
        viewModelScope.launch {
            val currentLogs = _uiState.value.exerciseStates
                .firstOrNull { it.definition.id == exerciseId }?.logs ?: emptyList()

            val nextSetNumber = currentLogs.size + 1

            val newLog = ExerciseSetLogEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                setNumber = nextSetNumber,
                setType = setType,
                weightKg = weightKg,
                reps = reps,
                rir = rir
            )
            repository.saveSetLog(newLog)

            // Start rest timer automatically after saving a work set
            val restTime = _uiState.value.exerciseStates
                .firstOrNull { it.definition.id == exerciseId }?.definition?.restTimeSeconds ?: 90
            startRestTimer(restTime)
        }
    }

    fun deleteSetLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteSetLog(logId)
        }
    }

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _uiState.update { it.copy(timerSecondsRemaining = seconds, isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timerSecondsRemaining > 0) {
                delay(1000)
                _uiState.update { state ->
                    val remaining = state.timerSecondsRemaining - 1
                    state.copy(
                        timerSecondsRemaining = remaining,
                        isTimerRunning = remaining > 0
                    )
                }
            }
        }
    }

    fun pauseRestTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            repository.finishWorkoutSession(sessionId)
            _uiState.update { it.copy(isCompleted = true) }
        }
    }
}
