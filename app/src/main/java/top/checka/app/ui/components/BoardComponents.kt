package top.checka.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.checka.app.domain.Player
import top.checka.app.domain.Orientation
import top.checka.app.domain.Position
import top.checka.app.domain.GameState
import top.checka.app.domain.Wall
import top.checka.app.ui.theme.Sage
import top.checka.app.ui.theme.Stone800
import top.checka.app.ui.theme.Terracotta
import top.checka.app.ui.theme.WarmGray
import top.checka.app.ui.theme.WarmGrayLight
import androidx.compose.animation.core.animateDpAsState
import kotlin.math.round

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Check

@Composable
fun CheckaBoard(
    state: GameState,
    isPlaceWallMode: Boolean,
    wallOrientation: Orientation,
    previewWall: Wall?,
    validMoves: List<Position>, 
    onCellClick: (Position) -> Unit,
    onIntersectionClick: (Int, Int) -> Unit,
    onToggleOrientation: () -> Unit, 
    onConfirmWall: () -> Unit,
    p1Avatar: String? = null, // New
    p2Avatar: String? = null, // New
    modifier: Modifier = Modifier
) {
    // Theme Colors
    val boardBase = Color(0xFF1A1A1A) // Dark Gray/Black base
    val cellColor = Color(0xFF262626) // Slightly lighter for cells (Raised look)
    
    val wallWoodColor = Color(0xFFD28C45) // Warm Wood
    val wallPreviewColor = wallWoodColor.copy(alpha = 0.5f)
    
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(12.dp, RoundedCornerShape(12.dp))
            .background(boardBase, RoundedCornerShape(12.dp)) 
            .padding(8.dp) // Outer rim
    ) {
        val boardSize = maxWidth
        
        // 1. Grid of Cells (Raised Squares)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween 
        ) {
            for (r in 0 until 9) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in 0 until 9) {
                        val pos = Position(r, c)
                        val isValid = !isPlaceWallMode && (pos in validMoves)
                        
                        // Custom Raised Cell Logic
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(1.dp) // The "Groove"
                                .background(cellColor, RoundedCornerShape(4.dp))
                                .clickable(onClick = { if (isValid || !isPlaceWallMode) onCellClick(pos) }),
                            contentAlignment = Alignment.Center
                        ) {
                             // Inner bevel/highlight logic could go here, for now simple color
                             if (isValid) {
                                 Box(
                                     modifier = Modifier
                                         .size(12.dp)
                                         .clip(CircleShape)
                                         .background(Color(0xFF4CAF50).copy(alpha=0.6f)) // Green glow for valid moves
                                         .shadow(4.dp, CircleShape)
                                 )
                             }
                        }
                    }
                }
            }
        }

        // 2. Walls Layer (Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / 9f
            val cellH = size.height / 9f
            // Walls sit in the grooves (padding gaps)
            // A wall spans 2 cells + 1 groove.
            // Width approx = 2 * cellW. 
            // Groove is 2.dp padding * 2 = 4dp approx? 
            // Let's stick to fractional coordinates for 2D.
            
            val thick = 10.dp.toPx()
            
            fun drawWoodWall(wall: Wall, color: Color) {
                val cx = (wall.col + 1) * cellW
                val cy = (wall.row + 1) * cellH
                
                // Draw drop shadow
                val shadowOffset = 4f
                val shadowColor = Color.Black.copy(alpha=0.5f)
                
                if (wall.orientation == Orientation.Horizontal) {
                    val wLen = cellW * 2
                    val left = cx - cellW + (cellW * 0.1f) // slight gap from edge
                    val realLen = wLen - (cellW * 0.2f)
                    val top = cy - thick / 2
                    
                    // Shadow
                    drawRoundRect(shadowColor, topLeft = Offset(left + shadowOffset, top + shadowOffset), size = Size(realLen, thick), cornerRadius = CornerRadius(4f))
                    // Wood
                    drawRoundRect(color, topLeft = Offset(left, top), size = Size(realLen, thick), cornerRadius = CornerRadius(4f))
                    
                } else {
                    val hLen = cellH * 2
                    val top = cy - cellH + (cellH * 0.1f)
                    val realLen = hLen - (cellH * 0.2f)
                    val left = cx - thick / 2
                    
                    // Shadow
                    drawRoundRect(shadowColor, topLeft = Offset(left + shadowOffset, top + shadowOffset), size = Size(thick, realLen), cornerRadius = CornerRadius(4f))
                    // Wood
                    drawRoundRect(color, topLeft = Offset(left, top), size = Size(thick, realLen), cornerRadius = CornerRadius(4f))
                }
            }

            // Existing Walls
            state.walls.forEach { wall ->
                drawWoodWall(wall, wallWoodColor)
            }

            // Preview Wall
            if (isPlaceWallMode && previewWall != null) {
                drawWoodWall(previewWall, wallPreviewColor)
            }
        }

        // 3. Pawns Layer (Animated)
        PawnsOverlay(
            p1Pos = state.p1Pos,
            p2Pos = state.p2Pos,
            p1Avatar = p1Avatar,
            p2Avatar = p2Avatar,
            boardSize = boardSize
        )

        // 4. Intersections Layer
        InteractionOverlay(
            isPlaceWallMode = isPlaceWallMode,
            onIntersectionClick = onIntersectionClick
        )

        // 5. On-Board Controls Overlay (New)
        if (isPlaceWallMode && previewWall != null) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val cellW = maxWidth / 9f
            val cellH = maxHeight / 9f
            
            // Wall Center coordinates relative to TopStart
            val cx = cellW * (previewWall.col + 1)
            val cy = cellH * (previewWall.row + 1)
            
            // Offsets for buttons (e.g., 50.dp away from center)
            val offsetDist = 50.dp
            
            // Layout buttons
            Box(modifier = Modifier.fillMaxSize()) {
                 // Rotate Button (Left of wall)
                 androidx.compose.material3.SmallFloatingActionButton(
                     onClick = onToggleOrientation,
                     modifier = Modifier
                         .offset(
                             x = cx - offsetDist - 24.dp, // Center the button (size ~48dp)
                             y = cy - 24.dp
                         ),
                     containerColor = MaterialTheme.colorScheme.secondaryContainer,
                     contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                 ) {
                     androidx.compose.material3.Icon(
                         imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                         contentDescription = "Rotate"
                     )
                 }

                 // Confirm Button (Right of wall)
                 androidx.compose.material3.SmallFloatingActionButton(
                     onClick = onConfirmWall,
                     modifier = Modifier
                         .offset(
                             x = cx + offsetDist - 24.dp,
                             y = cy - 24.dp
                         ),
                     containerColor = Color(0xFF4CAF50),
                     contentColor = Color.White
                 ) {
                     androidx.compose.material3.Icon(
                         imageVector = androidx.compose.material.icons.Icons.Default.Check,
                         contentDescription = "Confirm"
                     )
                 }
            }
        }
    }
}


