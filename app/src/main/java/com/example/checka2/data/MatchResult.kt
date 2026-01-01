package com.example.checka2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.checka2.domain.Difficulty
import com.example.checka2.domain.GameMode
import com.example.checka2.domain.Player

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
