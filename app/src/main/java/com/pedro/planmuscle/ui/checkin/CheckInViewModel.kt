package com.pedro.planmuscle.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedro.planmuscle.data.local.entity.BodyMeasurementEntity
import com.pedro.planmuscle.data.local.entity.DailyCheckInEntity
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CheckInUiState(
    val dateEpochDay: Long = 0L,
    val checkIn: DailyCheckInEntity? = null,
    val measurement: BodyMeasurementEntity? = null,
    val isSaved: Boolean = false
)

class CheckInViewModel(private val repository: PlanMuscleRepository) : ViewModel() {

    val todayEpochDay = repository.getTodayEpochDay()

    val uiState: StateFlow<CheckInUiState> = combine(
        repository.getDailyCheckIn(todayEpochDay),
        repository.getBodyMeasurement(todayEpochDay)
    ) { checkIn, measurement ->
        CheckInUiState(
            dateEpochDay = todayEpochDay,
            checkIn = checkIn,
            measurement = measurement
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckInUiState(dateEpochDay = todayEpochDay)
    )

    fun saveCheckIn(
        weightKg: Float?,
        readinessLevel: Int?,
        sleepMinutes: Int?,
        sleepQuality: Int?,
        unusualPain: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val entity = DailyCheckInEntity(
                dateEpochDay = todayEpochDay,
                weightKg = weightKg,
                readinessLevel = readinessLevel,
                sleepMinutes = sleepMinutes,
                sleepQuality = sleepQuality,
                unusualPain = unusualPain,
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
                dateEpochDay = todayEpochDay,
                waistCm = waistCm,
                armCm = armCm,
                thighCm = thighCm,
                chestCm = chestCm
            )
            repository.saveMeasurement(entity)
        }
    }
}
