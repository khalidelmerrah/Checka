package top.checka.app.data

import top.checka.app.data.api.ApiClient
import top.checka.app.data.api.FindMatchRequest
import top.checka.app.data.api.FindMatchResponse
import top.checka.app.data.api.ReportMatchRequest
import top.checka.app.data.api.ReportMatchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RankedRepository {

    suspend fun findMatchWithFallback(elo: Int): Result<FindMatchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Try API first (authentication via Bearer token)
                val response = ApiClient.service.findMatch(FindMatchRequest(elo))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        Result.success(body)
                    } else {
                        // Backend returned explicit failure
                         Result.success(createGhostBotResponse(elo, "No Match"))
                    }
                } else {
                     // Network/Server Error
                    Result.success(createGhostBotResponse(elo, "HTTP ${response.code()}"))
                }
            } catch (e: Exception) {
               // Exception (Offline etc)
               android.util.Log.e("RankedRepo", "Matchmaking failed: ${e.message}", e)
               Result.success(createGhostBotResponse(elo, e.javaClass.simpleName))
            }
        }
    }

    // Deprecated: Use findMatchWithFallback
    suspend fun findMatch(elo: Int): Result<FindMatchResponse> {
        return findMatchWithFallback(elo)
    }

    private fun createGhostBotResponse(playerElo: Int, errorMessage: String? = null): FindMatchResponse {
        val randomId = (1000..9999).random()
        // If error exists, show it in name for debugging
        val botName = if (errorMessage != null) "Err: ${errorMessage.take(15)}" else "Player $randomId" 
        
        val botData = top.checka.app.data.api.OpponentData(
            id = "bot_$randomId",
            name = botName,
            elo = playerElo + (-100..100).random(),
            title = "Beginner", 
            avatarUrl = null,
            level = (1..10).random(),
            winRate = "${(40..60).random()}%",
            totalGames = (50..200).random()
        )
        
        return FindMatchResponse(
            success = true,
            isBot = true,
            opponent = botData,
            message = "Ghost Bot Activated"
        )
    }

    suspend fun reportMatch(
        player2Id: String?,
        winnerId: String, 
        gameMode: String,
        difficulty: String?,
        duration: Long,
        turns: Int
    ): Result<ReportMatchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val req = ReportMatchRequest(
                    player2Id = player2Id,
                    winnerId = winnerId,
                    gameMode = gameMode,
                    difficulty = difficulty,
                    duration = duration,
                    turns = turns
                )
                val response = ApiClient.service.reportMatch(req)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Report failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    suspend fun getLeaderboard(limit: Int = 50, userId: String? = null): Result<top.checka.app.data.api.LeaderboardResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.service.getLeaderboard(limit, userId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: response.message()
                    Result.failure(Exception("Leaderboard failed: ${response.code()} - $errorMsg"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    suspend fun updateProfile(avatarUrl: String?, displayName: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = top.checka.app.data.api.UpdateProfileRequest(avatarUrl, displayName)
                val response = ApiClient.service.updateProfile(req)
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }
}
