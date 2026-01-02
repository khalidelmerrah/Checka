package top.checka.app.data

import top.checka.app.data.api.ApiClient
import top.checka.app.data.api.FindMatchRequest
import top.checka.app.data.api.FindMatchResponse
import top.checka.app.data.api.ReportMatchRequest
import top.checka.app.data.api.ReportMatchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RankedRepository {

    suspend fun findMatch(userId: String, elo: Int): Result<FindMatchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.service.findMatch(FindMatchRequest(userId, elo))
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Matchmaking failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun reportMatch(
        player1Id: String,
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
                    player1Id = player1Id,
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
    suspend fun updateProfile(userId: String, avatarUrl: String?, displayName: String?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val req = top.checka.app.data.api.UpdateProfileRequest(userId, avatarUrl, displayName)
                val response = ApiClient.service.updateProfile(req)
                response.isSuccessful && response.body()?.success == true
            } catch (e: Exception) {
                false
            }
        }
    }
}
