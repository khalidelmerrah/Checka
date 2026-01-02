package top.checka.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import top.checka.app.data.UserPreferences
import top.checka.app.domain.Difficulty
import top.checka.app.domain.GameMode
import top.checka.app.ui.screens.GameScreen
import top.checka.app.ui.screens.HomeScreen
import top.checka.app.ui.screens.LeaderboardScreen
import top.checka.app.ui.screens.SettingsScreen
import top.checka.app.ui.theme.CheckaTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import top.checka.app.utils.AnalyticsHelper

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge

import com.google.android.gms.games.PlayGamesSdk

import top.checka.app.data.AuthRepository
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.LaunchedEffect

import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        PlayGamesSdk.initialize(this)
        val authRepository = AuthRepository(this)
        
        installSplashScreen()
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val analyticsHelper = AnalyticsHelper(this)
        analyticsHelper.logAppOpen()

        setContent {
            val context = LocalContext.current
            val themePref = UserPreferences.getTheme(context).collectAsState(initial = "System")
            
            val isAuthenticated by authRepository.isAuthenticated.collectAsState()
            val currentPlayer by authRepository.currentPlayer.collectAsState()
            
            // Collect User Stats
            val userStats by UserPreferences.getUserStats(context).collectAsState(initial = null)

            LaunchedEffect(Unit) {
                authRepository.checkPreviousSession(this@MainActivity)
            }
            
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
                            isAuthenticated = isAuthenticated,
                            currentPlayer = currentPlayer,
                            userStats = userStats, // Passed here
                            onSignIn = { 
                                lifecycleScope.launch { authRepository.trySignIn(this@MainActivity) }
                            },
                            onUpdateProfile = { avatarUrl, displayName ->
                                val stats = userStats
                                if (stats?.userId != null) {
                                    val repo = top.checka.app.data.RankedRepository()
                                    lifecycleScope.launch {
                                        val result = repo.updateProfile(stats.userId, avatarUrl, displayName)
                                        
                                        if (result) {
                                            UserPreferences.saveUserStats(
                                                context,
                                                stats.userId,
                                                displayName ?: stats.username ?: "Player", // Update name if provided, else keep old
                                                stats.elo,
                                                stats.xp,
                                                stats.level,
                                                avatarUrl ?: stats.avatarUrl // Update avatar if provided, else keep old
                                            )
                                        }
                                    }
                                }
                            },
                            onStartGame = { mode, diff, p1, p2 ->
                                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                                scope.launch {
                                    val isFirst = UserPreferences.isFirstGame(context).first()
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

                        val viewModel: top.checka.app.ui.GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        
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
                        LeaderboardScreen(
                            userStats = userStats,
                            onBack = { navController.popBackStack() }
                        )
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
