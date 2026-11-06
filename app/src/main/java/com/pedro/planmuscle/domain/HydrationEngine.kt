package com.pedro.planmuscle.domain

data class HydrationScheduleSlot(
    val timeLabel: String,
    val targetVolumeMl: Int,
    val cumulativeTargetMl: Int,
    val note: String
)

object HydrationEngine {

    val dailySchedule = listOf(
        HydrationScheduleSlot("06:45–09:00", 400, 400, "Ao acordar com creatina e no lanche"),
        HydrationScheduleSlot("09:00–12:15", 400, 800, "Durante expediente da manhã"),
        HydrationScheduleSlot("12:15–15:30", 400, 1200, "No almoço e início da tarde"),
        HydrationScheduleSlot("15:30–18:15", 500, 1700, "Pré-treino e durante musculação na GrowFit"),
        HydrationScheduleSlot("18:15–22:05", 600, 2300, "Durante aulas no SENAI"),
        HydrationScheduleSlot("22:05–23:00", 300, 2600, "Jantar e recuperação em casa")
    )

    fun getExpectedVolumeForTime(hour: Int, minute: Int): Int {
        val timeInMinutes = hour * 60 + minute
        return when {
            timeInMinutes < (9 * 60) -> 400
            timeInMinutes < (12 * 60 + 15) -> 800
            timeInMinutes < (15 * 60 + 30) -> 1200
            timeInMinutes < (18 * 60 + 15) -> 1700
            timeInMinutes < (22 * 60 + 5) -> 2300
            else -> 2600
        }
    }

    fun getHydrationStatusMessage(currentWaterMl: Int, hour: Int, minute: Int): String {
        val expectedMl = getExpectedVolumeForTime(hour, minute)
        val formattedCurrent = String.format(java.util.Locale.US, "%.1f", currentWaterMl / 1000.0f)
        val formattedExpected = String.format(java.util.Locale.US, "%.1f", expectedMl / 1000.0f)

        return if (currentWaterMl >= expectedMl) {
            "Hidratação no alvo: $formattedCurrent / 2.6 L (Esperado: $formattedExpected L)"
        } else {
            val diff = expectedMl - currentWaterMl
            "$formattedCurrent L de $formattedExpected L esperado até agora (-$diff ml)"
        }
    }
}
