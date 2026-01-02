package top.checka.app.ui.screens

import androidx.compose.ui.res.stringResource
import top.checka.app.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import androidx.lifecycle.viewmodel.compose.viewModel
import top.checka.app.domain.Difficulty
import top.checka.app.domain.GameMode
import top.checka.app.domain.Player
import top.checka.app.domain.GameState
import top.checka.app.ui.GameViewModel
import top.checka.app.ui.components.CheckaBoard
import top.checka.app.ui.components.GameControls
import top.checka.app.ui.components.PlayerPanel
import top.checka.app.ui.components.TutorialDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

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
    
    // Dynamic Names & Avatars
    val p1Name by viewModel.p1NameState.collectAsState()
    val p2Name by viewModel.p2NameState.collectAsState()
    val p1Avatar by viewModel.p1Avatar.collectAsState()
    val p2Avatar by viewModel.p2Avatar.collectAsState()
    val opponentData by viewModel.opponentData.collectAsState()

    val isSearching by viewModel.isSearching.collectAsState()

    val isPassAndPlay = (gameMode == GameMode.PassAndPlay)
    
    val showTutorial by viewModel.showTutorial.collectAsState()
    val validMoves by viewModel.validMoves.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showP2Profile by remember { mutableStateOf(false) } // P2 Dialog

    androidx.activity.compose.BackHandler {
        if (state.winner == null) {
            showExitDialog = true
        } else {
            onExit()
        }
    }
    
    if (showP2Profile && opponentData != null) {
        top.checka.app.ui.components.PlayerProfileDialog(
            data = top.checka.app.ui.components.PlayerProfileData(
                name = opponentData!!.name,
                avatarUrl = opponentData!!.avatarUrl,
                elo = opponentData!!.elo,
                rankTitle = opponentData!!.title ?: "Beginner",
                level = opponentData!!.level, 
                winRate = opponentData!!.winRate ?: "-", 
                totalGames = opponentData!!.totalGames
            ),
            onDismiss = { showP2Profile = false }
        )
    }
    
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_game_title)) },
            text = { Text(stringResource(R.string.exit_game_confirmation)) },
            confirmButton = {
                Button(onClick = { showExitDialog = false; onExit() }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitDialog = false }) { Text(stringResource(R.string.no)) }
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // RANKED HEADER
            if (gameMode == GameMode.Ranked) {
                 Box(Modifier.fillMaxWidth().height(60.dp).background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF880E4F), Color(0xFFC2185B)))).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(androidx.compose.material.icons.Icons.Default.Star, contentDescription = null, tint = Color.Yellow)
                         Spacer(Modifier.width(8.dp))
                         Text("RANKED MATCH", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                         Spacer(Modifier.width(8.dp))
                         Icon(androidx.compose.material.icons.Icons.Default.Star, contentDescription = null, tint = Color.Yellow)
                     }
                 }
            } else {
                 Box(Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                     Text("Ad Banner (Placeholder)", style = MaterialTheme.typography.labelSmall)
                 }
            }

            // TOP AREA (P2)
            Box(
                modifier = Modifier.weight(1f).fillMaxSize().let { if (isPassAndPlay) it.rotate(180f) else it }, 
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isPassAndPlay && state.currentPlayer == Player.P2 && state.winner == null) {
                        GameControls(isPlaceWallMode = isPlaceWallMode, wallsLeft = state.p2WallsLeft, onToggleMode = viewModel::toggleWallMode)
                    }
                    PlayerPanel(
                        player = Player.P2,
                        name = p2Name, 
                        wallsLeft = state.p2WallsLeft,
                        isActive = state.currentPlayer == Player.P2,
                        avatarUrl = p2Avatar,
                        onClick = if (gameMode == GameMode.Ranked && opponentData != null) { { showP2Profile = true } } else null
                    )
                }
            }

            // CENTER BOARD
            Box(modifier = Modifier.weight(2f).padding(16.dp), contentAlignment = Alignment.Center) {
                CheckaBoard(
                    state = state,
                    isPlaceWallMode = isPlaceWallMode,
                    wallOrientation = wallOrientation,
                    previewWall = previewWall,
                    validMoves = validMoves, 
                    onCellClick = viewModel::onMoveSelected,
                    onIntersectionClick = viewModel::onIntersectionSelected,
                    onToggleOrientation = viewModel::toggleOrientation,
                    onConfirmWall = viewModel::confirmWall,
                    p1Avatar = p1Avatar,
                    p2Avatar = p2Avatar,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (isSearching) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                             Spacer(modifier = Modifier.height(16.dp))
                             Text(text = stringResource(R.string.searching_opponent), style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
                        }
                    }
                }
            }

            // BOTTOM AREA (P1)
            Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerPanel(
                        player = Player.P1,
                        name = p1Name,
                        wallsLeft = state.p1WallsLeft,
                        isActive = state.currentPlayer == Player.P1,
                        avatarUrl = p1Avatar,
                        onClick = null // P1 (Self) profile view? Maybe later.
                    )
                    
                    if ((state.currentPlayer == Player.P1 || !isPassAndPlay) && state.winner == null) {
                         if (state.currentPlayer == Player.P1) {
                            GameControls(isPlaceWallMode = isPlaceWallMode, wallsLeft = state.p1WallsLeft, onToggleMode = viewModel::toggleWallMode)
                        } else {
                            Text(stringResource(R.string.ai_thinking), modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(50.dp).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Text("Ad Banner (Placeholder)", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        if (state.winner != null) {
            AlertDialog(
                onDismissRequest = {}, 
                title = { Text(text = stringResource(R.string.game_over)) },
                text = { Text(text = stringResource(R.string.wins, if (state.winner == Player.P1) p1Name else p2Name)) },
                confirmButton = {
                    Button(onClick = onExit) { Text(stringResource(R.string.back_to_home)) }
                }
            )
        }
        if (showTutorial) {
            TutorialDialog(onDismiss = viewModel::onTutorialDismissed)
        }
    }
}
