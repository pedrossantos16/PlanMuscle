package com.pedro.planmuscle.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.model.BodyMeasurementEntity
import com.pedro.planmuscle.data.model.DailyCheckInEntity
import com.pedro.planmuscle.data.repository.PlanMuscleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckInUiState(
    val date: String = "",
    val checkIn: DailyCheckInEntity? = null,
    val measurement: BodyMeasurementEntity? = null,
    val isSaved: Boolean = false
)

class CheckInViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayDate = repository.getTodayDateString()

    val uiState: StateFlow<CheckInUiState> = combine(
        repository.getDailyCheckIn(todayDate),
        repository.getBodyMeasurement(todayDate)
    ) { checkIn, measurement ->
        CheckInUiState(
            date = todayDate,
            checkIn = checkIn,
            measurement = measurement
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckInUiState(date = todayDate)
    )

    fun saveCheckIn(
        weightKg: Float?,
        readinessLevel: Int?,
        sleepHours: Float?,
        sleepQuality: Int?,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = DailyCheckInEntity(
                date = todayDate,
                weightKg = weightKg,
                readinessLevel = readinessLevel,
                sleepHours = sleepHours,
                sleepQuality = sleepQuality,
                notes = notes
            )
            repository.saveCheckIn(entity)
        }
    }

    fun saveMeasurement(
        waistCm: Float?,
        armCm: Float?,
        thighCm: Float?,
        chestCm: Float?
    ) {
        viewModelScope.launch {
            val entity = BodyMeasurementEntity(
                date = todayDate,
                waistCm = waistCm,
                armCm = armCm,
                thighCm = thighCm,
                chestCm = chestCm
            )
            repository.saveMeasurement(entity)
        }
    }
}
