package top.checka.app.domain

import org.junit.Test
import org.junit.Assert.*
import top.checka.app.domain.Pathfinder
import top.checka.app.domain.Wall
import top.checka.app.domain.Orientation

class PathfinderTest {

    @Test
    fun testHasPath_NoWalls() {
        val start = Position(0, 4)
        val targetRow = 8
        val walls = emptyList<Wall>()
        
        // Should have path from top to bottom
        assertTrue(Pathfinder.hasPath(start, targetRow, walls))
    }

    @Test
    fun testHasPath_SimpleWallBlock() {
        // Place a horizontal wall blocking direct path?
        // Let's block (0,4) moving down.
        // Wall at (0,4) Horizontal blocks movement between (0,4)-(1,4) and (0,5)-(1,5)
        
        val start = Position(0, 4)
        val targetRow = 8
        val walls = listOf(
            Wall(0, 4, Orientation.Horizontal)
        )
        
        // Should still find path around
        assertTrue(Pathfinder.hasPath(start, targetRow, walls))
    }

    @Test
    fun testHasPath_FullBlock() {
        // Construct a full wall across row 0/1 boundary
        // Walls are 2 units wide.
        // To block 9 cols: 0, 2, 4, 6, 8 (partial)
        // Checka walls are Grid coordinate based.
        // Wall(r, c) Horizontal blocks (r, c)|(r+1,c) and (r, c+1)|(r+1, c+1)
        // Wait, logic is: Wall(r,c) H blocks crossing from r to r+1 at col c and c+1.
        
        val walls = mutableListOf<Wall>()
        // Block all columns 0..8 for crossing row 0->1
        // Wall(0,0) H covers cols 0,1
        // Wall(0,2) H covers cols 2,3
        // Wall(0,4) H covers cols 4,5
        // Wall(0,6) H covers cols 6,7
        // Wall(0,7) H covers cols 7,8 ?? No overlap allowed usually.
        // If we place Wall(0,8) it covers 8,9 (9 is OOB).
        
        walls.add(Wall(0, 0, Orientation.Horizontal))
        walls.add(Wall(0, 2, Orientation.Horizontal))
        walls.add(Wall(0, 4, Orientation.Horizontal))
        walls.add(Wall(0, 6, Orientation.Horizontal))
        
        // Cols 0,1,2,3,4,5,6,7 are blocked. Col 8 is open.
        // Path should exist via col 8.
        val start = Position(0, 4)
        assertTrue(Pathfinder.hasPath(start, 8, walls))
        
        // Now block col 8
        // Need a wall that covers col 8. Wall(0,7) covers 7,8?
        // But Wall(0,7) overlaps with Wall(0,6) (6 covers 6,7).
        // Overlap logic in isValidWallPlacement handles valid placement.
        // Here we just test Pathfinder. Pathfinder just checks "isBlocked".
        // Let's simulate a set of walls that we KNOW blocks.
        // Or just surround the player.
        
        val trapWalls = listOf(
            Wall(0, 4, Orientation.Horizontal), // Blocks Down
            Wall(0, 4, Orientation.Vertical),   // Blocks Right (0,4)-(0,5) ? No.
            // Vertical at 0,4 blocks crossing (0,4)-(0,5) and (1,4)-(1,5)
            
            // Surround (0,4)
            // Down: (0,4)->(1,4). Blocked by Wall(0,4) Horz (covers cols 4,5) OR Wall(0,3) Horz (covers 3,4).
            // Let's use Wall(0,3) Horz -> covers 3,4.
            // Wall(0,4) Horz -> covers 4,5.
            Wall(0, 4, Orientation.Horizontal),
            
            // Up: (0,4)->(-1,4) impossible (OOB).
            
            // Left: (0,4)->(0,3). Blocked by Vertical between 3 and 4?
            // Vertical Wall(0,3) blocks (0,3)|(0,4) and (1,3)|(1,4).
            Wall(0, 3, Orientation.Vertical),
            
            // Right: (0,4)->(0,5). Blocked by Vertical between 4 and 5?
            // Vertical Wall(0,4) blocks (0,4)|(0,5) and (1,4)|(1,5).
            Wall(0, 4, Orientation.Vertical)
        )
        
        // If surrounded, should return False
        assertFalse("Should be blocked", Pathfinder.hasPath(start, 8, trapWalls))
    }
}
