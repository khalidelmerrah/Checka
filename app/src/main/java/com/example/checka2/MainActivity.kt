package com.example.checka2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.checka2.data.UserPreferences
import com.example.checka2.domain.Difficulty
import com.example.checka2.domain.GameMode
import com.example.checka2.ui.screens.GameScreen
import com.example.checka2.ui.screens.HomeScreen
import com.example.checka2.ui.screens.LeaderboardScreen
import com.example.checka2.ui.screens.SettingsScreen
import com.example.checka2.ui.theme.CheckaTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import com.example.checka2.utils.AnalyticsHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val analyticsHelper = AnalyticsHelper(this)
        analyticsHelper.logAppOpen()

        setContent {
            val context = LocalContext.current
            val themePref = UserPreferences.getTheme(context).collectAsState(initial = "System")
            
            val useDarkTheme = when (themePref.value) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            CheckaTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onStartGame = { mode, diff, p1, p2 ->
                                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                                scope.launch {
                                    val isFirst = kotlinx.coroutines.flow.first(UserPreferences.isFirstGame(context))
                                    if (isFirst) {
                                        analyticsHelper.logFirstGame()
                                        UserPreferences.setFirstGamePlayed(context)
                                    }
                                    analyticsHelper.logGameStart(mode.name, diff.name)
                                }
                                
                                val route = "game/${mode.name}/${diff.name}/$p1/$p2"
                                navController.navigate(route)
                            },
                            onNavigateLeaderboard = { navController.navigate("leaderboard") },
                            onNavigateSettings = { navController.navigate("settings") }
                        )
                    }

                    composable(
                        route = "game/{mode}/{diff}/{p1}/{p2}",
                        arguments = listOf(
                            navArgument("mode") { type = NavType.StringType },
                            navArgument("diff") { type = NavType.StringType },
                            navArgument("p1") { type = NavType.StringType },
                            navArgument("p2") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val modeStr = backStackEntry.arguments?.getString("mode") ?: "PassAndPlay"
                        val diffStr = backStackEntry.arguments?.getString("diff") ?: "Easy"
                        val p1 = backStackEntry.arguments?.getString("p1") ?: "P1"
                        val p2 = backStackEntry.arguments?.getString("p2") ?: "P2"
                        
                        val mode = try { GameMode.valueOf(modeStr) } catch (e: Exception) { GameMode.PassAndPlay }
                        val diff = try { Difficulty.valueOf(diffStr) } catch (e: Exception) { Difficulty.Easy }

                        val viewModel: com.example.checka2.ui.GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        
                        androidx.compose.runtime.LaunchedEffect(Unit) {
                            viewModel.startGame(mode, diff, p1, p2)
                        }

                        GameScreen(
                            viewModel = viewModel,
                            gameMode = mode,
                            onExit = { navController.popBackStack() }
                        )
                    }

                    composable("leaderboard") {
                        LeaderboardScreen(onBack = { navController.popBackStack() })
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onThemeChanged = { theme -> analyticsHelper.logThemeChanged(theme) },
                            onLanguageChanged = { lang -> analyticsHelper.setUserLanguage(lang) }
                        )
                    }
                }
            }
        }
    }
}
