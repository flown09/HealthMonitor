package com.example.healthmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.healthmonitor.database.HealthDatabase
import com.example.healthmonitor.repository.HealthRepository
import com.example.healthmonitor.screens.HealthScreen
import com.example.healthmonitor.screens.NutritionScreen
import com.example.healthmonitor.screens.ProfileScreen
import com.example.healthmonitor.ui.theme.HealthMonitorTheme
import com.example.healthmonitor.utils.StepCounter
import com.example.healthmonitor.viewmodels.HealthViewModel
import com.example.healthmonitor.viewmodels.HealthViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    private lateinit var stepCounter: StepCounter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Permission granted, starting step counter")
                stepCounter.startListening()
            } else {
                Log.d("MainActivity", "Permission denied for activity recognition")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        try {
            stepCounter = StepCounter(this)

            // Запрашиваем permission для шагов
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        Log.d("MainActivity", "Permission already granted")
                        stepCounter.startListening()
                    }
                    else -> {
                        Log.d("MainActivity", "Requesting permission")
                        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }
            } else {
                stepCounter.startListening()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing step counter: ${e.message}", e)
        }

        setContent {
            HealthMonitorTheme {
                val database = HealthDatabase.getDatabase(this@MainActivity)
                val repository = HealthRepository(database)
                val factory = HealthViewModelFactory(repository)
                val viewModel = ViewModelProvider(this@MainActivity, factory).get(HealthViewModel::class.java)

                HealthMonitorApp(viewModel, stepCounter)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            stepCounter.stopListening()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error stopping step counter: ${e.message}")
        }
    }
}

@Composable
fun HealthMonitorApp(viewModel: HealthViewModel, stepCounter: StepCounter) {
    val selectedTab = remember { mutableStateOf(2) }

    LaunchedEffect(Unit) {
        stepCounter.stepCount
            .onEach { steps ->
                viewModel.saveTodayStepsToDatabase(steps)
            }
            .launchIn(this)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Здоровье") },
                    label = { Text("Здоровье", color = if (selectedTab.value == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                    selected = selectedTab.value == 2,
                    onClick = { selectedTab.value = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocalDining, contentDescription = "Питание") },
                    label = { Text("Питание", color = if (selectedTab.value == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                    selected = selectedTab.value == 1,
                    onClick = { selectedTab.value = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Профиль") },
                    label = { Text("Профиль", color = if (selectedTab.value == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
                    selected = selectedTab.value == 0,
                    onClick = { selectedTab.value = 0 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab.value) {
            0 -> ProfileScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            1 -> NutritionScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            2 -> HealthScreen(viewModel = viewModel, stepCounter = stepCounter, modifier = Modifier.padding(paddingValues))
        }
    }
}
