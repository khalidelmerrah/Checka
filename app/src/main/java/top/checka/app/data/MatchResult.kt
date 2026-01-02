package top.checka.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import top.checka.app.domain.Difficulty
import top.checka.app.domain.GameMode
import top.checka.app.domain.Player

@Entity(tableName = "match_results")
data class MatchResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode: GameMode, // Stored as Enum string via TypeConverter (or generic)
    val difficulty: String?, // Nullable if PassAndPlay
    val player1Name: String,
    val player2Name: String,
    val winnerName: String?,
    val totalTurns: Int,
    val timestamp: Long
)
