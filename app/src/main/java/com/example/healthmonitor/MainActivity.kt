package com.example.healthmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.healthmonitor.database.HealthDatabase
import com.example.healthmonitor.repository.HealthRepository
import com.example.healthmonitor.screens.HealthScreen
import com.example.healthmonitor.screens.NutritionScreen
import com.example.healthmonitor.screens.ProfileScreen
import com.example.healthmonitor.ui.theme.HealthMonitorTheme
import com.example.healthmonitor.viewmodels.HealthViewModel
import com.example.healthmonitor.viewmodels.HealthViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthMonitorTheme {
                val database = HealthDatabase.getDatabase(this@MainActivity)
                val repository = HealthRepository(database)
                val factory = HealthViewModelFactory(repository)
                val viewModel = ViewModelProvider(this@MainActivity, factory).get(HealthViewModel::class.java)

                HealthMonitorApp(viewModel)
            }
        }
    }
}

@Composable
fun HealthMonitorApp(viewModel: HealthViewModel) {
    val selectedTab = remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Профиль") },
                    label = { Text("Профиль") },
                    selected = selectedTab.value == 0,
                    onClick = { selectedTab.value = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Питание") },
                    label = { Text("Питание") },
                    selected = selectedTab.value == 1,
                    onClick = { selectedTab.value = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Здоровье") },
                    label = { Text("Здоровье") },
                    selected = selectedTab.value == 2,
                    onClick = { selectedTab.value = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab.value) {
            0 -> ProfileScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            1 -> NutritionScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
            2 -> HealthScreen(viewModel = viewModel, modifier = Modifier.padding(paddingValues))
        }
    }
}
