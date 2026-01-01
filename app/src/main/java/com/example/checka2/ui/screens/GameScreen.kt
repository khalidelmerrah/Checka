package com.example.checka2.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.checka2.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.checka2.domain.GameMode
import com.example.checka2.domain.Player
import com.example.checka2.ui.GameViewModel
import com.example.checka2.ui.components.CheckaBoard
import com.example.checka2.ui.components.GameControls
import com.example.checka2.ui.components.PlayerPanel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(),
    gameMode: GameMode, // Passed for layout logic (or read from VM)
    onExit: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val isPlaceWallMode by viewModel.isPlaceWallMode.collectAsState()
    val wallOrientation by viewModel.wallOrientation.collectAsState()
    val previewWall by viewModel.previewWall.collectAsState()

    val isPassAndPlay = (gameMode == GameMode.PassAndPlay)
    
    


    val showTutorial by viewModel.showTutorial.collectAsState()
    val validMoves by viewModel.validMoves.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AD BANNER (Top)
            Box(Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text("Ad Banner (Placeholder)", style = MaterialTheme.typography.labelSmall)
            }

            // TOP AREA (P2)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .let { if (isPassAndPlay) it.rotate(180f) else it }, // Rotate controls ONLY for P2
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Controls if P2 turn & PassAndPlay
                    if (isPassAndPlay && state.currentPlayer == Player.P2 && state.winner == null) {
                        GameControls(
                            isPlaceWallMode = isPlaceWallMode,
                            orientation = wallOrientation,
                            wallsLeft = state.p2WallsLeft,
                            onToggleMode = viewModel::toggleWallMode,
                            onToggleOrientation = viewModel::toggleOrientation
                        )
                    }
                    
                    PlayerPanel(
                        player = Player.P2,
                        name = stringResource(R.string.player_2_default), 
                        wallsLeft = state.p2WallsLeft,
                        isActive = state.currentPlayer == Player.P2
                    )
                }
            }

            // CENTER BOARD
            Box(
                 modifier = Modifier
                     .weight(2f)
                     .padding(16.dp),
                 contentAlignment = Alignment.Center
            ) {
                CheckaBoard(
                    state = state,
                    isPlaceWallMode = isPlaceWallMode,
                    wallOrientation = wallOrientation,
                    previewWall = previewWall,
                    validMoves = validMoves, 
                    onCellClick = viewModel::onMoveSelected,
                    onIntersectionClick = viewModel::onIntersectionSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // BOTTOM AREA (P1)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerPanel(
                        player = Player.P1,
                        name = stringResource(R.string.player_1_default),
                        wallsLeft = state.p1WallsLeft,
                        isActive = state.currentPlayer == Player.P1
                    )
                    
                    // Controls if P1 turn OR Solo Mode
                    if ((state.currentPlayer == Player.P1 || !isPassAndPlay) && state.winner == null) {
                         val enabled = (state.currentPlayer == Player.P1)
                        if (enabled) {
                            GameControls(
                                isPlaceWallMode = isPlaceWallMode,
                                orientation = wallOrientation,
                                wallsLeft = state.p1WallsLeft,
                                onToggleMode = viewModel::toggleWallMode,
                                onToggleOrientation = viewModel::toggleOrientation
                            )
                        } else {
                            Text(stringResource(R.string.ai_thinking), modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            // AD BANNER (Bottom)
            Box(Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text("Ad Banner (Placeholder)", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        // Winner Dialog
        if (state.winner != null) {
            AlertDialog(
                onDismissRequest = {}, 
                title = { Text(text = stringResource(R.string.game_over)) },
                text = { Text(text = stringResource(R.string.wins, if (state.winner == Player.P1) stringResource(R.string.player_1_default) else stringResource(R.string.player_2_default))) },
                confirmButton = {
                    Button(onClick = onExit) {
                        Text(stringResource(R.string.back_to_home))
                    }
                }
            )
        }

        // Tutorial Overlay
        if (showTutorial) {
            com.example.checka2.ui.components.TutorialDialog(
                onDismiss = viewModel::onTutorialDismissed
            )
        }
    }
}
