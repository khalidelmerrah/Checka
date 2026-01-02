package top.checka.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CheckaApiService {

    @POST("find_match.php")
    suspend fun findMatch(@Body request: FindMatchRequest): Response<FindMatchResponse>

    @POST("report_match.php")
    suspend fun reportMatch(@Body request: ReportMatchRequest): Response<ReportMatchResponse>

    @POST("auth.php")
    suspend fun authenticate(@Body request: AuthRequest): Response<AuthResponse>

    @POST("update_profile.php")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>

    @retrofit2.http.GET("leaderboard.php")
    suspend fun getLeaderboard(
        @retrofit2.http.Query("limit") limit: Int = 50,
        @retrofit2.http.Query("user_id") userId: String? = null
    ): Response<LeaderboardResponse>
}

// --- DTOs ---

data class LeaderboardResponse(
    val leaderboard: List<LeaderboardEntry>,
    @SerializedName("user_rank") val userRank: LeaderboardEntry? = null,
    val error: String? = null
)

data class LeaderboardEntry(
    val id: String,
    val username: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null, // New field from PHP
    @SerializedName("elo_rating") val eloRating: Int,
    @SerializedName("total_wins") val totalWins: Int,
    @SerializedName("total_matches") val totalMatches: Int,
    val xp: Int = 0,
    val level: Int = 1,
    @SerializedName("rank_title") val rankTitle: String? = null,
    @SerializedName("win_rate") val winRate: String? = null,
    val rank: Int? = null
)

data class AuthRequest(
    val code: String
)

data class AuthResponse(
    val success: Boolean,
    @SerializedName("user_id") val userId: String?,
    val username: String?,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val elo: Int?,
    val xp: Int?,
    val level: Int?,
    val message: String? = null
)


data class FindMatchRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("elo") val elo: Int
)

data class FindMatchResponse(
    val success: Boolean,
    @SerializedName("is_bot") val isBot: Boolean = false,
    val opponent: OpponentData? = null,
    val message: String? = null
)

data class OpponentData(
    val id: String,
    val name: String,
    val elo: Int,
    val title: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val level: Int = 1,
    @SerializedName("win_rate") val winRate: String? = null,
    @SerializedName("total_games") val totalGames: Int = 0
)

data class ReportMatchRequest(
    @SerializedName("player1_id") val player1Id: String,
    @SerializedName("player2_id") val player2Id: String?,
    @SerializedName("winner_id") val winnerId: String?,
    @SerializedName("game_mode") val gameMode: String,
    @SerializedName("difficulty") val difficulty: String?,
    @SerializedName("duration") val duration: Long,
    @SerializedName("turns") val turns: Int
)

data class ReportMatchResponse(
    val success: Boolean,
    val winner: PlayerResult?,
    val loser: PlayerResult?,
    val error: String? = null
)

data class PlayerResult(
    val id: String,
    @SerializedName("old_rating") val oldRating: Int,
    @SerializedName("new_rating") val newRating: Int,
    val gain: Int = 0,
    @SerializedName("xp_earned") val xpEarned: Int = 0,
    @SerializedName("xp_total") val xpTotal: Int = 0,
    val level: Int = 1,
    val title: String,
    val bonuses: Map<String, Int>? = null
)

data class UpdateProfileRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("display_name") val displayName: String? = null
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
