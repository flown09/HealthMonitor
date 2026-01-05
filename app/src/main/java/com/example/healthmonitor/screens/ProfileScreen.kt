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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Профиль",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Информация пользователя
        currentUser?.let { user ->
            UserInfoCard(user)

            // BMI
            BMICard(viewModel, user)

            // Калории
            CaloriesCard(viewModel)

            // Вода
            WaterCard(user)

            // Последние данные здоровья
            LastHealthDataCard(healthDataList)
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
            InfoRow("Активность", user.activityLevel)
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
fun CaloriesCard(viewModel: HealthViewModel) {
    val dailyCalories = viewModel.calculateDailyCalories()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val remaining = dailyCalories - todayCalories

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
                text = "Калории сегодня",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

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
    val waterNorm = String.format("%.1f", (user.targetWeight * 35) / 1000)

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
                text = "Это примерно ${(waterNorm.toFloat() * 1000).toInt() / 250} стаканов по 250мл",
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
                    Column(modifier = Modifier.weight(1f)) {
                        MetricBox("Вес", "${lastData.weight}", "кг")
                        MetricBox("Шаги", "${lastData.steps}", "")
                    }
                    Column(modifier = Modifier.weight(1f)) {
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
