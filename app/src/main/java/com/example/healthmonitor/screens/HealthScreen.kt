package com.example.healthmonitor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val stepGoal = currentUser?.dailyStepGoal ?: 10000

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Здоровье",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

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

        // Карточка отслеживания веса (передаем currentUser)
        WeightTrackingCard(healthDataList, viewModel, currentUser)

        // BMI Карточка
        currentUser?.let { user ->
            BMICard(viewModel, user)
            WaterCard(user)
        }
    }
}


@Composable
fun WeightTrackingCard(healthDataList: List<HealthData>, viewModel: HealthViewModel, currentUser: com.example.healthmonitor.models.User?) {
    val sortedData = healthDataList.sortedBy { it.date }
    val lastWeights = sortedData.takeLast(7) // Последние 7 записей

    // Используем целевой вес из профиля как текущий вес
    val currentWeight = currentUser?.targetWeight ?: (sortedData.lastOrNull()?.weight ?: 0f)
    val previousWeight = sortedData.getOrNull(sortedData.size - 2)?.weight ?: currentWeight
    val weightChange = currentWeight - previousWeight

    var showWeightDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Отслеживание веса",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showWeightDialog = true },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("+ Добавить", fontSize = 12.sp)
                }
            }

            if (lastWeights.isNotEmpty() || currentWeight > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Текущий вес", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = String.format("%.1f кг", currentWeight),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.End
                    ) {
                        Text("Изменение", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val changeColor = if (weightChange <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        val changeSymbol = if (weightChange <= 0) "↓" else "↑"
                        Text(
                            text = "$changeSymbol ${String.format("%.1f кг", kotlin.math.abs(weightChange))}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                    }
                }

                // Мини-график (столбцы)
                if (lastWeights.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.Bottom
                    ) {
                        val minWeight = lastWeights.minOf { it.weight } - 2
                        val maxWeight = lastWeights.maxOf { it.weight } + 2
                        val range = maxWeight - minWeight

                        lastWeights.forEach { data ->
                            val normalizedHeight = ((data.weight - minWeight) / range * 80).coerceIn(5f, 80f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(normalizedHeight.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    Text(
                        text = "Последние ${lastWeights.size} записей",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Нет данных о весе",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showWeightDialog) {
        AddWeightDialog(
            onDismiss = { showWeightDialog = false },
            onAdd = { weight ->
                viewModel.addHealthData(
                    weight = weight,
                    heartRate = 0,
                    sys = 0,
                    dia = 0,
                    steps = 0,
                    sleep = 0f,
                    water = 0f
                )
                showWeightDialog = false
            }
        )
    }
}


@Composable
fun AddWeightDialog(
    onDismiss: () -> Unit,
    onAdd: (Float) -> Unit
) {
    var weightVal by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить вес") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Введите свой вес в килограммах",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextField(
                    value = weightVal,
                    onValueChange = { weightVal = it },
                    label = { Text("Вес (кг)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(weightVal.toFloatOrNull() ?: 70f)
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



@Composable
fun BMICard(viewModel: HealthViewModel, user: com.example.healthmonitor.models.User) {
    val bmi = viewModel.calculateBMI()
    val bmiCategory = when {
        bmi < 18.5 -> "Недостаток веса"
        bmi < 25 -> "Нормальный вес"
        bmi < 30 -> "Избыточный вес"
        else -> "Ожирение"
    }

    val bmiColor = when {
        bmi < 18.5 -> MaterialTheme.colorScheme.secondary
        bmi < 25 -> MaterialTheme.colorScheme.primary
        bmi < 30 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Индекс массы тела (BMI)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("ИМТ", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("%.1f", bmi),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = bmiColor
                    )
                }

                Column {
                    Text("Категория", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = bmiCategory,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun WaterCard(user: com.example.healthmonitor.models.User) {
    // Используем точку как разделитель, независимо от локали
    val waterNormFloat = (user.targetWeight * 35) / 1000
    val waterNorm = String.format(java.util.Locale.US, "%.1f", waterNormFloat)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Рекомендуемое потребление воды",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$waterNorm литров в день",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Это примерно ${(waterNormFloat * 1000).toInt() / 250} стаканов по 250мл",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
