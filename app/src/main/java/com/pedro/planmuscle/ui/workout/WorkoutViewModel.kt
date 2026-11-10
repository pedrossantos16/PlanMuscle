package com.pedro.planmuscle.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.entity.ExerciseSetLogEntity
import com.pedro.planmuscle.data.local.entity.SessionExercisePrescriptionEntity
import com.pedro.planmuscle.data.local.entity.WorkoutSessionEntity
import com.pedro.planmuscle.domain.engine.ProgressionDecision
import com.pedro.planmuscle.domain.engine.ProgressionEngine
import com.pedro.planmuscle.domain.engine.TrainingPhaseEngine
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExercisePrescriptionUiState(
    val prescription: SessionExercisePrescriptionEntity,
    val logs: List<ExerciseSetLogEntity> = emptyList(),
    val progressionDecision: ProgressionDecision? = null
)

data class WorkoutSessionUiState(
    val session: WorkoutSessionEntity? = null,
    val exercisePrescriptions: List<ExercisePrescriptionUiState> = emptyList(),
    val timerSecondsRemaining: Int = 0,
    val isTimerRunning: Boolean = false,
    val restEndsAtEpochMillis: Long = 0L,
    val isCompleted: Boolean = false,
    val uncompletedSetsWarning: String? = null
)

class WorkoutViewModel(
    private val repository: PlanMuscleRepository,
    private val sessionId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionUiState())
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            repository.getWorkoutSessionById(sessionId).collectLatest { session ->
                if (session != null) {
                    _uiState.update {
                        it.copy(
                            session = session,
                            isCompleted = session.status == "COMPLETED"
                        )
                    }
                    loadPrescriptionsAndLogs(sessionId)
                }
            }
        }
    }

    private fun loadPrescriptionsAndLogs(sessionId: Long) {
        viewModelScope.launch {
            repository.getPrescriptionsForSession(sessionId).collectLatest { prescriptions ->
                repository.getSetLogsForSession(sessionId).collectLatest { allLogs ->
                    val uiPrescriptions = prescriptions.map { prescription ->
                        val logsForPrescription = allLogs.filter { it.sessionExercisePrescriptionId == prescription.id }
                        val lastCompleted = repository.getLastCompletedSetLogsForExercise(prescription.exerciseId)
                        val decision = ProgressionEngine.evaluateProgression(
                            prescription = prescription,
                            lastCompletedWorkSetLogs = lastCompleted,
                            loadIncrementKg = 2.0f
                        )

                        ExercisePrescriptionUiState(
                            prescription = prescription,
                            logs = logsForPrescription,
                            progressionDecision = decision
                        )
                    }
                    _uiState.update { it.copy(exercisePrescriptions = uiPrescriptions) }
                }
            }
        }
    }

    fun saveSetLog(
        prescriptionId: Long,
        exerciseId: String,
        setType: String,
        weightKg: Float,
        reps: Int,
        rir: Int?,
        techniqueOk: Boolean
    ) {
        viewModelScope.launch {
            val prescriptionState = _uiState.value.exercisePrescriptions
                .firstOrNull { it.prescription.id == prescriptionId }
            val nextSetIndex = (prescriptionState?.logs?.size ?: 0) + 1

            val newLog = ExerciseSetLogEntity(
                sessionId = sessionId,
                sessionExercisePrescriptionId = prescriptionId,
                setIndex = nextSetIndex,
                setType = setType,
                weightKg = weightKg,
                reps = reps,
                rir = rir,
                techniqueOk = techniqueOk
            )
            repository.saveSetLog(newLog)

            // Start rest timer after WORK set
            if (setType == "WORK") {
                val restSecs = prescriptionState?.prescription?.restSeconds ?: 90
                startRestTimer(restSecs)
            }
        }
    }

    fun deleteSetLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteSetLog(logId)
        }
    }

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        val targetEnd = System.currentTimeMillis() + (seconds * 1000L)
        _uiState.update {
            it.copy(
                timerSecondsRemaining = seconds,
                isTimerRunning = true,
                restEndsAtEpochMillis = targetEnd
            )
        }

        timerJob = viewModelScope.launch {
            while (true) {
                val remainingMillis = targetEnd - System.currentTimeMillis()
                val remainingSecs = (remainingMillis / 1000L).toInt()
                if (remainingSecs <= 0) {
                    _uiState.update {
                        it.copy(timerSecondsRemaining = 0, isTimerRunning = false)
                    }
                    break
                }
                _uiState.update { it.copy(timerSecondsRemaining = remainingSecs, isTimerRunning = true) }
                delay(1000)
            }
        }
    }

    fun pauseRestTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun finishWorkout(notes: String = "") {
        viewModelScope.launch {
            repository.finishWorkoutSession(sessionId, notes)
            _uiState.update { it.copy(isCompleted = true) }
        }
    }

    fun abandonWorkout() {
        viewModelScope.launch {
            repository.abandonWorkoutSession(sessionId)
            _uiState.update { it.copy(isCompleted = true) }
        }
    }
}
