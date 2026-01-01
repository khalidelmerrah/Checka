package com.example.checka2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import com.example.checka2.domain.GameState
import com.example.checka2.domain.Orientation
import com.example.checka2.domain.Player
import com.example.checka2.domain.Position
import com.example.checka2.domain.Wall
import com.example.checka2.ui.theme.Sage
import com.example.checka2.ui.theme.Stone800
import com.example.checka2.ui.theme.Terracotta
import com.example.checka2.ui.theme.WarmGray
import com.example.checka2.ui.theme.WarmGrayLight
import kotlin.math.round

@Composable
fun CheckaBoard(
    state: GameState,
    isPlaceWallMode: Boolean,
    wallOrientation: Orientation,
    previewWall: Wall?,
    validMoves: List<Position>, // Add this parameter
    onCellClick: (Position) -> Unit,
    onIntersectionClick: (Int, Int) -> Unit, 
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .background(WarmGray, RoundedCornerShape(8.dp)) 
            .padding(4.dp) 
    ) {
        val boardSize = maxWidth
        
        // 1. Grid of Cells
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 9) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until 9) {
                        val pos = Position(r, c)
                        val isValid = !isPlaceWallMode && (pos in validMoves)
                        BoardCell(
                            pos = pos,
                            isValidMoveTarget = isValid,
                            onClick = { if (isValid || !isPlaceWallMode) onCellClick(pos) }, // Let VM filter invalid clicks if needed, or strictly allow valid? User said "Tapping anywhere else does nothing". VM filters it.
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Walls Layer (Canvas)
        val wallColor = Stone800 
        val previewColor = Stone800.copy(alpha = 0.5f) // As requested "alpha-reduced versions"
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / 9f
            val cellH = size.height / 9f
            val wallThickness = 8.dp.toPx() // Fixed 8.dp
            
            fun drawGameWall(wall: Wall, color: Color) {
                val cx = (wall.col + 1) * cellW
                val cy = (wall.row + 1) * cellH
                
                if (wall.orientation == Orientation.Horizontal) {
                    val left = cx - cellW + (cellW * 0.1f) 
                    val top = cy - wallThickness / 2
                    val wLen = (cellW * 2) - (cellW * 0.2f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(wLen, wallThickness),
                        cornerRadius = CornerRadius(wallThickness/2)
                    )
                } else {
                    val left = cx - wallThickness / 2
                    val top = cy - cellH + (cellH * 0.1f)
                    val hLen = (cellH * 2) - (cellH * 0.2f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(wallThickness, hLen),
                        cornerRadius = CornerRadius(wallThickness/2)
                    )
                }
            }

            // Existing Walls
            state.walls.forEach { wall ->
                drawGameWall(wall, wallColor)
            }

            // Preview Wall
            if (isPlaceWallMode && previewWall != null) {
                drawGameWall(previewWall, previewColor)
            }
        }

        // 3. Pawns Layer (Animated)
        PawnsOverlay(
            p1Pos = state.p1Pos,
            p2Pos = state.p2Pos,
            boardSize = boardSize
        )

        // 4. Intersections Layer
        InteractionOverlay(
            isPlaceWallMode = isPlaceWallMode,
            onIntersectionClick = onIntersectionClick
        )
    }
}



@Composable
fun BoardCell(
    pos: Position,
    isValidMoveTarget: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isValidMoveTarget) 0.6f else 0f,
        label = "highlight"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, Color(0xFFD4CDC5)) 
            .background(WarmGrayLight) 
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
         if (isValidMoveTarget) {
             // Soft glowing dot
             Box(
                 modifier = Modifier
                     .size(12.dp)
                     .clip(CircleShape)
                     .background(MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim))
                     .shadow(4.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary)
             )
         }
    }
}

@Composable
fun PawnsOverlay(
    p1Pos: Position,
    p2Pos: Position,
    boardSize: Dp
) {
    val cellSize = boardSize / 9f
    
    // Animate P1
    val p1Row by animateDpAsState(targetValue = cellSize * p1Pos.row, label = "p1RowAnimation")
    val p1Col by animateDpAsState(targetValue = cellSize * p1Pos.col, label = "p1ColAnimation")
    
    // Animate P2
    val p2Row by animateDpAsState(targetValue = cellSize * p2Pos.row, label = "p2RowAnimation")
    val p2Col by animateDpAsState(targetValue = cellSize * p2Pos.col, label = "p2ColAnimation")

    Box(modifier = Modifier.fillMaxSize()) {
        val pawnSize = cellSize * 0.7f // 70% size
        val offset = (cellSize - pawnSize) / 2 // Center it

        // P1 Pawn
        Pawn(
            player = Player.P1, 
            modifier = Modifier
                .offset(x = p1Col + offset, y = p1Row + offset)
                .size(pawnSize)
        )
        
        // P2 Pawn
        Pawn(
            player = Player.P2, 
            modifier = Modifier
                .offset(x = p2Col + offset, y = p2Row + offset)
                .size(pawnSize)
        )
    }
}

@Composable
fun Pawn(player: Player, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .shadow(4.dp, CircleShape)
            .background(if (player == Player.P1) Terracotta else Sage, CircleShape)
            .border(4.dp, Color.White, CircleShape) // 4dp White Ring
    )
}

@Composable
fun InteractionOverlay(
    isPlaceWallMode: Boolean,
    onIntersectionClick: (Int, Int) -> Unit
) {
    if (!isPlaceWallMode) return
    
    val touchRadius = 16.dp // 32dp diameter
    
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
         // Use density for pixel calculation
         val density = androidx.compose.ui.platform.LocalDensity.current
         
         val cellW = maxWidth / 9f
         val cellH = maxHeight / 9f
         
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
                           val centerX = (col + 1) * cellW.toPx()
                           val centerY = (row + 1) * cellH.toPx()
                           val dist = (offset - Offset(centerX, centerY)).getDistance()
                           
                           val radiusPx = with(density) { touchRadius.toPx() }
                           
                           if (dist <= radiusPx) { // Strict 32dp target
                               onIntersectionClick(row, col)
                           }
                       }
                   }
                }
        )
    }
}
