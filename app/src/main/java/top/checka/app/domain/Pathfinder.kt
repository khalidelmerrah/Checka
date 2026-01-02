package top.checka.app.domain

import java.util.LinkedList
import java.util.Queue
import kotlin.math.abs

object Pathfinder {

    // Board size
    private const val ROWS = 9
    private const val COLS = 9

    /**
     * Checks if a path exists for the given player/start position to their goal row.
     * Opponent position is NOT treated as an obstacle for connectivity (rule).
     */
    fun hasPath(start: Position, goalRow: Int, walls: List<Wall>): Boolean {
        // Optimization: Use a 9x9 boolean array for visited
        val visited = Array(ROWS) { BooleanArray(COLS) }
        val queue: Queue<Position> = LinkedList()

        queue.add(start)
        visited[start.row][start.col] = true

        while (queue.isNotEmpty()) {
            val current = queue.poll()!!
            if (current.row == goalRow) return true

            // Explore neighbors
            val neighbors = getNeighbors(current, walls)
            for (next in neighbors) {
                if (!visited[next.row][next.col]) {
                    visited[next.row][next.col] = true
                    queue.add(next)
                }
            }
        }
        return false
    }

    /**
     * Gets path length using BFS. Returns -1 if no path.
     * Useful for AI evaluation.
     */
    fun getShortestPathLength(start: Position, goalRow: Int, walls: List<Wall>): Int {
        val visited = Array(ROWS) { BooleanArray(COLS) }
        val dist = Array(ROWS) { IntArray(COLS) { -1 } }
        val queue: Queue<Position> = LinkedList()

        queue.add(start)
        visited[start.row][start.col] = true
        dist[start.row][start.col] = 0

        while (queue.isNotEmpty()) {
            val current = queue.poll()!!
            if (current.row == goalRow) return dist[current.row][current.col]

            for (next in getNeighbors(current, walls)) {
                if (!visited[next.row][next.col]) {
                    visited[next.row][next.col] = true
                    dist[next.row][next.col] = dist[current.row][current.col] + 1
                    queue.add(next)
                }
            }
        }
        return -1
    }

    private fun getNeighbors(pos: Position, walls: List<Wall>): List<Position> {
        val list = ArrayList<Position>(4)
        val (r, c) = pos

        // Up
        if (r > 0 && !isBlocked(pos, Position(r - 1, c), walls)) list.add(Position(r - 1, c))
        // Down
        if (r < ROWS - 1 && !isBlocked(pos, Position(r + 1, c), walls)) list.add(Position(r + 1, c))
        // Left
        if (c > 0 && !isBlocked(pos, Position(r, c - 1), walls)) list.add(Position(r, c - 1))
        // Right
        if (c < COLS - 1 && !isBlocked(pos, Position(r, c + 1), walls)) list.add(Position(r, c + 1))

        return list
    }


    // Helper to check if movement between adjacent cells is blocked by a wall
    fun isBlocked(from: Position, to: Position, walls: List<Wall>): Boolean {
        if (walls.isEmpty()) return false

        // Vertical Movement
        if (from.col == to.col) {
            val rMin = minOf(from.row, to.row)
            return walls.any { w ->
                w.orientation == Orientation.Horizontal && 
                w.row == rMin && 
                (w.col == from.col || w.col == from.col - 1)
            }
        }
        
        // Horizontal Movement
        if (from.row == to.row) {
            val cMin = minOf(from.col, to.col)
            return walls.any { w ->
                w.orientation == Orientation.Vertical && 
                w.col == cMin && 
                (w.row == from.row || w.row == from.row - 1)
            }
        }

        return false
    }
    
    fun getNextMoveOnShortestPath(start: Position, goalRow: Int, walls: List<Wall>, opponentPos: Position? = null): Position? {
        // BFS to find all distances
        val dist = Array(ROWS) { IntArray(COLS) { Int.MAX_VALUE } }
        val queue: Queue<Position> = LinkedList()
        
        // Start BFS from GOAL ROW upwards to find distance map TO goal?
        // Actually, normally we BFS from start.
        // To find "next step", we can BFS to goal, then backward backtrack? 
        // Or BFS from all goal cells backwards to start.
        // Let's do BFS from all goal cells (row == goalRow).
        
        for (c in 0 until COLS) {
            dist[goalRow][c] = 0
            queue.add(Position(goalRow, c))
        }
        
        while (queue.isNotEmpty()) {
            val current = queue.poll()!!
            
            // If we reached start, we have distances.
            // But we want full map to pick best neighbor.
            
            for (next in getNeighbors(current, walls)) {
                if (dist[next.row][next.col] == Int.MAX_VALUE) {
                    dist[next.row][next.col] = dist[current.row][current.col] + 1
                    queue.add(next)
                }
            }
        }
        
        val currentDist = dist[start.row][start.col]
        if (currentDist == Int.MAX_VALUE) return null // No path
        
        // Find neighbor with dist == currentDist - 1
        val neighbors = getNeighbors(start, walls)
        // Filter out opponent pos if provided (cannot move there)
        val validNeighbors = if (opponentPos != null) neighbors.filter { it != opponentPos } else neighbors
        
        return validNeighbors.minByOrNull { dist[it.row][it.col] }
    }
}
