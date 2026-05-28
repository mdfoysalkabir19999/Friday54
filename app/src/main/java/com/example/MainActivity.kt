package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FridayViewModel
import com.example.ui.screens.CoreScreen
import com.example.ui.screens.LogsScreen
import com.example.ui.screens.MatrixScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FridayAppContainer()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FridayAppContainer(
    viewModel: FridayViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isKeyboardVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isKeyboardVisible) {
                NavigationBar(
                    containerColor = CyberCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("friday_bottom_navigation")
                ) {
                // Tab 0: Neuro-Core
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "Neuro-Core Link", modifier = Modifier.size(22.dp)) },
                    label = { Text("NEURO-CORE", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary.copy(alpha = 0.1f),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    ),
                    modifier = Modifier.testTag("tab_core")
                )

                // Tab 1: Terminal Logs
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Autonomous trace console", modifier = Modifier.size(22.dp)) },
                    label = { Text("TRACE LOGS", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary.copy(alpha = 0.1f),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    ),
                    modifier = Modifier.testTag("tab_logs")
                )

                // Tab 2: Encrypted Vault
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Lock, contentDescription = "Encrypted Vault records", modifier = Modifier.size(22.dp)) },
                    label = { Text("DATA VAULT", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary.copy(alpha = 0.1f),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    ),
                    modifier = Modifier.testTag("tab_vault")
                )

                // Tab 3: Matrix System
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Core cognitive metrics", modifier = Modifier.size(22.dp)) },
                    label = { Text("CORE MATRIX", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberPrimary,
                        selectedTextColor = CyberPrimary,
                        indicatorColor = CyberPrimary.copy(alpha = 0.1f),
                        unselectedIconColor = Color(0xFF888888),
                        unselectedTextColor = Color(0xFF888888)
                    ),
                    modifier = Modifier.testTag("tab_matrix")
                )
            }
        }
    }
) { innerPadding ->
        when (selectedTab) {
            0 -> CoreScreen(viewModel = viewModel, paddingValues = innerPadding)
            1 -> LogsScreen(viewModel = viewModel, paddingValues = innerPadding)
            2 -> VaultScreen(viewModel = viewModel, paddingValues = innerPadding)
            3 -> MatrixScreen(viewModel = viewModel, paddingValues = innerPadding)
        }
    }
}
