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
        val t = hour * 60 + minute
        val scale = dailyTargetMl / 2600.0f

        val rawExpected = when {
            t <= 405 -> 0.0f // < 06:45
            t <= 540 -> 0.0f + 400.0f * ((t - 405).toFloat() / 135.0f) // 06:45 - 09:00
            t <= 735 -> 400.0f + 400.0f * ((t - 540).toFloat() / 195.0f) // 09:00 - 12:15
            t <= 930 -> 800.0f + 400.0f * ((t - 735).toFloat() / 195.0f) // 12:15 - 15:30
            t <= 1095 -> 1200.0f + 500.0f * ((t - 930).toFloat() / 165.0f) // 15:30 - 18:15
            t <= 1325 -> 1700.0f + 600.0f * ((t - 1095).toFloat() / 230.0f) // 18:15 - 22:05
            t <= 1380 -> 2300.0f + 300.0f * ((t - 1325).toFloat() / 55.0f) // 22:05 - 23:00
            else -> 2600.0f
        }

        return (rawExpected * scale).toInt()
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
