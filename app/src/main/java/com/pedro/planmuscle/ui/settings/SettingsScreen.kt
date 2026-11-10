package com.pedro.planmuscle.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pedro.planmuscle.domain.ExerciseDefinition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onThemeChanged: (com.pedro.planmuscle.ui.theme.AppThemeOption) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var calInput by remember(state.activeProtocol) { mutableStateOf(state.activeProtocol?.calorieTarget?.toString() ?: "2550") }
    var proteinInput by remember(state.activeProtocol) { mutableStateOf(state.activeProtocol?.proteinTargetGrams?.toString() ?: "150") }
    var carbInput by remember(state.activeProtocol) { mutableStateOf(state.activeProtocol?.carbTargetGrams?.toString() ?: "330") }
    var fatInput by remember(state.activeProtocol) { mutableStateOf(state.activeProtocol?.fatTargetGrams?.toString() ?: "70") }
    var waterInput by remember(state.activeProtocol) { mutableStateOf(state.activeProtocol?.waterTargetMl?.toString() ?: "2600") }
    var showSavedToast by remember { mutableStateOf(false) }

    // Controla se a seção de temas está expandida ou contraída
    var themesExpanded by remember { mutableStateOf(false) }
    var workoutEditExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Configurações & Guia", fontWeight = FontWeight.Bold)
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
            // Seção 1: Seleção de Tema / Paleta de Cores (EXPANDÍVEL / SANFONA)
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { themesExpanded = !themesExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "Paletas de Cores & Temas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Atual: ${state.selectedTheme.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (themesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Contrair/Expandir"
                            )
                        }

                        AnimatedVisibility(visible = themesExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                                Text(text = "🌙 Temas Escuros (Dark)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                com.pedro.planmuscle.ui.theme.AppThemeOption.entries.filter { it.isDark }.forEach { option ->
                                    ThemeOptionRow(
                                        option = option,
                                        isSelected = state.selectedTheme == option,
                                        onSelect = {
                                            viewModel.selectTheme(option)
                                            onThemeChanged(option)
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "☀️ Temas Claros (Light)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                com.pedro.planmuscle.ui.theme.AppThemeOption.entries.filter { !it.isDark }.forEach { option ->
                                    ThemeOptionRow(
                                        option = option,
                                        isSelected = state.selectedTheme == option,
                                        onSelect = {
                                            viewModel.selectTheme(option)
                                            onThemeChanged(option)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Seção 2: Edição do Plano Alimentar e Água
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Alterar Metas de Nutrição & Água", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = calInput,
                                onValueChange = { calInput = it },
                                label = { Text("Calorias (kcal)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = proteinInput,
                                onValueChange = { proteinInput = it },
                                label = { Text("Proteína (g)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = carbInput,
                                onValueChange = { carbInput = it },
                                label = { Text("Carbos (g)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = fatInput,
                                onValueChange = { fatInput = it },
                                label = { Text("Gorduras (g)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = waterInput,
                            onValueChange = { waterInput = it },
                            label = { Text("Meta de Água (ml/dia)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val c = calInput.toIntOrNull() ?: 2550
                                val p = proteinInput.toIntOrNull() ?: 150
                                val cb = carbInput.toIntOrNull() ?: 330
                                val f = fatInput.toIntOrNull() ?: 70
                                val w = waterInput.toIntOrNull() ?: 2600
                                viewModel.updateProtocolGoals(c, p, cb, f, w)
                                showSavedToast = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SALVAR NOVAS METAS ALIMENTARES")
                        }

                        if (showSavedToast) {
                            Text(
                                text = "Metas salvas com sucesso! ✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Seção 3: EDIÇÃO COMPLETA DE TREINOS E EXERCÍCIOS (Treino A & B)
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { workoutEditExpanded = !workoutEditExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "💪 Personalizar Treinos A & B", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "Ajustar séries, reps e descansos de cada exercício", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(
                                imageVector = if (workoutEditExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        AnimatedVisibility(visible = workoutEditExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                                Text(text = "🏋️ Treino A (Ênfase Peito, Quadríceps e Costas)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))

                                state.workoutA.exercises.forEach { exercise ->
                                    ExerciseEditCard(
                                        exercise = exercise,
                                        onSave = { name, workSets, minReps, maxReps, restSecs, warmup ->
                                            viewModel.saveCustomExercise(
                                                exerciseId = exercise.id,
                                                name = name,
                                                workSets = workSets,
                                                minReps = minReps,
                                                maxReps = maxReps,
                                                restSeconds = restSecs,
                                                warmupGuidance = warmup
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(text = "🏋️ Treino B (Ênfase Posteriores, Ombros e Braços)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))

                                state.workoutB.exercises.forEach { exercise ->
                                    ExerciseEditCard(
                                        exercise = exercise,
                                        onSave = { name, workSets, minReps, maxReps, restSecs, warmup ->
                                            viewModel.saveCustomExercise(
                                                exerciseId = exercise.id,
                                                name = name,
                                                workSets = workSets,
                                                minReps = minReps,
                                                maxReps = maxReps,
                                                restSeconds = restSecs,
                                                warmupGuidance = warmup
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Seção 4: Guia Interativo do Aplicativo
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Guia do Aplicativo & Dicionário", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Aprenda como funciona cada tela, ícone e elemento do seu plano.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        GuideAccordionItem(
                            title = "🏠 Tela Início (Dashboard)",
                            icon = Icons.Default.Home,
                            content = "• Ícone de Gota (💧): Exibe seu consumo acumulado e o volume de água esperado para o horário atual.\n• Card de Treino (🏋️): Mostra se é Treino A ou B no dia e lembra do seu horário na GrowFit (17:30 às 18:15).\n• Lembrete de Suplementos (🧪): Atalho para marcar a Creatina (06:45 com suco) e Whey (15:30 no trabalho)."
                        )

                        GuideAccordionItem(
                            title = "⚡ Tela de Treino Ativo",
                            icon = Icons.Default.FitnessCenter,
                            content = "• Timer de Descanso (⏱️): Contagem regressiva automática (150s compostos / 90s isoladores).\n• RIR (Reps em Reserva): Quantas reps faltavam para falhar (ex: RIR 2 = dava para fazer mais 2).\n• Série de Aquecimento (WARMUP): Prepara tendões sem gerar fadiga. Não conta no volume de trabalho útil.\n• Sugestão da Carga: O app sugere aumentar +2kg quando você atinge o limite superior de reps."
                        )

                        GuideAccordionItem(
                            title = "📝 Tela Diário (Check-in)",
                            icon = Icons.Default.EditNote,
                            content = "• Peso em Jejum: Registrado diariamente para calcular a Média Móvel de 7 dias.\n• Readiness / Disposição (1 a 5): Avalia sua recuperação e capacidade de treino.\n• Medidas Corporais: Preenchidas 1x por semana para monitorar cintura x hipertrofia."
                        )

                        GuideAccordionItem(
                            title = "🍲 Tela do Plano & Refeições",
                            icon = Icons.Default.RestaurantMenu,
                            content = "• Badges Nutricionais: Resumo de Calorias, Proteínas, Carbos e Gorduras.\n• Linha do Tempo: Refeições distribuídas conforme sua rotina (06:45, 09:00, 12:15, 15:30, 20:10 no SENAI e 22:30 em casa)."
                        )

                        GuideAccordionItem(
                            title = "📈 Tela Evolução (Histórico)",
                            icon = Icons.Default.Analytics,
                            content = "• Média Móvel de 7 Dias: Suaviza flutuações normais de retenção de água.\n• Revisão Quinzenal: Análise automática de ganho (~100–250g/sem) com recomendação de ajuste calórico (+150 kcal)."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseEditCard(
    exercise: ExerciseDefinition,
    onSave: (name: String, workSets: Int, minReps: Int, maxReps: Int, restSecs: Int, warmup: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var nameText by remember(exercise) { mutableStateOf(exercise.name) }
    var setsText by remember(exercise) { mutableStateOf(exercise.defaultWorkSets.toString()) }
    var minRepsText by remember(exercise) { mutableStateOf(exercise.targetRepsMin.toString()) }
    var maxRepsText by remember(exercise) { mutableStateOf(exercise.targetRepsMax.toString()) }
    var restText by remember(exercise) { mutableStateOf(exercise.restTimeSeconds.toString()) }
    var warmupText by remember(exercise) { mutableStateOf(exercise.warmupGuidance) }
    var savedMsg by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = exercise.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(text = "${exercise.defaultWorkSets} séries × ${exercise.targetRepsMin}–${exercise.targetRepsMax} reps (${exercise.restTimeSeconds}s)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Nome do Exercício") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = setsText,
                            onValueChange = { setsText = it },
                            label = { Text("Séries") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = minRepsText,
                            onValueChange = { minRepsText = it },
                            label = { Text("Reps Min") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = maxRepsText,
                            onValueChange = { maxRepsText = it },
                            label = { Text("Reps Max") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = restText,
                        onValueChange = { restText = it },
                        label = { Text("Tempo de Descanso (segundos)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = warmupText,
                        onValueChange = { warmupText = it },
                        label = { Text("Guia de Aquecimento") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val sets = setsText.toIntOrNull() ?: exercise.defaultWorkSets
                            val minR = minRepsText.toIntOrNull() ?: exercise.targetRepsMin
                            val maxR = maxRepsText.toIntOrNull() ?: exercise.targetRepsMax
                            val rest = restText.toIntOrNull() ?: exercise.restTimeSeconds
                            onSave(nameText, sets, minR, maxR, rest, warmupText)
                            savedMsg = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salvar Alterações")
                    }

                    if (savedMsg) {
                        Text(text = "Exercício atualizado! ✓", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    option: com.pedro.planmuscle.ui.theme.AppThemeOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val sampleColor = when (option) {
        com.pedro.planmuscle.ui.theme.AppThemeOption.NEON_FITNESS -> Color(0xFF00E676)
        com.pedro.planmuscle.ui.theme.AppThemeOption.EMERALD_POWER -> Color(0xFF10B981)
        com.pedro.planmuscle.ui.theme.AppThemeOption.SUNSET_VIBRANT -> Color(0xFFFF6D00)
        com.pedro.planmuscle.ui.theme.AppThemeOption.ROYAL_PURPLE -> Color(0xFFD500F9)
        com.pedro.planmuscle.ui.theme.AppThemeOption.LIGHT_MINT -> Color(0xFF059669)
        com.pedro.planmuscle.ui.theme.AppThemeOption.LIGHT_CORAL -> Color(0xFFEA580C)
        com.pedro.planmuscle.ui.theme.AppThemeOption.LIGHT_OCEAN -> Color(0xFF0284C7)
        com.pedro.planmuscle.ui.theme.AppThemeOption.LIGHT_LAVENDER -> Color(0xFF7C3AED)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(sampleColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = option.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
        }
    }
}

@Composable
fun GuideAccordionItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                    Text(text = content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
