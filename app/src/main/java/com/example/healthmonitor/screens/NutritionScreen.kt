package com.example.healthmonitor.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthmonitor.models.Food
import com.example.healthmonitor.models.NutritionData
import com.example.healthmonitor.viewmodels.HealthViewModel
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign


@Composable
fun NutritionScreen(viewModel: HealthViewModel, modifier: Modifier = Modifier) {
    val foods by viewModel.foods.collectAsState()
    val nutritionData by viewModel.nutritionDataList.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val todayMacros by viewModel.todayMacros.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(getTodayTimestamp()) }

    // ← ПОЛУЧАЕМ СЕГОДНЯ
    val todayTimestamp = getTodayTimestamp()

    val nutritionForSelectedDate = remember(nutritionData, selectedDate) {
        nutritionData.filter { it.date == selectedDate }
    }

    val selectedDateCalories = remember(nutritionForSelectedDate) {
        nutritionForSelectedDate.sumOf { it.calories }
    }

    val selectedDateMacros = remember(nutritionForSelectedDate) {
        Triple(
            nutritionForSelectedDate.sumOf { it.protein.toInt() },
            nutritionForSelectedDate.sumOf { it.fat.toInt() },
            nutritionForSelectedDate.sumOf { it.carbs.toInt() }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp)
    ) {
        item {
            Text(
                text = "Дневник питания",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DatePickerRow(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                todayTimestamp = todayTimestamp  // ← ПЕРЕДАЁМ СЕГОДНЯ
            )
        }

        item {
            CalorieSummaryCard(selectedDateCalories, viewModel.calculateDailyCalories())
        }

        item {
            MacroSummaryCard(
                selectedDateMacros,
                viewModel.calculateDailyMacros()
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(weight = 1f).height(40.dp),
                    enabled = selectedDate <= todayTimestamp  // ← ОТКЛЮЧАЕМ ДЛЯ БУДУЩИХ ДАТ
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

        if (nutritionForSelectedDate.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "Нет записей о еде",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(nutritionForSelectedDate) { nutrition ->
                NutritionCard(
                    nutrition = nutrition,
                    onDelete = { viewModel.deleteNutritionData(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddFoodDialog(
            foods = foods,
            onDismiss = { showAddDialog = false },
            onAdd = { food, portion, mealType ->
                viewModel.addNutritionData(food, portion, mealType, selectedDate)
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
fun MacroSummaryCard(
    todayMacros: Triple<Int, Int, Int>,
    dailyGoalMacros: Triple<Int, Int, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "БЖУ за день",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Один Row с 3 макросами
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SimpleMacroBar(
                    label = "Б",
                    current = todayMacros.first,
                    goal = dailyGoalMacros.first,
                    color = Color(0xFF2180A8),
                    modifier = Modifier.weight(1f)  // ← ДОБАВЬ ЭТО
                )
                SimpleMacroBar(
                    label = "Ж",
                    current = todayMacros.second,
                    goal = dailyGoalMacros.second,
                    color = Color(0xFF20B89A),
                    modifier = Modifier.weight(1f)  // ← ДОБАВЬ ЭТО
                )
                SimpleMacroBar(
                    label = "У",
                    current = todayMacros.third,
                    goal = dailyGoalMacros.third,
                    color = Color(0xFF0066CC),
                    modifier = Modifier.weight(1f)  // ← ДОБАВЬ ЭТО
                )
            }
        }
    }
}

@Composable
fun SimpleMacroBar(
    label: String,
    current: Int,
    goal: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        LinearProgressIndicator(
            progress = { (current.toFloat() / goal).coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color
        )

        Text(
            text = "$current/$goal г",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}





@Composable
fun MacroBar(
    label: String,
    current: Int,
    goal: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.33f)
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "$current/$goal г",  // ← Убрал пробел: было "/ 275", теперь "/$goal"
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = { (current.toFloat() / goal).coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color
        )
    }
}


@Composable
fun DatePickerRow(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    todayTimestamp: Long
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate

    val dateFormatter = remember { SimpleDateFormat("dd MMM", Locale("ru")) }
    val dateString = remember(selectedDate, todayTimestamp) {
        if (selectedDate == todayTimestamp) {
            "Сегодня"
        } else {
            dateFormatter.format(Date(selectedDate))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.timeInMillis)
            },
            modifier = Modifier.height(36.dp).width(50.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("◀", fontSize = 16.sp)
        }

        Text(
            text = dateString,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    // Можно добавить DatePicker позже
                },
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.timeInMillis)
            },
            enabled = selectedDate < todayTimestamp,
            modifier = Modifier.height(36.dp).width(50.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("▶", fontSize = 16.sp)
        }
    }
}




// Helper функция для получения today timestamp
fun getTodayTimestamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
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
fun NutritionCard(nutrition: NutritionData, onDelete: (NutritionData) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showDeleteDialog = true }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = nutrition.foodName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${nutrition.portionGrams.toInt()}г",  // ← ДОБАВЬ ЭТО
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${nutrition.calories} ккал",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),  // ← Убери левый отступ
                horizontalArrangement = Arrangement.SpaceEvenly  // ← Используй SpaceEvenly вместо spacedBy
            ) {
                MacroIndicator("Б", nutrition.protein.toInt())
                MacroIndicator("Ж", nutrition.fat.toInt())
                MacroIndicator("У", nutrition.carbs.toInt())
            }
        }
    }

    // Диалог удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить запись?") },
            text = { Text("Вы уверены, что хотите удалить \"${nutrition.foodName}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(nutrition)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}




@Composable
fun MacroIndicator(label: String, val1: Int) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${val1}г",
            fontSize = 14.sp,
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    var portion by remember { mutableStateOf("100") }
    var mealType by remember { mutableStateOf("breakfast") }  // ← Оставляем по умолчанию, но скрываем из UI

    // Фильтруем продукты по поиску
    val filteredFoods = remember(searchQuery, foods) {
        if (searchQuery.isEmpty()) {
            foods
        } else {
            foods.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить еду") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Поисковая строка
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Поиск продукта") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Введите название...") }
                )

                // Список продуктов
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredFoods) { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFood = food }
                                .padding(8.dp)
                                .background(
                                    color = if (selectedFood?.id == food.id)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = food.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${food.calories} ккал/100г",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (selectedFood?.id == food.id) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Если ничего не найдено
                if (filteredFoods.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "Продукт не найден",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Порция (БЕЗ "Прием пищи")
                Text("Порция (г):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                TextField(
                    value = portion,
                    onValueChange = { portion = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedFood?.let {
                        onAdd(it, portion.toFloatOrNull() ?: 100f, "breakfast")  // ← Всегда "breakfast"
                    }
                },
                enabled = selectedFood != null
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
fun AddNewFoodDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Float, Float, Float, String) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }

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

                // ← ВСЕ RadioButton удали отсюда!
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
                    "vegetables"  // ← Используем категорию по умолчанию (овощи)
                )
            }) {
                Text("Добавить")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
