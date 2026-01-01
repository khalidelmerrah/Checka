package com.example.checka2.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class Player {
    P1, P2;

    fun other(): Player = if (this == P1) P2 else P1
}

enum class Orientation {
    Horizontal, Vertical
}

@Parcelize
data class Position(val row: Int, val col: Int) : Parcelable {
    fun offset(dRow: Int, dCol: Int) = Position(row + dRow, col + dCol)
}

@Parcelize
data class Wall(val row: Int, val col: Int, val orientation: Orientation) : Parcelable

enum class GameMode {
    PassAndPlay, Solo
}

enum class Difficulty {
    Easy, Hard, Master
}


sealed class GameAction {
    data class Move(val target: Position) : GameAction()
    data class PlaceWall(val wall: Wall) : GameAction()
}

@Parcelize
data class GameState(
    val p1Pos: Position = Position(8, 4),
    val p2Pos: Position = Position(0, 4),
    val walls: List<Wall> = emptyList(),
    val currentPlayer: Player = Player.P1,
    val p1WallsLeft: Int = 10,
    val p2WallsLeft: Int = 10,
    val winner: Player? = null,
    val turnCount: Int = 0
) : Parcelable {
    fun getPlayerPos(player: Player) = if (player == Player.P1) p1Pos else p2Pos
    fun getWallsLeft(player: Player) = if (player == Player.P1) p1WallsLeft else p2WallsLeft
}
