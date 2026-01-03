package top.checka.app.data

import android.content.Context
import android.app.Activity
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val _currentPlayer = MutableStateFlow<Player?>(null)
    val currentPlayer: StateFlow<Player?> = _currentPlayer.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    suspend fun trySignIn(activity: Activity) {
        try {
            val result = PlayGames.getGamesSignInClient(activity).signIn().await()
            if (result.isAuthenticated) {
                _isAuthenticated.value = true
                fetchPlayerProfile(activity)
                
                // Get ID Token from Google Play Games
                try {
                    val idToken = PlayGames.getGamesSignInClient(activity)
                        .requestServerSideAccess(
                            context.getString(top.checka.app.R.string.default_web_client_id), 
                            /* forceRefreshToken = */ false
                        ).await()
                    
                    Log.d("AuthRepository", "Got Google ID Token")
                    
                    // Authenticate with backend using ID token
                    if (idToken != null) {
                        try {
                            val authResponse = top.checka.app.data.api.ApiClient.service.authenticate(
                                top.checka.app.data.api.AuthRequest(idToken)
                            )
                            val body = authResponse.body()
                            
                            if (authResponse.isSuccessful && body != null && body.success) {
                                // Check if account is banned
                                if (body.banned == true) {
                                    Log.w("AuthRepository", "Account is banned")
                                    _isAuthenticated.value = false
                                    // Could emit a specific banned state here
                                    return
                                }
                                
                                // Store session token in ApiClient
                                body.sessionToken?.let {
                                    top.checka.app.data.api.ApiClient.sessionToken = it
                                    Log.d("AuthRepository", "Session token stored")
                                }
                                
                                // Save user data locally
                                UserPreferences.saveUserStats(
                                    context, 
                                    userId = body.userId ?: "", 
                                    username = body.username ?: "Unknown", 
                                    elo = body.elo ?: 1200, 
                                    xp = body.xp ?: 0, 
                                    level = body.level ?: 1,
                                    avatarUrl = body.avatarUrl
                                )
                                Log.d("AuthRepository", "Backend Auth Success: ${body.userId}")
                            } else {
                                Log.e("AuthRepository", "Backend Auth Failed: ${body?.message} Code: ${authResponse.code()}")
                                _isAuthenticated.value = false
                            }
                        } catch (apiEx: Exception) {
                             Log.e("AuthRepository", "Backend API Error", apiEx)
                             _isAuthenticated.value = false
                        }
                    } else {
                        Log.e("AuthRepository", "Failed to get ID token from Google")
                        _isAuthenticated.value = false
                    }
                } catch (e: Exception) {
                    Log.e("AuthRepository", "Failed to retrieve ID token", e)
                    _isAuthenticated.value = false
                }

            } else {
                _isAuthenticated.value = false
                Log.d("AuthRepository", "Sign in failed")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in error", e)
            _isAuthenticated.value = false
        }
    }

    suspend fun checkPreviousSession(activity: Activity) {
        try {
            val result = PlayGames.getGamesSignInClient(activity).isAuthenticated.await()
            if (result.isAuthenticated) {
                _isAuthenticated.value = true
                fetchPlayerProfile(activity)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session check error", e)
        }
    }

    private suspend fun fetchPlayerProfile(activity: Activity) {
        try {
            val player = PlayGames.getPlayersClient(activity).currentPlayer.await()
            _currentPlayer.value = player
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error fetching player", e)
        }
    }
    suspend fun reportMatch(
        player2Id: String?,
        winnerId: String?,
        gameMode: String,
        difficulty: String?,
        duration: Long,
        turns: Int
    ) {
        try {
            val request = top.checka.app.data.api.ReportMatchRequest(
                player2Id = player2Id,
                winnerId = winnerId,
                gameMode = gameMode,
                difficulty = difficulty,
                duration = duration,
                turns = turns
            )
            
            val response = top.checka.app.data.api.ApiClient.service.reportMatch(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                Log.d("AuthRepository", "Match Reported Successfully: ${body}")
                
                // Update Local Stats from Server Response
                // We assume we are Player 1
                val myStats = if (player1Id == body.winner?.id) body.winner else body.loser
                
                if (myStats != null) {
                    UserPreferences.saveUserStats(
                        context,
                        userId = myStats.id, // Should match player1Id
                        username = myStats.title, // or name? API returns title/name
                        elo = myStats.newRating,
                        xp = myStats.xpTotal,
                        level = myStats.level,
                        avatarUrl = null
                    )
                }
            } else {
                Log.e("AuthRepository", "Failed to report match: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error reporting match", e)
        }
    }
}
