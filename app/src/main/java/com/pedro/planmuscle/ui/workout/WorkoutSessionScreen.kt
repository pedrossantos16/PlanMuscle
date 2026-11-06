package com.pedro.planmuscle.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.workoutDefinition.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GrowFit • 45 minutos (17:30 – 18:15)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            // Banner do Timer de Descanso
            RestTimerBanner(
                secondsRemaining = state.timerSecondsRemaining,
                isRunning = state.isTimerRunning,
                onPause = { viewModel.pauseRestTimer() },
                onAdd30s = { viewModel.startRestTimer(state.timerSecondsRemaining + 30) }
            )

            // Banner da Fase Atual
            PhaseInfoBanner(phaseDescription = state.currentPhase.description)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.exerciseStates) { exerciseState ->
                    ExerciseCard(
                        exerciseState = exerciseState,
                        onSaveSet = { setType, weight, reps, rir ->
                            viewModel.saveSetLog(
                                exerciseId = exerciseState.definition.id,
                                exerciseName = exerciseState.definition.name,
                                setType = setType,
                                weightKg = weight,
                                reps = reps,
                                rir = rir
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
                        Text("FINALIZAR TREINO NA GROWFIT")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
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
fun PhaseInfoBanner(phaseDescription: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = phaseDescription,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ExerciseCard(
    exerciseState: ExerciseState,
    onSaveSet: (setType: String, weight: Float, reps: Int, rir: Int?) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var rirInput by remember { mutableStateOf("2") }
    var isWarmupSet by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exerciseState.definition.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Meta: ${exerciseState.definition.defaultWorkSets} series × ${exerciseState.definition.targetRepsMin}–${exerciseState.definition.targetRepsMax} reps (Descanso: ${exerciseState.definition.restTimeSeconds}s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Aquecimento: ${exerciseState.definition.warmupGuidance}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            if (exerciseState.suggestedProgression.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exerciseState.suggestedProgression,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Registros gravados
            if (exerciseState.logs.isNotEmpty()) {
                Text(text = "Séries Registradas:", style = MaterialTheme.typography.labelMedium)
                exerciseState.logs.forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val typeLabel = if (log.setType == "WARMUP") "[AQUECIMENTO]" else "Série ${log.setNumber}"
                        Text(
                            text = "$typeLabel: ${log.weightKg} kg × ${log.reps} reps (RIR ${log.rir ?: "-"})",
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

            // Form de adição de série
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Peso (kg)") },
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
                    Text("Série de Aquecimento", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = {
                        val w = weightInput.toFloatOrNull() ?: 0f
                        val r = repsInput.toIntOrNull() ?: 0
                        val rir = rirInput.toIntOrNull()
                        if (w > 0 && r > 0) {
                            onSaveSet(
                                if (isWarmupSet) "WARMUP" else "WORK",
                                w,
                                r,
                                rir
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
