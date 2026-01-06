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
import com.example.healthmonitor.models.User
import com.example.healthmonitor.viewmodels.HealthViewModel

@Composable
fun ProfileScreen(viewModel: HealthViewModel, modifier: Modifier = Modifier) {
    val currentUser by viewModel.currentUser.collectAsState()
    val healthDataList by viewModel.healthDataList.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

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
                text = "Профиль",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.height(40.dp)
            ) {
                Text("Редактировать")
            }
        }

        currentUser?.let { user ->
            UserInfoCard(user)

            BMICard(viewModel, user)

            CaloriesCard(viewModel, user)

            WaterCard(user)

            LastHealthDataCard(healthDataList)
        }
    }

    if (showEditDialog) {
        currentUser?.let { user ->
            EditProfileDialog(
                user = user,
                onDismiss = { showEditDialog = false },
                onSave = { name, age, heightCm, targetWeight, activityLevel, weightGoal, stepGoal ->
                    viewModel.updateUser(name, age, heightCm, targetWeight, activityLevel, weightGoal, stepGoal)
                    showEditDialog = false
                }
            )
        }
    }


}

@Composable
fun UserInfoCard(user: User) {
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
                text = "Информация",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            InfoRow("Имя", user.name)
            InfoRow("Возраст", "${user.age} лет")
            InfoRow("Рост", "${user.heightCm} см")
            InfoRow("Целевой вес", "${user.targetWeight} кг")
            InfoRow("Активность", getActivityName(user.activityLevel))
            InfoRow("Цель по весу", getWeightGoalName(user.weightGoal))
        }
    }
}

@Composable
fun BMICard(viewModel: HealthViewModel, user: User) {
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
fun CaloriesCard(viewModel: HealthViewModel, user: User) {
    val dailyCalories = viewModel.calculateDailyCalories()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val remaining = dailyCalories - todayCalories

    val goalText = when(user.weightGoal) {
        "lose" -> "📉 Снижение веса (-15%)"
        "maintain" -> "➡️ Удержание веса"
        "gain" -> "📈 Набор веса (+15%)"
        else -> "Нормальный режим"
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
            Column {
                Text(
                    text = "Калории сегодня",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = goalText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricBox("Потреблено", "$todayCalories", "ккал")
                MetricBox("Осталось", "$remaining", "ккал")
            }

            LinearProgressIndicator(
                progress = { todayCalories.toFloat() / dailyCalories.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Дневная норма: $dailyCalories ккал",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WaterCard(user: User) {
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


@Composable
fun LastHealthDataCard(healthDataList: List<com.example.healthmonitor.models.HealthData>) {
    val lastData = healthDataList.firstOrNull()

    if (lastData != null) {
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
                    text = "Последние показатели",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        MetricBox("Вес", "${lastData.weight}", "кг")
                        MetricBox("Шаги", "${lastData.steps}", "")
                    }
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        MetricBox("Пульс", "${lastData.heartRate}", "уд/мин")
                        MetricBox("Сон", "${lastData.sleepHours}", "ч")
                    }
                }

                Text(
                    text = "Давление: ${lastData.bloodPressureSystolic}/${lastData.bloodPressureDiastolic} мм рт.ст.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MetricBox(label: String, value: String, unit: String) {
    Column(
        modifier = Modifier
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, Int, Float, Float, String, String, Int) -> Unit
) {
    var nameVal by remember { mutableStateOf(user.name) }
    var ageVal by remember { mutableStateOf(user.age.toString()) }
    var heightVal by remember { mutableStateOf(user.heightCm.toString()) }
    var targetWeightVal by remember { mutableStateOf(user.targetWeight.toString()) }
    var activityVal by remember { mutableStateOf(user.activityLevel) }
    var weightGoalVal by remember { mutableStateOf(user.weightGoal) }
    var stepGoalVal by remember { mutableStateOf(user.dailyStepGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать профиль") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = nameVal,
                    onValueChange = { nameVal = it },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = ageVal,
                    onValueChange = { ageVal = it },
                    label = { Text("Возраст") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = heightVal,
                    onValueChange = { heightVal = it },
                    label = { Text("Рост (см)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = targetWeightVal,
                    onValueChange = { targetWeightVal = it },
                    label = { Text("Целевой вес (кг)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = stepGoalVal,
                    onValueChange = { stepGoalVal = it },
                    label = { Text("Цель по шагам (шагов/день)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Уровень активности:", fontWeight = FontWeight.Bold)
                val activities = listOf("sedentary", "light", "moderate", "active", "very_active")
                activities.forEach { activity ->
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        RadioButton(
                            selected = activityVal == activity,
                            onClick = { activityVal = activity }
                        )
                        Text(
                            text = getActivityName(activity),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Text("Цель по весу:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                val goals = listOf("lose", "maintain", "gain")
                goals.forEach { goal ->
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        RadioButton(
                            selected = weightGoalVal == goal,
                            onClick = { weightGoalVal = goal }
                        )
                        Text(
                            text = getWeightGoalName(goal),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        nameVal,
                        ageVal.toIntOrNull() ?: user.age,
                        heightVal.toFloatOrNull() ?: user.heightCm,
                        targetWeightVal.toFloatOrNull() ?: user.targetWeight,
                        activityVal,
                        weightGoalVal,
                        stepGoalVal.toIntOrNull() ?: user.dailyStepGoal
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}


fun getActivityName(level: String): String = when(level) {
    "sedentary" -> "Малоподвижный образ жизни"
    "light" -> "Легкая активность (1-3 дня в неделю)"
    "moderate" -> "Умеренная активность (3-5 дней)"
    "active" -> "Высокая активность (6-7 дней)"
    "very_active" -> "Очень высокая (ежедневные интенсивные тренировки)"
    else -> "Неизвестно"
}

fun getWeightGoalName(goal: String): String = when(goal) {
    "lose" -> "📉 Сбросить вес"
    "maintain" -> "➡️ Удерживать вес"
    "gain" -> "📈 Набрать вес"
    else -> "Неизвестно"
}
