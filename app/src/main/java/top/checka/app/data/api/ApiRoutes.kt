package top.checka.app.data.api

/**
 * Defines the contract for the PHP Backend API.
 * This can be used with Retrofit later.
 */
interface ApiRoutes {
    /*
    // Example Retrofit definition:
    
    @POST("v1/register_token.php")
    suspend fun registerToken(@Body token: String): Response<Unit>

    @POST("v1/score.php")
    suspend fun submitScore(@Body scoreData: ScoreData): Response<Unit>
    
    @GET("v1/leaderboard.php")
    suspend fun getLeaderboard(): Response<List<ScoreEntry>>
    */

    companion object {
        const val BASE_URL = "https://your-cpanel-domain.com/api/"
        
        // Endpoints expected on the PHP side:
        const val ENDPOINT_REGISTER_TOKEN = "v1/register_token.php"
        const val ENDPOINT_SUBMIT_SCORE = "v1/submit_score.php"
        const val ENDPOINT_GET_LEADERBOARD = "v1/get_leaderboard.php"
    }
}
