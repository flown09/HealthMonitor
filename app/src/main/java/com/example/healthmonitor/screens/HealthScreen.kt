package com.example.healthmonitor.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmonitor.viewmodels.HealthViewModel

@Composable
fun HealthScreen(viewModel: HealthViewModel, modifier: Modifier = Modifier) {
    val healthDataList by viewModel.healthDataList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок и кнопка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Здоровье",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Добавить")
            }
        }

        // Последние показатели
        val lastData = healthDataList.firstOrNull()
        if (lastData != null) {
            // Шаги
            HealthMetricCard(
                title = "Шаги",
                value = "${lastData.steps}",
                unit = "шагов",
                icon = "🚶"
            )

            // Вес
            HealthMetricCard(
                title = "Вес",
                value = String.format("%.1f", lastData.weight),
                unit = "кг",
                icon = "⚖️"
            )

            // Пульс
            HealthMetricCard(
                title = "Пульс",
                value = "${lastData.heartRate}",
                unit = "уд/мин",
                icon = "❤️"
            )

            // Давление
            HealthMetricCard(
                title = "Артериальное давление",
                value = "${lastData.bloodPressureSystolic}/${lastData.bloodPressureDiastolic}",
                unit = "мм рт.ст.",
                icon = "📊"
            )

            // Сон
            HealthMetricCard(
                title = "Сон",
                value = String.format("%.1f", lastData.sleepHours),
                unit = "часов",
                icon = "😴"
            )

            // Вода
            HealthMetricCard(
                title = "Вода",
                value = String.format("%.1f", lastData.waterIntakeL),
                unit = "литров",
                icon = "💧"
            )

            // График веса
            Spacer(modifier = Modifier.height(8.dp))
            WeightTrendCard(healthDataList)

            // График активности
            ActivityTrendCard(healthDataList)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Нет данных о здоровье",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Диалог добавления
    if (showAddDialog) {
        AddHealthDataDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { weight, heartRate, sys, dia, steps, sleep, water ->
                viewModel.addHealthData(weight, heartRate, sys, dia, steps, sleep, water)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun WeightTrendCard(healthDataList: List<com.example.healthmonitor.models.HealthData>) {
    if (healthDataList.size >= 2) {
        val sortedData = healthDataList.sortedByDescending { it.date }
        val currentWeight = sortedData.first().weight
        val previousWeight = sortedData.getOrNull(1)?.weight ?: currentWeight
        val diff = currentWeight - previousWeight
        val isWeightIncreasing = diff > 0

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Тренд веса",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Изменение",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%+.1f кг", diff),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeightIncreasing)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = if (isWeightIncreasing) "📈" else "📉",
                        fontSize = 32.sp
                    )
                }

                Text(
                    text = "Последние записи показывают ${if (isWeightIncreasing) "увеличение" else "снижение"} веса",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ActivityTrendCard(healthDataList: List<com.example.healthmonitor.models.HealthData>) {
    if (healthDataList.size >= 2) {
        val sortedData = healthDataList.sortedByDescending { it.date }
        val currentSteps = sortedData.first().steps
        val averageSteps = sortedData.take(7).map { it.steps }.average().toInt()

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Активность",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Средний дневной шаг",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$averageSteps шагов",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "🏃",
                        fontSize = 32.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { (averageSteps.toFloat() / 10000).coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Text(
                    text = "Цель: 10000 шагов",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddHealthDataDialog(
    onDismiss: () -> Unit,
    onAdd: (Float, Int, Int, Int, Int, Float, Float) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf("") }
    var sleep by remember { mutableStateOf("") }
    var water by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить показатели здоровья") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Вес (кг)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = heartRate,
                    onValueChange = { heartRate = it },
                    label = { Text("Пульс (уд/мин)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = systolic,
                        onValueChange = { systolic = it },
                        label = { Text("Систолическое") },
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = diastolic,
                        onValueChange = { diastolic = it },
                        label = { Text("Диастолическое") },
                        modifier = Modifier.weight(1f)
                    )
                }

                TextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Шаги") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = sleep,
                    onValueChange = { sleep = it },
                    label = { Text("Сон (часов)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = water,
                    onValueChange = { water = it },
                    label = { Text("Вода (литров)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        weight.toFloatOrNull() ?: 70f,
                        heartRate.toIntOrNull() ?: 70,
                        systolic.toIntOrNull() ?: 120,
                        diastolic.toIntOrNull() ?: 80,
                        steps.toIntOrNull() ?: 0,
                        sleep.toFloatOrNull() ?: 8f,
                        water.toFloatOrNull() ?: 2f
                    )
                }
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
