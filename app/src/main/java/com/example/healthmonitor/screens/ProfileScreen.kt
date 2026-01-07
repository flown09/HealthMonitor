package com.example.healthmonitor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
    var editingField by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Профиль",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        currentUser?.let { user ->
            UserInfoCard(user) { field ->
                editingField = field
                showEditDialog = true
            }
        }
    }

    if (showEditDialog && currentUser != null) {
        EditProfileDialog(
            user = currentUser!!,
            editingField = editingField,
            onDismiss = {
                showEditDialog = false
                editingField = null
            },
            onSave = { name, gender, age, heightCm, targetWeight, activityLevel, weightGoal, stepGoal ->
                viewModel.updateUser(name, gender, age, heightCm, targetWeight, activityLevel, weightGoal, stepGoal)
                showEditDialog = false
                editingField = null
            }
        )
    }
}

@Composable
fun UserInfoCard(user: User, onFieldClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            InfoRowClickable("Имя", user.name) { onFieldClick("name") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Пол", getGenderName(user.gender)) { onFieldClick("gender") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Возраст", "${user.age} лет") { onFieldClick("age") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Рост", "${user.heightCm} см") { onFieldClick("height") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Целевой вес", "${user.targetWeight} кг") { onFieldClick("weight") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Активность", getActivityNameShort(user.activityLevel)) { onFieldClick("activity") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Цель по весу", getWeightGoalName(user.weightGoal)) { onFieldClick("weightGoal") }
            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            InfoRowClickable("Цель по шагам", "${user.dailyStepGoal} шагов/день") { onFieldClick("steps") }
        }
    }
}

@Composable
fun InfoRowClickable(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    user: User,
    editingField: String?,
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
        title = {
            Text(
                when (editingField) {
                    "name" -> "Редактировать имя"
                    "gender" -> "Выбрать пол"
                    "age" -> "Редактировать возраст"
                    "height" -> "Редактировать рост"
                    "weight" -> "Редактировать вес"
                    "activity" -> "Выбрать активность"
                    "weightGoal" -> "Выбрать цель по весу"
                    "steps" -> "Редактировать цель по шагам"
                    else -> "Редактировать профиль"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (editingField) {
                    "name" -> {
                        TextField(
                            value = nameVal,
                            onValueChange = { nameVal = it },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "gender" -> {
                        val genders = listOf("male", "female")
                        genders.forEach { gender ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
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
                    }

                    "age" -> {
                        TextField(
                            value = ageVal,
                            onValueChange = { ageVal = it },
                            label = { Text("Возраст") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "height" -> {
                        TextField(
                            value = heightVal,
                            onValueChange = { heightVal = it },
                            label = { Text("Рост (см)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "weight" -> {
                        TextField(
                            value = targetWeightVal,
                            onValueChange = { targetWeightVal = it },
                            label = { Text("Целевой вес (кг)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "activity" -> {
                        val activities = listOf("sedentary", "light", "moderate", "active", "very_active")
                        activities.forEach { activity ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                RadioButton(
                                    selected = activityVal == activity,
                                    onClick = { activityVal = activity }
                                )
                                Text(
                                    text = getActivityNameFull(activity),
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    "weightGoal" -> {
                        val goals = listOf("lose", "maintain", "gain")
                        goals.forEach { goal ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
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

                    "steps" -> {
                        TextField(
                            value = stepGoalVal,
                            onValueChange = { stepGoalVal = it },
                            label = { Text("Цель по шагам (шагов/день)") },
                            modifier = Modifier.fillMaxWidth()
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

fun getActivityNameShort(level: String): String = when(level) {
    "sedentary" -> "Малоподвижный"
    "light" -> "Легкие тренировки"
    "moderate" -> "Умеренные тренировки"
    "active" -> "Активные тренировки"
    "very_active" -> "Ежедневные нагрузки"
    else -> "Неизвестно"
}

fun getActivityNameFull(level: String): String = when(level) {
    "sedentary" -> "Малоподвижный образ жизни"
    "light" -> "Легкие тренировки 1–2 раза в неделю"
    "moderate" -> "Умеренные тренировки 3–4 раза в неделю"
    "active" -> "Активные тренировки 5+ раз в неделю"
    "very_active" -> "Ежедневные интенсивные нагрузки"
    else -> "Неизвестно"
}


fun getWeightGoalName(goal: String): String = when(goal) {
    "lose" -> "Сбросить вес"
    "maintain" -> "Удерживать вес"
    "gain" -> "Набрать вес"
    else -> "Неизвестно"
}
