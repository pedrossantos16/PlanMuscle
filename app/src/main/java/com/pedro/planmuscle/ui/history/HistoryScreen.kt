package com.pedro.planmuscle.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Histórico & Evolução de 8 Semanas",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de Revisão Quinzenal
            state.reviewCard?.let { review ->
                item {
                    BiWeeklyReviewCardSection(
                        review = review,
                        onApplyAdjustment = { calories -> viewModel.applyCalorieAdjustment(calories) }
                    )
                }
            }

            // Histórico de Peso e Média Móvel
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Acompanhamento de Peso (Diário + Média Móvel)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Valores em cinza são o peso diário. O valor em destaque é a média móvel de 7 dias.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(state.weightDataPoints) { point ->
                WeightDataPointRow(point = point)
            }

            // Histórico de Medidas
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Histórico de Medidas Corporais Semanais:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.measurementsHistory) { measurement ->
                MeasurementRow(measurement = measurement)
            }
        }
    }
}

@Composable
fun BiWeeklyReviewCardSection(
    review: BiWeeklyReviewCard,
    onApplyAdjustment: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Revisão Automatizada — Semana ${review.reviewWeekNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            val formattedRate = String.format(java.util.Locale.US, "%.2f", review.avgWeightGainPerWeekKg)
            Text(text = "• Ganho médio de peso: ${formattedRate} kg/semana (${review.weightGainStatus})")
            Text(text = "• Variação de cintura: ${review.waistChangeCm} cm")
            Text(text = "• Disposição média: ${review.avgReadinessScore} / 5.0")

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.recommendationMessage,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            if (review.isAdjustmentRecommended && review.suggestedCalorieChange > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onApplyAdjustment(2550 + review.suggestedCalorieChange) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("APLICAR AJUSTE DE +${review.suggestedCalorieChange} KCAL")
                }
            }
        }
    }
}

@Composable
fun WeightDataPointRow(point: WeightDataPoint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = point.date, style = MaterialTheme.typography.bodyMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Diário: ${point.dailyWeight ?: "-"} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                val avgFormatted = if (point.movingAverage7Days != null)
                    String.format(java.util.Locale.US, "%.2f kg", point.movingAverage7Days)
                else "-"

                Text(
                    text = "Média 7d: $avgFormatted",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MeasurementRow(measurement: com.pedro.planmuscle.data.model.BodyMeasurementEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Data: ${measurement.date}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Cintura: ${measurement.waistCm ?: "-"} cm", style = MaterialTheme.typography.bodySmall)
                Text(text = "Braço: ${measurement.armCm ?: "-"} cm", style = MaterialTheme.typography.bodySmall)
                Text(text = "Coxa: ${measurement.thighCm ?: "-"} cm", style = MaterialTheme.typography.bodySmall)
                Text(text = "Peito: ${measurement.chestCm ?: "-"} cm", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
