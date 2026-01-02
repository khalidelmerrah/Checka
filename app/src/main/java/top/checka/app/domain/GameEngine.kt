package top.checka.app.domain

import kotlin.math.abs

object GameEngine {

    const val BOARD_SIZE = 9
    const val P1_GOAL_ROW = 0
    const val P2_GOAL_ROW = 8
    
    fun applyAction(state: GameState, action: GameAction): GameState {
        return when (action) {
            is GameAction.Move -> applyMove(state, action.target)
            is GameAction.PlaceWall -> applyWall(state, action.wall)
        }
    }

    fun applyMove(state: GameState, target: Position): GameState {
        // Assume validated
        val isValid = canMove(state.currentPlayer, state.getPlayerPos(state.currentPlayer), target, state.walls, state.getPlayerPos(state.currentPlayer.other()))
        if (!isValid) return state // Or throw

        val newState = if (state.currentPlayer == Player.P1) {
            state.copy(p1Pos = target)
        } else {
            state.copy(p2Pos = target)
        }

        val winner = checkWin(newState)
        
        return newState.copy(
            currentPlayer = state.currentPlayer.other(),
            turnCount = state.turnCount + 1,
            winner = winner
        )
    }

    fun applyWall(state: GameState, wall: Wall): GameState {
        // Assume validated
        val isValid = isValidWallPlacement(wall, state.walls, state.p1Pos, state.p2Pos)
        if (!isValid) return state

        val wallsLeft = if (state.currentPlayer == Player.P1) state.p1WallsLeft else state.p2WallsLeft
        if (wallsLeft <= 0) return state

        val newWalls = state.walls + wall
        
        return if (state.currentPlayer == Player.P1) {
            state.copy(
                walls = newWalls,
                p1WallsLeft = state.p1WallsLeft - 1,
                currentPlayer = Player.P2,
                turnCount = state.turnCount + 1
            )
        } else {
            state.copy(
                walls = newWalls,
                p2WallsLeft = state.p2WallsLeft - 1,
                currentPlayer = Player.P1,
                turnCount = state.turnCount + 1
            )
        }
    }

    fun checkWin(state: GameState): Player? {
        if (state.p1Pos.row == P1_GOAL_ROW) return Player.P1
        if (state.p2Pos.row == P2_GOAL_ROW) return Player.P2
        return null
    }

    // --- Validation ---

    fun canMove(
        player: Player,
        current: Position,
        target: Position,
        walls: List<Wall>,
        opponentPos: Position
    ): Boolean {
        // 1. Bounds
        if (target.row !in 0 until BOARD_SIZE || target.col !in 0 until BOARD_SIZE) return false
        
        // 2. Occupancy
        if (target == opponentPos) return false

        // 3. Orthogonal & Distance 1
        val dr = abs(current.row - target.row)
        val dc = abs(current.col - target.col)
        if (!((dr == 1 && dc == 0) || (dr == 0 && dc == 1))) return false
        
        // 4. Walls
        if (Pathfinder.isBlocked(current, target, walls)) return false

        return true
    }

    fun isValidWallPlacement(
        newWall: Wall,
        existingWalls: List<Wall>,
        p1Pos: Position,
        p2Pos: Position
    ): Boolean {
        // 1. Bounds (Intersection 0..7)
        if (newWall.row !in 0..7 || newWall.col !in 0..7) return false

        // 2. Overlap / Crossing
        for (w in existingWalls) {
            // Same orientation overlap
            if (w.orientation == newWall.orientation) {
                if (w.orientation == Orientation.Horizontal) {
                    if (w.row == newWall.row && abs(w.col - newWall.col) < 2) return false
                } else {
                    if (w.col == newWall.col && abs(w.row - newWall.row) < 2) return false
                }
            } else {
                // Crossing: Different orientation cannot intersect at same r,c
                if (w.row == newWall.row && w.col == newWall.col) return false
            }
        }

        // 3. Path Rule
        val testWalls = existingWalls + newWall
        // P1 must reach row 0
        if (!Pathfinder.hasPath(p1Pos, P1_GOAL_ROW, testWalls)) return false
        // P2 must reach row 8
        if (!Pathfinder.hasPath(p2Pos, P2_GOAL_ROW, testWalls)) return false

        return true
    }
    fun getValidMoves(state: GameState): List<Position> {
        val player = state.currentPlayer
        val currentPos = state.getPlayerPos(player)
        val opponentPos = state.getPlayerPos(player.other())
        val validMoves = mutableListOf<Position>()

        // Check orthogonal neighbors
        val offsets = listOf(
            Position(-1, 0), Position(1, 0), 
            Position(0, -1), Position(0, 1)
        )

        for (offset in offsets) {
            val target = currentPos.offset(offset.row, offset.col)
            if (canMove(player, currentPos, target, state.walls, opponentPos)) {
                validMoves.add(target)
            }
        }
        
        return validMoves
    }
}
