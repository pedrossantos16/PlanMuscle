package com.pedro.planmuscle.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(viewModel: CheckInViewModel) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Anotações Diárias & Medidas",
                        fontWeight = FontWeight.Bold
                    )
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
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Check-in Diário") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Medidas Semanais") }
                )
            }

            if (selectedTab == 0) {
                DailyCheckInForm(
                    initialCheckIn = state.checkIn,
                    onSave = { weight, readiness, sleepHours, sleepQuality, notes ->
                        viewModel.saveCheckIn(weight, readiness, sleepHours, sleepQuality, notes)
                    }
                )
            } else {
                WeeklyMeasurementForm(
                    initialMeasurement = state.measurement,
                    onSave = { waist, arm, thigh, chest ->
                        viewModel.saveMeasurement(waist, arm, thigh, chest)
                    }
                )
            }
        }
    }
}

@Composable
fun DailyCheckInForm(
    initialCheckIn: com.pedro.planmuscle.data.model.DailyCheckInEntity?,
    onSave: (weight: Float?, readiness: Int?, sleepHours: Float?, sleepQuality: Int?, notes: String) -> Unit
) {
    var weightText by remember(initialCheckIn) { mutableStateOf(initialCheckIn?.weightKg?.toString() ?: "") }
    var sleepHoursText by remember(initialCheckIn) { mutableStateOf(initialCheckIn?.sleepHours?.toString() ?: "") }
    var readiness by remember(initialCheckIn) { mutableIntStateOf(initialCheckIn?.readinessLevel ?: 4) }
    var sleepQuality by remember(initialCheckIn) { mutableIntStateOf(initialCheckIn?.sleepQuality ?: 4) }
    var notesText by remember(initialCheckIn) { mutableStateOf(initialCheckIn?.notes ?: "") }
    var savedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Peso em Jejum & Sono",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso corporal em jejum (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sleepHoursText,
                        onValueChange = { sleepHoursText = it },
                        label = { Text("Horas de sono (ex: 7.5)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Disposição & Recuperação",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Nível de Disposição (Readiness: 1 a 5):")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..5).forEach { level ->
                            FilterChip(
                                selected = readiness == level,
                                onClick = { readiness = level },
                                label = { Text("$level") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Qualidade do Sono (1 a 5):")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..5).forEach { q ->
                            FilterChip(
                                selected = sleepQuality == q,
                                onClick = { sleepQuality = q },
                                label = { Text("$q") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Observações do Dia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Anotações gerais (ex: treino rendeu muito, cansaço no SENAI)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSave(
                        weightText.toFloatOrNull(),
                        readiness,
                        sleepHoursText.toFloatOrNull(),
                        sleepQuality,
                        notesText
                    )
                    savedMessage = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SALVAR CHECK-IN DIÁRIO")
            }

            if (savedMessage) {
                Text(
                    text = "Check-in salvo com sucesso! ✓",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyMeasurementForm(
    initialMeasurement: com.pedro.planmuscle.data.model.BodyMeasurementEntity?,
    onSave: (waist: Float?, arm: Float?, thigh: Float?, chest: Float?) -> Unit
) {
    var waistText by remember(initialMeasurement) { mutableStateOf(initialMeasurement?.waistCm?.toString() ?: "") }
    var armText by remember(initialMeasurement) { mutableStateOf(initialMeasurement?.armCm?.toString() ?: "") }
    var thighText by remember(initialMeasurement) { mutableStateOf(initialMeasurement?.thighCm?.toString() ?: "") }
    var chestText by remember(initialMeasurement) { mutableStateOf(initialMeasurement?.chestCm?.toString() ?: "") }
    var savedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Medidas Corporais (Registro Semanal)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recomendado medir uma vez por semana em jejum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = waistText,
                        onValueChange = { waistText = it },
                        label = { Text("Circunferência Abdominal / Cintura (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = armText,
                        onValueChange = { armText = it },
                        label = { Text("Braço contraído (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = thighText,
                        onValueChange = { thighText = it },
                        label = { Text("Coxa (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = chestText,
                        onValueChange = { chestText = it },
                        label = { Text("Peitoral (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSave(
                        waistText.toFloatOrNull(),
                        armText.toFloatOrNull(),
                        thighText.toFloatOrNull(),
                        chestText.toFloatOrNull()
                    )
                    savedMessage = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SALVAR MEDIDAS SEMANAIS")
            }

            if (savedMessage) {
                Text(
                    text = "Medidas salvas com sucesso! ✓",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
