package top.checka.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: MatchResult)

    @Query("SELECT * FROM match_results ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchResult>>

    @Query("SELECT * FROM match_results WHERE mode = :modeName AND (difficulty = :diffName OR :diffName IS NULL) ORDER BY totalTurns ASC LIMIT 10")
    fun getBestMatches(modeName: String, diffName: String?): Flow<List<MatchResult>>
    
    @Query("DELETE FROM match_results")
    suspend fun deleteAll()
}
