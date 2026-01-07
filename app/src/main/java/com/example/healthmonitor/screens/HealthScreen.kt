package com.example.healthmonitor.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmonitor.models.HealthData
import com.example.healthmonitor.utils.StepCounter
import com.example.healthmonitor.viewmodels.HealthViewModel
import kotlinx.coroutines.delay

@Composable
fun HealthScreen(viewModel: HealthViewModel, stepCounter: StepCounter, modifier: Modifier = Modifier) {
    val healthDataList by viewModel.healthDataList.collectAsState()
    val currentSteps by stepCounter.stepCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val refreshTrigger by viewModel._refreshTrigger.collectAsState()

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
            WaterCard(viewModel, user)
        }
    }
}


@Composable
fun WeightTrackingCard(healthDataList: List<HealthData>, viewModel: HealthViewModel, currentUser: com.example.healthmonitor.models.User?) {
    // Используем collectAsState для реактивного обновления
    val healthDataList by viewModel.healthDataList.collectAsState()

    val sortedData = healthDataList.sortedBy { it.date }
    val lastWeights = sortedData.takeLast(7)

    val currentWeight = currentUser?.targetWeight ?: (sortedData.lastOrNull()?.weight ?: 0f)
    val previousWeight = sortedData.getOrNull(sortedData.size - 2)?.weight ?: currentWeight
    val weightChange = currentWeight - previousWeight

    var showWeightDialog by remember { mutableStateOf(false) }
    var selectedWeightIndex by remember { mutableStateOf(-1) }
    var showWeightDetailsDialog by remember { mutableStateOf(false) }
    var selectedWeight by remember { mutableStateOf(0f) }
    var selectedWeightData by remember { mutableStateOf<HealthData?>(null) }

    // Очищаем выделение когда изменился список
    LaunchedEffect(lastWeights.size) {
        selectedWeightIndex = -1
        selectedWeightData = null
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

                // Мини-график (столбцы) с интерактивностью
                if (lastWeights.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.Bottom
                        ) {
                            val minWeight = lastWeights.minOf { it.weight } - 2
                            val maxWeight = lastWeights.maxOf { it.weight } + 2
                            val range = maxWeight - minWeight

                            lastWeights.forEachIndexed { index, data ->
                                val normalizedHeight = ((data.weight - minWeight) / range * 80).coerceIn(5f, 80f)
                                val isSelected = selectedWeightIndex == index

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(normalizedHeight.dp)
                                        .background(
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.secondary
                                            else
                                                MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    val pressStart = System.currentTimeMillis()
                                                    tryAwaitRelease()
                                                    val pressDuration = System.currentTimeMillis() - pressStart

                                                    if (pressDuration < 500) {
                                                        // Короткое нажатие - показываем значение
                                                        selectedWeightIndex = index
                                                        selectedWeight = data.weight
                                                        selectedWeightData = data
                                                    } else {
                                                        // Долгое нажатие - открываем диалог редактирования
                                                        selectedWeightIndex = index
                                                        selectedWeight = data.weight
                                                        selectedWeightData = data
                                                        showWeightDetailsDialog = true
                                                    }
                                                }
                                            )
                                        }
                                        .animateContentSize()
                                )
                            }
                        }

                        // Показываем значение выбранного столбца
                        if (selectedWeightIndex >= 0 && selectedWeightIndex < lastWeights.size) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                // Используем data из lastWeights напрямую, а не selectedWeight
                                val displayWeight = lastWeights.getOrNull(selectedWeightIndex)
                                Text(
                                    text = "${String.format("%.1f кг", displayWeight?.weight ?: selectedWeight)} • ${formatDate(displayWeight?.date ?: 0)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Text(
                        text = "Последние ${lastWeights.size} записей • Нажмите на столбец для просмотра, долгое нажатие для редактирования",
                        fontSize = 11.sp,
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

    // Диалог редактирования веса при долгом нажатии
    if (showWeightDetailsDialog && selectedWeightData != null) {
        var newWeight by remember { mutableStateOf(selectedWeight.toString()) }

        AlertDialog(
            onDismissRequest = { showWeightDetailsDialog = false },
            title = {
                Text("Редактировать вес")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Дата: ${formatDate(selectedWeightData!!.date)}",
                        fontSize = 14.sp
                    )
                    TextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("Вес (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val weight = newWeight.toFloatOrNull() ?: selectedWeight
                        selectedWeightData?.let { data ->
                            viewModel.updateHealthData(data.copy(weight = weight))

                            // Обновляем профиль используя существующую функцию
                            val currentUser = viewModel.currentUser.value
                            if (currentUser != null) {
                                viewModel.updateUser(
                                    name = currentUser.name,
                                    gender = currentUser.gender,
                                    age = currentUser.age,
                                    heightCm = currentUser.heightCm,
                                    targetWeight = weight,
                                    activityLevel = currentUser.activityLevel,
                                    weightGoal = currentUser.weightGoal,
                                    dailyStepGoal = currentUser.dailyStepGoal
                                )
                            }
                        }
                        showWeightDetailsDialog = false
                        selectedWeightIndex = -1
                        selectedWeightData = null
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showWeightDetailsDialog = false
                            selectedWeightIndex = -1
                            selectedWeightData = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }
                    Button(
                        onClick = {
                            selectedWeightData?.let { data ->
                                viewModel.deleteHealthData(data)
                            }
                            showWeightDetailsDialog = false
                            selectedWeightIndex = -1
                            selectedWeightData = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Удалить")
                    }
                }
            }
        )
    }


}

// Функция для форматирования даты
fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
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
    // Наблюдаем за изменениями currentUser
    val currentUser by viewModel.currentUser.collectAsState()

    // Пересчитываем BMI когда изменяется currentUser
    val bmi = remember(currentUser) {
        currentUser?.let { u ->
            if (u.heightCm > 0 && u.targetWeight > 0) {
                u.targetWeight / ((u.heightCm / 100f) * (u.heightCm / 100f))
            } else {
                0f
            }
        } ?: 0f
    }

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
fun WaterCard(viewModel: HealthViewModel, user: com.example.healthmonitor.models.User) {
    val currentUser by viewModel.currentUser.collectAsState()

    // Пересчитываем рекомендуемое потребление воды с учетом активности
    val recommendedWater = remember(currentUser) {
        currentUser?.let { u ->
            val baseIntake = u.targetWeight * 30 // 30 мл на 1 кг
            val activityMultiplier = when (u.activityLevel) {
                "sedentary" -> 1.2f
                "light" -> 1.375f
                "moderate" -> 1.55f
                "active" -> 1.725f
                "very_active" -> 1.9f
                else -> 1.2f
            }
            baseIntake * activityMultiplier / 1000 // Конвертируем в литры
        } ?: 0f
    }

    val glassesCount = (recommendedWater * 4).toInt() // 1 стакан ≈ 250мл (0.25л)

    val activityName = when (currentUser?.activityLevel) {
        "sedentary" -> "Малоподвижный образ жизни"
        "light" -> "Легкая активность (1-3 дня в неделю)"
        "moderate" -> "Умеренная активность (3-5 дней)"
        "active" -> "Высокая активность (6-7 дней)"
        "very_active" -> "Очень высокая (ежедневные интенсивные тренировки)"
        else -> "Неизвестно"
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
                text = "Рекомендуемое потребление воды",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column {
                    Text("Рекомендация", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format("%.1f л", recommendedWater),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text("💧", fontSize = 32.sp)
            }

            Text(
                text = "≈ $glassesCount стаканов по 250мл в день",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}