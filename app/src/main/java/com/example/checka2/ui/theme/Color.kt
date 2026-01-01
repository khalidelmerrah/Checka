package com.example.checka2.ui.theme

import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFAF7F2)       // Background
val Terracotta = Color(0xFFE07856)  // Player 1
val Sage = Color(0xFF7B9E89)        // Player 2
val WarmGray = Color(0xFFE8E3DC)    // Board
val WarmGrayLight = Color(0xFFF4F1ED) // Cell
val Stone800 = Color(0xFF292524)    // Text/Accents

// Aliases for semantic usage
val CheckaBackground = Cream
val Player1Color = Terracotta
val Player2Color = Sage
val BoardColor = WarmGray
val CellColor = WarmGrayLight
val PrimaryText = Stone800

// Functional Colors
val ValidMoveHighlight = Sage.copy(alpha = 0.5f)
val InvalidMoveHighlight = Terracotta.copy(alpha = 0.5f)
