package com.pedro.planmuscle.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pedro.planmuscle.data.local.entity.NutritionTargetEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartWorkout: (sessionId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PlanMuscle — Painel Diário",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Semana ${state.weekNumber} de 8 • Data: ${state.todayDateString}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            // Card do Próximo Treino
            item {
                WorkoutCard(
                    title = state.scheduleTitle,
                    subtitle = state.scheduleSubtitle,
                    workoutType = state.todayWorkoutType,
                    isWorkoutDay = state.isWorkoutDay,
                    onStart = {
                        scope.launch {
                            val sessionId = viewModel.startOrResumeWorkoutSession()
                            if (sessionId > 0) {
                                onStartWorkout(sessionId)
                            }
                        }
                    }
                )
            }

            // Card de Hidratação com Progresso
            item {
                WaterTrackerCard(
                    totalWaterMl = state.totalWaterMl,
                    targetWaterMl = state.activeNutritionTarget?.waterTargetMl ?: 2600,
                    statusMessage = state.hydrationMessage,
                    onAddWater = { viewModel.addWater(it) },
                    onUndoWater = { viewModel.deleteLastWaterEntry() }
                )
            }

            // Card de Metas Nutricionais
            item {
                NutritionSummaryCard(
                    target = state.activeNutritionTarget
                )
            }

            // Card de Lembrete de Suplementação
            item {
                SupplementTrackerCard(
                    onToggleSupplement = { id, amount -> viewModel.toggleSupplement(id, amount) }
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(
    title: String,
    subtitle: String,
    workoutType: String,
    isWorkoutDay: Boolean,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWorkoutDay) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isWorkoutDay) Icons.Default.FitnessCenter else Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isWorkoutDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = if (isWorkoutDay) "17:30 – 18:15" else "DESCANSO",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isWorkoutDay) "INICIAR / RETOMAR TREINO $workoutType" else "VER SESSÃO DE TREINO")
            }
        }
    }
}

@Composable
fun WaterTrackerCard(
    totalWaterMl: Int,
    targetWaterMl: Int,
    statusMessage: String,
    onAddWater: (Int) -> Unit,
    onUndoWater: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hidratação Diária",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$totalWaterMl / $targetWaterMl ml",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (totalWaterMl.toFloat() / targetWaterMl.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = Color(0xFF2196F3),
                trackColor = Color(0xFFBBDEFB)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddWater(250) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+250 ml")
                }
                OutlinedButton(
                    onClick = { onAddWater(500) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+500 ml")
                }
                IconButton(onClick = onUndoWater) {
                    Icon(imageVector = Icons.Default.Undo, contentDescription = "Desfazer")
                }
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(target: NutritionTargetEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Metas de Nutrição Diária",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NutrientBox(title = "Calorias", value = "${target?.calorieTarget ?: 2550} kcal")
                NutrientBox(title = "Proteínas", value = "${target?.proteinTargetGrams ?: 150}g")
                NutrientBox(title = "Carbos", value = "${target?.carbTargetGrams ?: 330}g")
                NutrientBox(title = "Gorduras", value = "${target?.fatTargetGrams ?: 70}g")
            }
        }
    }
}

@Composable
fun NutrientBox(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SupplementTrackerCard(onToggleSupplement: (String, String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Lembrete de Suplementação",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Creatina (06:45)", fontWeight = FontWeight.SemiBold)
                    Text(text = "3–5g com suco de laranja", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { onToggleSupplement("CREATINE", "5g") }) {
                    Text("Tomado ✓")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Whey Protein (15:30)", fontWeight = FontWeight.SemiBold)
                    Text(text = "1 dose pré-treino no trabalho", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { onToggleSupplement("WHEY", "1 dose") }) {
                    Text("Tomado ✓")
                }
            }
        }
    }
}
