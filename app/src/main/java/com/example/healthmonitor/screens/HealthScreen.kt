package com.example.healthmonitor.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmonitor.models.HealthData
import com.example.healthmonitor.utils.StepCounter
import com.example.healthmonitor.viewmodels.HealthViewModel

@Composable
fun HealthScreen(viewModel: HealthViewModel, stepCounter: StepCounter, modifier: Modifier = Modifier) {
    val healthDataList by viewModel.healthDataList.collectAsState()
    val currentSteps by stepCounter.stepCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val stepGoal = currentUser?.dailyStepGoal ?: 10000

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        // Карточка с шагами
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Шаги сегодня",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currentSteps",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🚶",
                        fontSize = 32.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentSteps.toFloat() / stepGoal).coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Text(
                    text = "${(currentSteps.toFloat() / stepGoal * 100).toInt()}% от $stepGoal",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Последние показатели здоровья
        if (healthDataList.isNotEmpty()) {
            Text(
                text = "История",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(healthDataList) { health ->
                    HealthDataItemCard(health)
                }
            }
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
fun HealthDataItemCard(health: HealthData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Вес: ${health.weight} кг",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Пульс: ${health.heartRate} уд/мин",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "⚕️",
                    fontSize = 28.sp
                )
            }

            Text(
                text = "Давление: ${health.bloodPressureSystolic}/${health.bloodPressureDiastolic} | Сон: ${health.sleepHours}ч | Вода: ${health.waterIntakeL}л",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddHealthDataDialog(
    onDismiss: () -> Unit,
    onAdd: (Float, Int, Int, Int, Int, Float, Float) -> Unit
) {
    var weightVal by remember { mutableStateOf("") }
    var heartRateVal by remember { mutableStateOf("") }
    var sysBPVal by remember { mutableStateOf("") }
    var diaBPVal by remember { mutableStateOf("") }
    var stepsVal by remember { mutableStateOf("") }
    var sleepVal by remember { mutableStateOf("") }
    var waterVal by remember { mutableStateOf("") }

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
                    value = weightVal,
                    onValueChange = { weightVal = it },
                    label = { Text("Вес (кг)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = heartRateVal,
                    onValueChange = { heartRateVal = it },
                    label = { Text("Пульс (уд/мин)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = sysBPVal,
                    onValueChange = { sysBPVal = it },
                    label = { Text("Систолическое АД") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = diaBPVal,
                    onValueChange = { diaBPVal = it },
                    label = { Text("Диастолическое АД") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = stepsVal,
                    onValueChange = { stepsVal = it },
                    label = { Text("Шаги") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = sleepVal,
                    onValueChange = { sleepVal = it },
                    label = { Text("Сон (часов)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = waterVal,
                    onValueChange = { waterVal = it },
                    label = { Text("Вода (литров)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        weightVal.toFloatOrNull() ?: 70f,
                        heartRateVal.toIntOrNull() ?: 70,
                        sysBPVal.toIntOrNull() ?: 120,
                        diaBPVal.toIntOrNull() ?: 80,
                        stepsVal.toIntOrNull() ?: 0,
                        sleepVal.toFloatOrNull() ?: 8f,
                        waterVal.toFloatOrNull() ?: 2f
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
