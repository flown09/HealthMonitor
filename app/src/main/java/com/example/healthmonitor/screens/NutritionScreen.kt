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
import com.example.healthmonitor.models.Food
import com.example.healthmonitor.models.NutritionData
import com.example.healthmonitor.viewmodels.HealthViewModel

@Composable
fun NutritionScreen(viewModel: HealthViewModel, modifier: Modifier = Modifier) {
    val foods by viewModel.foods.collectAsState()
    val nutritionData by viewModel.nutritionDataList.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Дневник питания",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            CalorieSummaryCard(todayCalories, viewModel.calculateDailyCalories())

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(weight = 1f).height(40.dp)
                ) {
                    Text("Добавить еду")
                }

                Button(
                    onClick = { showAddFoodDialog = true },
                    modifier = Modifier.weight(weight = 1f).height(40.dp)
                ) {
                    Text("Новый продукт")
                }
            }
        }

        if (nutritionData.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Нет записей о еде",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(nutritionData) { nutrition ->
                    NutritionCard(nutrition)
                }
            }
        }
    }

    if (showAddDialog) {
        AddFoodDialog(
            foods = foods,
            onDismiss = { showAddDialog = false },
            onAdd = { food, portion, mealType ->
                viewModel.addNutritionData(food, portion, mealType)
                showAddDialog = false
            }
        )
    }

    if (showAddFoodDialog) {
        AddNewFoodDialog(
            onDismiss = { showAddFoodDialog = false },
            onAdd = { name, calories, protein, carbs, fat, category ->
                viewModel.addFood(name, calories, protein, carbs, fat, category)
                showAddFoodDialog = false
            }
        )
    }
}

@Composable
fun CalorieSummaryCard(todayCalories: Int, dailyGoal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Калории за день",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$todayCalories / $dailyGoal ккал",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${((todayCalories.toFloat() / dailyGoal) * 100).toInt()}%",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { (todayCalories.toFloat() / dailyGoal).coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        }
    }
}

@Composable
fun NutritionCard(nutrition: NutritionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = nutrition.foodName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = nutrition.mealType,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${nutrition.calories} ккал",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroIndicator("Б", nutrition.protein.toInt())
                MacroIndicator("Ж", nutrition.fat.toInt())
                MacroIndicator("У", nutrition.carbs.toInt())
            }
        }
    }
}

@Composable
fun MacroIndicator(label: String, val1: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.33f)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${val1}г",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun AddFoodDialog(
    foods: List<Food>,
    onDismiss: () -> Unit,
    onAdd: (Food, Float, String) -> Unit
) {
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var portion by remember { mutableStateOf("100") }
    var mealType by remember { mutableStateOf("breakfast") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить еду") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Продукт:", fontWeight = FontWeight.Bold)
                foods.forEach { food ->
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        RadioButton(
                            selected = selectedFood?.id == food.id,
                            onClick = { selectedFood = food }
                        )
                        Text(
                            text = food.name,
                            modifier = Modifier.padding(start = 8.dp).weight(weight = 1f)
                        )
                    }
                }

                Text("Порция (г):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                TextField(
                    value = portion,
                    onValueChange = { portion = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Прием пищи:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")
                mealTypes.forEach { meal ->
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        RadioButton(
                            selected = mealType == meal,
                            onClick = { mealType = meal }
                        )
                        Text(
                            text = when (meal) {
                                "breakfast" -> "Завтрак"
                                "lunch" -> "Обед"
                                "dinner" -> "Ужин"
                                else -> "Перекус"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                selectedFood?.let { onAdd(it, portion.toFloatOrNull() ?: 100f, mealType) }
            }) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun AddNewFoodDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Float, Float, Float, String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var categoryStr by remember { mutableStateOf("vegetables") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить новый продукт") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = caloriesStr,
                    onValueChange = { caloriesStr = it },
                    label = { Text("Калории (на 100г)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = proteinStr,
                    onValueChange = { proteinStr = it },
                    label = { Text("Белки (г)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = carbsStr,
                    onValueChange = { carbsStr = it },
                    label = { Text("Углеводы (г)") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = fatStr,
                    onValueChange = { fatStr = it },
                    label = { Text("Жиры (г)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Категория:", fontWeight = FontWeight.Bold)
                val categories = listOf("meat", "dairy", "vegetables", "fruits", "grains")
                categories.forEach { cat ->
                    Row {
                        RadioButton(
                            selected = categoryStr == cat,
                            onClick = { categoryStr = cat }
                        )
                        Text(
                            text = when (cat) {
                                "meat" -> "Мясо"
                                "dairy" -> "Молочное"
                                "vegetables" -> "Овощи"
                                "fruits" -> "Фрукты"
                                else -> "Зерна"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(
                    foodName,
                    caloriesStr.toIntOrNull() ?: 100,
                    proteinStr.toFloatOrNull() ?: 0f,
                    carbsStr.toFloatOrNull() ?: 0f,
                    fatStr.toFloatOrNull() ?: 0f,
                    categoryStr
                )
            }) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}