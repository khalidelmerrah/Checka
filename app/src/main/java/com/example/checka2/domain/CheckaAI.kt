package com.example.checka2.domain

object CheckaAI {

    fun chooseAction(state: GameState, difficulty: Difficulty): GameAction {
        val player = state.currentPlayer
        val opponent = player.other()
        val myPos = state.getPlayerPos(player)
        val oppPos = state.getPlayerPos(opponent)
        val myGoal = if (player == Player.P1) GameEngine.P1_GOAL_ROW else GameEngine.P2_GOAL_ROW
        val oppGoal = if (opponent == Player.P1) GameEngine.P1_GOAL_ROW else GameEngine.P2_GOAL_ROW

        // Logic split by difficulty
        return when (difficulty) {
            Difficulty.Easy -> chooseEasy(state, myPos, myGoal, state.walls, oppPos)
            Difficulty.Hard -> chooseHard(state, myPos, myGoal, oppGoal, state.walls, oppPos)
            Difficulty.Master -> chooseMaster(state, myPos, myGoal, oppGoal, state.walls, oppPos)
        }
    }

    private fun chooseEasy(
        state: GameState,
        myPos: Position,
        myGoalRow: Int,
        walls: List<Wall>,
        oppPos: Position
    ): GameAction {
        // Mostly moves toward goal
        // 10% chance to put a random wall if walls left > 0
        val wallsLeft = state.getWallsLeft(state.currentPlayer)
        if (wallsLeft > 0 && Math.random() < 0.1) {
            val randomWall = tryFindRandomWall(state)
            if (randomWall != null) return GameAction.PlaceWall(randomWall)
        }
        
        // Otherwise move
        val nextStep = Pathfinder.getNextMoveOnShortestPath(myPos, myGoalRow, walls, oppPos)
        return if (nextStep != null) {
            GameAction.Move(nextStep)
        } else {
            // Stuck? Should not happen if path exists.
            // Move random legal
            GameAction.Move(myPos) // no-op if stuck, essentially skip turn or crash, but rule says path always exists
        }
    }

    private fun chooseHard(
        state: GameState,
        myPos: Position,
        myGoalRow: Int,
        oppGoalRow: Int,
        walls: List<Wall>,
        oppPos: Position
    ): GameAction {
        val wallsLeft = state.getWallsLeft(state.currentPlayer)
        
        // 30% chance to consider wall placement to block opponent
        if (wallsLeft > 0 && Math.random() < 0.3) {
            val bestWall = findBestWallToDelayOpponent(state, oppPos, oppGoalRow, myPos, myGoalRow)
            if (bestWall != null) return GameAction.PlaceWall(bestWall)
        }

        // Otherwise move
        val nextStep = Pathfinder.getNextMoveOnShortestPath(myPos, myGoalRow, walls, oppPos)
        return if (nextStep != null) GameAction.Move(nextStep) else GameAction.Move(myPos)
    }

    private fun chooseMaster(
        state: GameState,
        myPos: Position,
        myGoalRow: Int,
        oppGoalRow: Int,
        walls: List<Wall>,
        oppPos: Position
    ): GameAction {
        val wallsLeft = state.getWallsLeft(state.currentPlayer)

        if (wallsLeft > 0) {
            // Evaluates all legal wall placements
            // Choose one that increases opponent shortest path most
            // Tie-break: keep AI shortest path minimal (or unchanged)
            
            val initialOppPathLen = Pathfinder.getShortestPathLength(oppPos, oppGoalRow, walls)
            val initialMyPathLen = Pathfinder.getShortestPathLength(myPos, myGoalRow, walls)
            
            // Heuristic: OppPathLen - MyPathLen. Maximize this.
            
            var bestWall: Wall? = null
            var maxAdvantage = initialOppPathLen - initialMyPathLen
            
            // Iterate all possible walls
            // Optimization: Only check walls near opponent? Or All?
            // "Evaluates all legal wall placements" -> All 128 positions.
            
            for (r in 0..7) {
                for (c in 0..7) {
                    for (o in Orientation.values()) {
                        val w = Wall(r, c, o)
                        if (GameEngine.isValidWallPlacement(w, walls, state.p1Pos, state.p2Pos)) {
                            val newWalls = walls + w
                            val newOppLen = Pathfinder.getShortestPathLength(oppPos, oppGoalRow, newWalls)
                            val newMyLen = Pathfinder.getShortestPathLength(myPos, myGoalRow, newWalls)
                            
                            val advantage = newOppLen - newMyLen
                            if (advantage > maxAdvantage) {
                                maxAdvantage = advantage
                                bestWall = w
                            }
                        }
                    }
                }
            }
            
            // If improving advantage significantly (e.g. slowing them down more than use), place wall
            // Strategy: Checka reference says "Move unless wall increases human path".
            // If bestWall increases opponent path length > current, use it.
            // But we should prioritize moving if we are winning?
            // "chooses the one that increases the human shortest path most"
            // "Otherwise moves along its own shortest path"
            
            if (bestWall != null && maxAdvantage > (initialOppPathLen - initialMyPathLen)) {
                return GameAction.PlaceWall(bestWall)
            }
        }
        
        val nextStep = Pathfinder.getNextMoveOnShortestPath(myPos, myGoalRow, walls, oppPos)
        return if (nextStep != null) GameAction.Move(nextStep) else GameAction.Move(myPos)
    }

    private fun tryFindRandomWall(state: GameState): Wall? {
        // Try decent number of times
        repeat(20) {
            val r = (0..7).random()
            val c = (0..7).random()
            val o = if (Math.random() < 0.5) Orientation.Horizontal else Orientation.Vertical
            val w = Wall(r, c, o)
            if (GameEngine.isValidWallPlacement(w, state.walls, state.p1Pos, state.p2Pos)) {
                return w
            }
        }
        return null
    }

    private fun findBestWallToDelayOpponent(
        state: GameState, 
        oppPos: Position, 
        oppGoalRow: Int, 
        myPos: Position, 
        myGoalRow: Int
    ): Wall? {
        // Simplified Master logic: verify random subset or near opponent
        // Search walls near opponent
        val candidates = mutableListOf<Wall>()
        for (r in (oppPos.row-2)..(oppPos.row+2)) {
            for (c in (oppPos.col-2)..(oppPos.col+2)) {
                if (r in 0..7 && c in 0..7) {
                    candidates.add(Wall(r, c, Orientation.Horizontal))
                    candidates.add(Wall(r, c, Orientation.Vertical))
                }
            }
        }
        
        val initialOppLen = Pathfinder.getShortestPathLength(oppPos, oppGoalRow, state.walls)
        var bestW: Wall? = null
        var maxLen = initialOppLen
        
        for (w in candidates) {
            if (GameEngine.isValidWallPlacement(w, state.walls, state.p1Pos, state.p2Pos)) {
                val newWalls = state.walls + w
                val len = Pathfinder.getShortestPathLength(oppPos, oppGoalRow, newWalls)
                if (len > maxLen) {
                    maxLen = len
                    bestW = w
                }
            }
        }
        return bestW
    }
}
