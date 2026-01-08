package com.example.healthmonitor.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.util.Calendar
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.healthmonitor.models.User


@Composable
fun HealthScreen(viewModel: HealthViewModel, stepCounter: StepCounter, modifier: Modifier = Modifier) {
    val healthDataList by viewModel.healthDataList.collectAsState()
    val currentSteps by stepCounter.stepCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val refreshTrigger by viewModel._refreshTrigger.collectAsState()

    var selectedDate by remember { mutableStateOf(getTodayTimestamp()) }

    // Получаем сегодняшнюю дату для проверки
    val todayTimestamp = getTodayTimestamp()

    // Фильтруем данные по выбранной дате
    val selectedDateData = remember(healthDataList, selectedDate) {
        healthDataList.filter { it.date == selectedDate }
    }

    val todayData = selectedDateData.firstOrNull()

    // Для отображения шагов берём либо выбранный день, либо текущий счётчик если это сегодня
    val displaySteps = if (selectedDate == todayTimestamp) currentSteps else (todayData?.steps ?: 0)

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

        // Карточка с шагами - теперь с переключением дат внутри
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Заголовок с переключателем дат
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Кнопка назад (левая стрелка)
                    IconButton(
                        onClick = {
                            val newCalendar = Calendar.getInstance()
                            newCalendar.timeInMillis = selectedDate
                            newCalendar.add(Calendar.DAY_OF_YEAR, -1)
                            newCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            newCalendar.set(Calendar.MINUTE, 0)
                            newCalendar.set(Calendar.SECOND, 0)
                            newCalendar.set(Calendar.MILLISECOND, 0)
                            selectedDate = newCalendar.timeInMillis
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("◄", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Шаги",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatDateShort(selectedDate),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Кнопка вперёд (правая стрелка) - ОТКЛЮЧЕНА если это будущая дата
                    IconButton(
                        onClick = {
                            val newCalendar = Calendar.getInstance()
                            newCalendar.timeInMillis = selectedDate
                            newCalendar.add(Calendar.DAY_OF_YEAR, 1)
                            newCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            newCalendar.set(Calendar.MINUTE, 0)
                            newCalendar.set(Calendar.SECOND, 0)
                            newCalendar.set(Calendar.MILLISECOND, 0)

                            // Проверяем что не переходим за текущий день
                            if (newCalendar.timeInMillis <= todayTimestamp) {
                                selectedDate = newCalendar.timeInMillis
                            }
                        },
                        enabled = selectedDate < todayTimestamp, // Кнопка работает только если не сегодня
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            "►",
                            fontSize = 20.sp,
                            color = if (selectedDate < todayTimestamp)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                Divider(modifier = Modifier.fillMaxWidth())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$displaySteps",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🚶",
                        fontSize = 32.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { (displaySteps.toFloat() / stepGoal).coerceAtMost(1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Text(
                    text = "${(displaySteps.toFloat() / stepGoal * 100).toInt()}% от $stepGoal",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Карточка отслеживания веса
        WeightTrackingCard(healthDataList, viewModel, currentUser)

        // BMI и Вода
        currentUser?.let { user ->
            BMICard(viewModel, user)
            WaterCard(viewModel, user)
        }
    }
}


fun formatDateShort(timestamp: Long): String {
    val today = getTodayTimestamp()

    return if (timestamp == today) {
        "Сегодня"
    } else {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        val todayCalendar = Calendar.getInstance()
        todayCalendar.timeInMillis = today

        val sdf = if (calendar.get(Calendar.YEAR) != todayCalendar.get(Calendar.YEAR)) {
            // Если год другой - показываем год
            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("ru"))
        } else {
            // Если год текущий - только день и месяц
            java.text.SimpleDateFormat("dd MMM", java.util.Locale("ru"))
        }

        sdf.format(java.util.Date(timestamp))
    }
}


@Composable
fun WeightTrackingCard(healthDataList: List<HealthData>, viewModel: HealthViewModel, currentUser: com.example.healthmonitor.models.User?) {
    val healthDataList by viewModel.healthDataList.collectAsState()
    var scrollPosition by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf(-1) }
    var showEditDialog by remember { mutableStateOf(false) }

    val sortedData = healthDataList.sortedBy { it.date }
    val lastWeights = sortedData.filter { it.weight > 0 }

    val maxItemsToShow = 7

    // ← ОБНОВЛЯЕМ scrollPosition когда меняются данные
    LaunchedEffect(lastWeights.size) {
        if (lastWeights.size > maxItemsToShow) {
            scrollPosition = lastWeights.size - maxItemsToShow
        }
    }

    val currentWeight = if (lastWeights.isNotEmpty())
        lastWeights.last().weight
    else
        currentUser?.targetWeight ?: 0f

    val previousWeight = if (lastWeights.size >= 7) {
        lastWeights[lastWeights.size - 7].weight
    } else if (lastWeights.isNotEmpty()) {
        lastWeights.first().weight
    } else {
        currentWeight
    }

    val weightChange = currentWeight - previousWeight

    val chartsData = if (lastWeights.size > maxItemsToShow) {
        lastWeights.drop(scrollPosition).take(maxItemsToShow)
    } else {
        lastWeights
    }

    val canScrollLeft = scrollPosition > 0
    val canScrollRight = (scrollPosition + maxItemsToShow) < lastWeights.size

    var showWeightDialog by remember { mutableStateOf(false) }

    if (showWeightDialog) {
        AddWeightDialog(
            onDismiss = { showWeightDialog = false },
            onAdd = { weight, dateTimestamp ->
                viewModel.addHealthData(
                    weight = weight,
                    heartRate = 0,
                    sys = 0,
                    dia = 0,
                    steps = 0,
                    sleep = 0f,
                    water = 0f,
                    dateTimestamp = dateTimestamp
                )
                showWeightDialog = false
            },
            healthDataList = healthDataList,
            currentUser = currentUser
        )
    }

    // Диалог редактирования
    if (showEditDialog && selectedIndex >= 0 && selectedIndex < chartsData.size) {
        val selectedData = chartsData[selectedIndex]
        var showDeleteConfirm by remember { mutableStateOf(false) }
        var isEditMode by remember { mutableStateOf(false) }
        var editedWeight by remember { mutableStateOf(String.format("%.1f", selectedData.weight)) }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Удалить запись?") },
                text = { Text("Вы уверены? Запись будет удалена безвозвратно.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteHealthData(selectedData)
                            showEditDialog = false
                            showDeleteConfirm = false
                            selectedIndex = -1
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Отмена")
                    }
                }
            )
        } else if (isEditMode) {
            // ДИАЛОГ РЕДАКТИРОВАНИЯ ВЕСА
            AlertDialog(
                onDismissRequest = { isEditMode = false },
                title = { Text("Изменить вес") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Дата: ${formatDateShort(selectedData.date)}",
                            fontSize = 14.sp
                        )
                        TextField(
                            value = editedWeight,
                            onValueChange = { editedWeight = it },
                            label = { Text("Вес (кг)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newWeight = editedWeight.toFloatOrNull()
                            if (newWeight != null && newWeight > 0) {
                                viewModel.deleteHealthData(selectedData)
                                viewModel.addHealthData(
                                    weight = newWeight,
                                    heartRate = 0,
                                    sys = 0,
                                    dia = 0,
                                    steps = 0,
                                    sleep = 0f,
                                    water = 0f,
                                    dateTimestamp = selectedData.date
                                )
                                isEditMode = false
                                showEditDialog = false
                                selectedIndex = -1
                            }

                        },
                        enabled = editedWeight.toFloatOrNull() != null && editedWeight.toFloatOrNull()!! > 0
                    ) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isEditMode = false }) {
                        Text("Отмена")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Запись о весе") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Дата: ${formatDateShort(selectedData.date)}")
                        Text("Вес: ${String.format("%.1f", selectedData.weight)} кг")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { isEditMode = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Редактировать")
                    }
                },
                dismissButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Закрыть")
                        }
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Удалить")
                        }
                    }
                }
            )
        }
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Отслеживание веса",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { showWeightDialog = true },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "+ Добавить",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Текущий вес",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f кг", currentWeight),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Изменение",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (weightChange > 0) "↑" else if (weightChange < 0) "↓" else "→"} ${String.format("%.1f кг", kotlin.math.abs(weightChange))}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (weightChange > 0) MaterialTheme.colorScheme.error else if (weightChange < 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ГРАФИК С ФИКСИРОВАННОЙ ВЫСОТОЙ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Левая стрелка
                    IconButton(
                        onClick = {
                            if (canScrollLeft) {
                                scrollPosition -= 1
                            }
                        },
                        enabled = canScrollLeft,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            "◀",
                            fontSize = 16.sp,
                            color = if (canScrollLeft) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    // График
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (chartsData.isNotEmpty()) {
                                val maxWeight = chartsData.maxOf { it.weight }
                                val minWeight = chartsData.minOf { it.weight }
                                val range = maxWeight - minWeight
                                val safeRange = if (range < 1) 2f else range

                                chartsData.forEachIndexed { index, data ->
                                    val heightPercent = if (safeRange > 0) {
                                        ((data.weight - minWeight) / safeRange * 0.85f) + 0.15f
                                    } else {
                                        0.5f
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(heightPercent)
                                            .background(
                                                color = if (index == selectedIndex) MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                            .clickable {
                                                selectedIndex = index
                                                showEditDialog = true
                                            }
                                    )
                                }
                            } else {
                                Text(
                                    text = "Нет данных",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }

                    // Правая стрелка
                    IconButton(
                        onClick = {
                            if (canScrollRight) {
                                scrollPosition += 1
                            }
                        },
                        enabled = canScrollRight,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            "▶",
                            fontSize = 16.sp,
                            color = if (canScrollRight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                // ТЕКСТ С ЗНАЧЕНИЯМИ ОТДЕЛЬНО ПОД ГРАФИКОМ
                if (chartsData.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp, end = 40.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        chartsData.forEach { data ->
                            Text(
                                text = formatWeight(data.weight),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Последние записи
            if (lastWeights.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "${lastWeights.size} ${if (lastWeights.size % 10 == 1 && lastWeights.size % 100 != 11) "запись" else "записей"} • Нажмите на столбец для редактирования",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Добавь эту функцию в файл
fun formatWeight(weight: Float): String {
    return if (weight == weight.toInt().toFloat()) {
        // Если вес целое число, показываем без дробной части
        weight.toInt().toString()
    } else {
        // Иначе показываем одно или два знака после запятой
        String.format("%.1f", weight).trimEnd('0').trimEnd('.')
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
    onAdd: (Float, Long) -> Unit,
    healthDataList: List<HealthData>,
    currentUser: User?
) {
    var weightVal by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getTodayTimestamp()) }
    val today = getTodayTimestamp()

    val hasWeightForSelectedDate = remember(healthDataList, selectedDate) {
        healthDataList.any { it.date == selectedDate && it.weight > 0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить вес") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ← СНАЧАЛА выбор даты
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = selectedDate
                            calendar.add(Calendar.DAY_OF_YEAR, -1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            selectedDate = calendar.timeInMillis
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("◄", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = formatDateShort(selectedDate),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (hasWeightForSelectedDate) {
                            Text(
                                text = "Уже есть запись",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = selectedDate
                            calendar.add(Calendar.DAY_OF_YEAR, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            if (calendar.timeInMillis <= today) {
                                selectedDate = calendar.timeInMillis
                            }
                        },
                        enabled = selectedDate < today,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            "►",
                            fontSize = 16.sp,
                            color = if (selectedDate < today)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                // ← ПОТОМ вес
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
                    if (weightVal.isNotEmpty() && !hasWeightForSelectedDate) {
                        onAdd(weightVal.toFloatOrNull() ?: 70f, selectedDate)
                    }
                },
                enabled = weightVal.isNotEmpty() && !hasWeightForSelectedDate
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

    // Рекомендации ВОЗ с учетом пола и активности
    val recommendedWater = remember(currentUser) {
        currentUser?.let { u ->
            // Формула ВОЗ
            val baseIntake = when (u.gender) {
                "male" -> u.targetWeight * 35 // 35мл на 1кг для мужчин
                "female" -> u.targetWeight * 31 // 31мл на 1кг для женщин
                else -> u.targetWeight * 33
            }

            val activityMultiplier = when (u.activityLevel) {
                "sedentary" -> 1.0f
                "light" -> 1.2f
                "moderate" -> 1.4f
                "active" -> 1.6f
                "very_active" -> 1.8f
                else -> 1.0f
            }
            baseIntake * activityMultiplier / 1000 // Конвертируем в литры
        } ?: 0f
    }

    val glassesCount = (recommendedWater * 4).toInt() // 1 стакан ≈ 250мл (0.25л)

    val activityName = when (currentUser?.activityLevel) {
        "sedentary" -> "Малоподвижный образ жизни"
        "light" -> "Легкие тренировки 1–2 раза в неделю"
        "moderate" -> "Умеренные тренировки 3–4 раза в неделю"
        "active" -> "Активные тренировки 5+ раз в неделю"
        "very_active" -> "Ежедневные интенсивные нагрузки"
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
                text = "≈ $glassesCount стаканов в день (250мл)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

