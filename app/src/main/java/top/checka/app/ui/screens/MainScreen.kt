package top.checka.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import top.checka.app.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings

@Composable
fun MainScreen(
    navController: androidx.navigation.NavHostController,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide BottomBar on Game Screen
    val isGameScreen = currentRoute?.startsWith("game") == true

    Scaffold(
        bottomBar = {
            if (!isGameScreen) {
                NavigationBar {
                    // Home
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Leaderboard
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                        label = { Text("Rankings") },
                        selected = currentRoute == "leaderboard",
                        onClick = {
                            navController.navigate("leaderboard") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // How to Play (Use "Info" or "Tutorial" route - treating as a full screen for nav simplicity)
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "How To Play") },
                        label = { Text("Help") },
                        selected = currentRoute == "tutorial",
                        onClick = {
                            navController.navigate("tutorial") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    // Settings
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}
