package com.pedro.planmuscle.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sessão de Treino ${state.session?.workoutType ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GrowFit • Janela 45 min (17:30 – 18:15)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Rest Timer
            RestTimerBanner(
                secondsRemaining = state.timerSecondsRemaining,
                isRunning = state.isTimerRunning,
                onPause = { viewModel.pauseRestTimer() },
                onAdd30s = { viewModel.startRestTimer(state.timerSecondsRemaining + 30) }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.exercisePrescriptions) { prescriptionState ->
                    PrescriptionCard(
                        prescriptionState = prescriptionState,
                        onSaveSet = { setType, weight, reps, rir, techniqueOk ->
                            viewModel.saveSetLog(
                                prescriptionId = prescriptionState.prescription.id,
                                exerciseId = prescriptionState.prescription.exerciseId,
                                setType = setType,
                                weightKg = weight,
                                reps = reps,
                                rir = rir,
                                techniqueOk = techniqueOk
                            )
                        },
                        onDeleteSet = { logId -> viewModel.deleteSetLog(logId) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.finishWorkout()
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FINALIZAR SESSÃO DE TREINO")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Continuar ou Sair?") },
                text = { Text("Você pode continuar o treino mais tarde ou abandonar a sessão.") },
                confirmButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    }) {
                        Text("CONTINUAR DEPOIS")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExitDialog = false
                        viewModel.abandonWorkout()
                        onNavigateBack()
                    }) {
                        Text("ABANDONAR TREINO", color = Color.Red)
                    }
                }
            )
        }
    }
}

@Composable
fun RestTimerBanner(
    secondsRemaining: Int,
    isRunning: Boolean,
    onPause: () -> Unit,
    onAdd30s: () -> Unit
) {
    if (secondsRemaining > 0 || isRunning) {
        val mins = secondsRemaining / 60
        val secs = secondsRemaining % 60
        val timeFormatted = String.format(java.util.Locale.US, "%02d:%02d", mins, secs)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Descanso: $timeFormatted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    TextButton(onClick = onAdd30s) { Text("+30s") }
                    IconButton(onClick = onPause) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrescriptionCard(
    prescriptionState: ExercisePrescriptionUiState,
    onSaveSet: (setType: String, weight: Float, reps: Int, rir: Int?, techniqueOk: Boolean) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    val prescription = prescriptionState.prescription
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var rirInput by remember { mutableStateOf("2") }
    var isWarmupSet by remember { mutableStateOf(false) }
    var techniqueOk by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prescription.exerciseNameSnapshot,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Prescrição: ${prescription.prescribedWorkSets} séries × ${prescription.minReps}–${prescription.maxReps} reps (Descanso: ${prescription.restSeconds}s, RIR ${prescription.targetRirMin}–${prescription.targetRirMax})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (prescription.warmupGuidanceSnapshot.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Aquecimento: ${prescription.warmupGuidanceSnapshot}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            prescriptionState.progressionDecision?.let { decision ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 Engine: ${decision.rationale}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Registros gravados
            if (prescriptionState.logs.isNotEmpty()) {
                Text(text = "Séries Registradas:", style = MaterialTheme.typography.labelMedium)
                prescriptionState.logs.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val typeLabel = if (log.setType == "WARMUP") "[AQUECIMENTO]" else "Série ${log.setIndex}"
                        val formattedWeight = String.format(java.util.Locale.US, "%.1f", log.weightKg)
                        Text(
                            text = "$typeLabel: ${formattedWeight} kg × ${log.reps} reps (RIR ${log.rir ?: "-"})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (log.setType == "WORK") FontWeight.Bold else FontWeight.Normal
                        )
                        IconButton(
                            onClick = { onDeleteSet(log.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Deletar",
                                tint = Color.Red
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Input form com suporte a decimais com vírgula ou ponto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Carga (kg)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = rirInput,
                    onValueChange = { rirInput = it },
                    label = { Text("RIR") },
                    modifier = Modifier.weight(0.8f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isWarmupSet,
                        onCheckedChange = { isWarmupSet = it }
                    )
                    Text("Aquecimento", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val sanitizedWeight = weightInput.replace(',', '.').toFloatOrNull() ?: 0f
                        val r = repsInput.toIntOrNull() ?: 0
                        val rir = rirInput.toIntOrNull()
                        if (sanitizedWeight > 0f && r > 0) {
                            onSaveSet(
                                if (isWarmupSet) "WARMUP" else "WORK",
                                sanitizedWeight,
                                r,
                                rir,
                                techniqueOk
                            )
                            repsInput = ""
                        }
                    }
                ) {
                    Text("Salvar Série")
                }
            }
        }
    }
}
