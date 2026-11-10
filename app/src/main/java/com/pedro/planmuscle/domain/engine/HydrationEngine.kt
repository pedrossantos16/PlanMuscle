package com.pedro.planmuscle.domain.engine

data class HydrationSlot(
    val timeLabel: String,
    val targetVolumeMl: Int,
    val cumulativeTargetMl: Int,
    val note: String
)

object HydrationEngine {

    val defaultSchedule = listOf(
        HydrationSlot("06:45–09:00", 400, 400, "Ao acordar com creatina e no lanche"),
        HydrationSlot("09:00–12:15", 400, 800, "Durante expediente da manhã"),
        HydrationSlot("12:15–15:30", 400, 1200, "No almoço e início da tarde"),
        HydrationSlot("15:30–18:15", 500, 1700, "Pré-treino e durante musculação na GrowFit"),
        HydrationSlot("18:15–22:05", 600, 2300, "Durante aulas no SENAI"),
        HydrationSlot("22:05–23:00", 300, 2600, "Jantar e recuperação em casa")
    )

    fun getInterpolatedExpectedVolume(hour: Int, minute: Int, dailyTargetMl: Int): Int {
        val totalMinutes = hour * 60 + minute
        val ratio = when {
            totalMinutes <= (6 * 60 + 45) -> 0.0f
            totalMinutes >= (22 * 60 + 5) -> 1.0f
            else -> {
                val dayStart = 6 * 60 + 45
                val dayEnd = 22 * 60 + 5
                ((totalMinutes - dayStart).toFloat() / (dayEnd - dayStart).toFloat()).coerceIn(0.0f, 1.0f)
            }
        }
        return (dailyTargetMl * ratio).toInt()
    }

    fun getHydrationStatusMessage(currentWaterMl: Int, hour: Int, minute: Int, dailyTargetMl: Int): String {
        val expectedMl = getInterpolatedExpectedVolume(hour, minute, dailyTargetMl)
        val formattedCurrent = String.format(java.util.Locale.US, "%.1f", currentWaterMl / 1000.0f)
        val formattedTarget = String.format(java.util.Locale.US, "%.1f", dailyTargetMl / 1000.0f)
        val formattedExpected = String.format(java.util.Locale.US, "%.1f", expectedMl / 1000.0f)

        return if (currentWaterMl >= expectedMl) {
            "Hidratação no alvo: $formattedCurrent L de $formattedTarget L (Esperado até agora: $formattedExpected L)"
        } else {
            val diff = expectedMl - currentWaterMl
            "$formattedCurrent L de $formattedExpected L esperado até agora (-$diff ml para o alvo da hora)"
        }
    }
}