@Composable
fun BoardCell(
    pos: Position,
    isValidMoveTarget: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Deprecated? Merged into main Loop for tighter layout control
}

@Composable
fun PawnsOverlay(
    p1Pos: Position,
    p2Pos: Position,
    p1Avatar: String?,
    p2Avatar: String?,
    boardSize: Dp
) {
    val cellSize = boardSize / 9f
    
    val p1Row by animateDpAsState(targetValue = cellSize * p1Pos.row, label = "p1Row")
    val p1Col by animateDpAsState(targetValue = cellSize * p1Pos.col, label = "p1Col")
    val p2Row by animateDpAsState(targetValue = cellSize * p2Pos.row, label = "p2Row")
    val p2Col by animateDpAsState(targetValue = cellSize * p2Pos.col, label = "p2Col")

    Box(modifier = Modifier.fillMaxSize()) {
        val pawnSize = cellSize * 0.75f // Slightly larger for avatars
        val offset = (cellSize - pawnSize) / 2 

        // P1 Pawn
        Pawn(
            color = Color(0xFFC62828),
            avatarUrl = p1Avatar,
            modifier = Modifier.offset(x = p1Col + offset, y = p1Row + offset).size(pawnSize)
        )
        // P2 Pawn
        Pawn(
            color = Color(0xFFFBE9E7),
            avatarUrl = p2Avatar,
            modifier = Modifier.offset(x = p2Col + offset, y = p2Row + offset).size(pawnSize)
        )
    }
}

@Composable
fun Pawn(color: Color, avatarUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(6.dp, CircleShape, spotColor = Color.Black)
            .background(color, CircleShape)
            .border(2.dp, Color.White, CircleShape) // White border for avatar pop
    ) {
        if (avatarUrl != null) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Pawn Avatar",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit // Fit entire emoji
            )
        } else {
             // Fallback Styling
             Box(Modifier.size(10.dp).align(Alignment.TopStart).offset(4.dp, 4.dp).background(Color.White.copy(alpha=0.3f), CircleShape))
        }
    }
}

@Composable
fun InteractionOverlay(
    isPlaceWallMode: Boolean,
    onIntersectionClick: (Int, Int) -> Unit
) {
    if (!isPlaceWallMode) return
    /* Same logic, just transparent layer on top */
    val touchRadius = 24.dp // Larger touch target
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
         val cellW = maxWidth / 9f
         val cellH = maxHeight / 9f
         val density = androidx.compose.ui.platform.LocalDensity.current
         
         Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                   detectTapGestures { offset ->
                       val colFloat = (offset.x / cellW.toPx()) - 1
                       val rowFloat = (offset.y / cellH.toPx()) - 1
                       val col = round(colFloat).toInt()
                       val row = round(rowFloat).toInt()
                       if (col in 0..7 && row in 0..7) {
                           onIntersectionClick(row, col)
                       }
                   }
                }
        )
    }
}
