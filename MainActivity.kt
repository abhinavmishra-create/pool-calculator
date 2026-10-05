package com.example.poolcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.poolcalculator.ui.theme.PoolCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PoolCalculatorTheme {
                Surface {
                    val nav = rememberNavController()
                    val vm: PoolViewModel = viewModel()   // one VM shared by all screens

                    NavHost(nav, startDestination = "input") {
                        composable("input") {
                            InputScreen(vm,
                                onCalculate = { nav.navigate("result") },
                                onSettings = { nav.navigate("settings") },
                                onHistory = { nav.navigate("history") })
                        }
                        composable("result") {
                            ResultScreen(vm, { nav.popBackStack() }, { nav.navigate("cost") })
                        }
                        composable("cost") { CostScreen(vm) { nav.popBackStack() } }
                        composable("settings") { SettingsScreen(vm) { nav.popBackStack() } }
                        composable("history") { HistoryScreen { nav.popBackStack() } }
                    }
                }
            }
        }
    }
}
