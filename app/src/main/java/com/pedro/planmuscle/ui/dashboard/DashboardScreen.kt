package com.pedro.planmuscle.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartWorkout: (workoutType: String, weekNumber: Int, sessionId: Long?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

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
                            text = "Semana ${state.weekNumber} de 8 • Data: ${state.todayDate}",
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
                    weekNumber = state.weekNumber,
                    workoutType = state.todayWorkoutType,
                    existingSessionId = state.workoutSession?.id,
                    isCompleted = state.workoutSession?.status == "COMPLETED",
                    onStart = {
                        val session = state.workoutSession
                        if (session != null) {
                            onStartWorkout(session.workoutType, session.weekNumber, session.id)
                        } else {
                            viewModel.startWorkoutSession(state.todayWorkoutType, state.weekNumber)
                            onStartWorkout(state.todayWorkoutType, state.weekNumber, null)
                        }
                    }
                )
            }

            // Card de Hidratação com Progresso
            item {
                WaterTrackerCard(
                    totalWaterMl = state.totalWaterMl,
                    targetWaterMl = state.activeProtocol?.waterTargetMl ?: 2600,
                    statusMessage = state.hydrationMessage,
                    onAddWater = { viewModel.addWater(it) }
                )
            }

            // Card de Metas Nutricionais
            item {
                NutritionSummaryCard(
                    protocol = state.activeProtocol
                )
            }

            // Card de Lembrete de Suplementação
            item {
                SupplementTrackerCard(
                    onLogSupplement = { id, amount -> viewModel.logSupplement(id, amount) }
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(
    weekNumber: Int,
    workoutType: String,
    existingSessionId: Long?,
    isCompleted: Boolean,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Treino $workoutType Agendado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = if (isCompleted) "CONCLUÍDO ✓" else "17:30 – 18:15",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Local: GrowFit Unidade 2 (Duração: 45 min)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (workoutType == "A")
                    "Foco: Supino reto, Agachamento, Puxada alta, Mesa flexora, Elevação lateral, Rosca direta"
                else
                    "Foco: Leg press, Remada baixa, Supino inclinado, Stiff, Desenvolvimento, Rosca martelo + Tríceps",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isCompleted) "VER SESSÃO GRAVADA" else "INICIAR TREINO $workoutType AGORA")
            }
        }
    }
}

@Composable
fun WaterTrackerCard(
    totalWaterMl: Int,
    targetWaterMl: Int,
    statusMessage: String,
    onAddWater: (Int) -> Unit
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
                    Text("+ 250 ml")
                }
                OutlinedButton(
                    onClick = { onAddWater(500) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ 500 ml")
                }
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(protocol: com.pedro.planmuscle.data.model.ProtocolEntity?) {
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
                NutrientBox(title = "Calorias", value = "${protocol?.calorieTarget ?: 2550} kcal")
                NutrientBox(title = "Proteínas", value = "${protocol?.proteinTargetGrams ?: 150}g")
                NutrientBox(title = "Carbos", value = "${protocol?.carbTargetGrams ?: 330}g")
                NutrientBox(title = "Gorduras", value = "${protocol?.fatTargetGrams ?: 70}g")
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
fun SupplementTrackerCard(onLogSupplement: (String, String) -> Unit) {
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
                TextButton(onClick = { onLogSupplement("CREATINE", "5g") }) {
                    Text("Tomado ✓")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Whey Protein (15:30)", fontWeight = FontWeight.SemiBold)
                    Text(text = "1 dose pré-treino no trabalho", style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { onLogSupplement("WHEY", "1 dose") }) {
                    Text("Tomado ✓")
                }
            }
        }
    }
}
