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
        }
    }

    if (showEditDialog) {
        currentUser?.let { user ->
            EditProfileDialog(
                user = user,
                onDismiss = { showEditDialog = false },
                onSave = { name, gender, age, heightCm, targetWeight, activityLevel, weightGoal, stepGoal ->
                    viewModel.updateUser(name, gender, age, heightCm, targetWeight, activityLevel, weightGoal, stepGoal)
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
            InfoRow("Пол", getGenderName(user.gender))
            InfoRow("Возраст", "${user.age} лет")
            InfoRow("Рост", "${user.heightCm} см")
            InfoRow("Целевой вес", "${user.targetWeight} кг")
            InfoRow("Активность", getActivityName(user.activityLevel))
            InfoRow("Цель по весу", getWeightGoalName(user.weightGoal))
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
    onSave: (String, String, Int, Float, Float, String, String, Int) -> Unit
) {
    var nameVal by remember { mutableStateOf(user.name) }
    var genderVal by remember { mutableStateOf(user.gender) }
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

                Text("Пол:", fontWeight = FontWeight.Bold)
                val genders = listOf("male", "female")
                genders.forEach { gender ->
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        RadioButton(
                            selected = genderVal == gender,
                            onClick = { genderVal = gender }
                        )
                        Text(
                            text = getGenderName(gender),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }


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
                        genderVal,
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

fun getGenderName(gender: String): String = when(gender) {
    "male" -> "Мужчина"
    "female" -> "Женщина"
    else -> "Не указано"
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
    "lose" -> "Сбросить вес"
    "maintain" -> "Удерживать вес"
    "gain" -> "Набрать вес"
    else -> "Неизвестно"
}

