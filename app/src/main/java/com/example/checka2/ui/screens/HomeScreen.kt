package com.example.checka2.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.checka2.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.checka2.domain.Difficulty
import com.example.checka2.domain.GameMode

@Composable
fun HomeScreen(
    onStartGame: (GameMode, Difficulty, String, String) -> Unit,
    onNavigateLeaderboard: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    var showPassPlayDialog by remember { mutableStateOf(false) }
    var showSoloDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Right Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            androidx.compose.material3.IconButton(onClick = onNavigateSettings) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            androidx.compose.material3.IconButton(onClick = { showTutorialDialog = true }) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Info, // using Info as Help/Question
                    contentDescription = stringResource(R.string.how_to_play),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Center / Top Visuals (Logo)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val brush = Brush.verticalGradient(
                colors = listOf(
                    com.example.checka2.ui.theme.Terracotta,
                    com.example.checka2.ui.theme.Terracotta.copy(alpha = 0.6f)
                )
            )
            
            Text(
                text = "CHECKA",
                style = MaterialTheme.typography.displayLarge.copy(
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Bold,
                    brush = brush
                ),
            )
            // Placeholder for future illustration
        }

        // Bottom Buttons
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MenuButton(stringResource(R.string.solo_vs_ai), onClick = { showSoloDialog = true })
                MenuButton(stringResource(R.string.pass_and_play), onClick = { showPassPlayDialog = true })
                MenuButton(stringResource(R.string.leaderboard), onClick = { onNavigateLeaderboard() })
            }
        }
    }

    if (showTutorialDialog) {
        com.example.checka2.ui.components.TutorialDialog(
            onDismiss = { showTutorialDialog = false }
        )
    }

    if (showPassPlayDialog) {
        GameSetupDialog(
            title = stringResource(R.string.pass_and_play),
            isSolo = false,
            onDismiss = { showPassPlayDialog = false },
            onStart = { p1, p2, diff ->
                onStartGame(GameMode.PassAndPlay, diff, p1, p2)
                showPassPlayDialog = false
            }
        )
    }
    
    if (showSoloDialog) {
         GameSetupDialog(
            title = stringResource(R.string.solo_vs_ai),
            isSolo = true,
            onDismiss = { showSoloDialog = false },
            onStart = { p1, p2, diff ->
                onStartGame(GameMode.Solo, diff, p1, p2) 
                showSoloDialog = false
            }
        )
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GameSetupDialog(
    title: String,
    isSolo: Boolean,
    onDismiss: () -> Unit,
    onStart: (String, String, Difficulty) -> Unit
) {
    val defaultP1 = stringResource(R.string.player_1_default)
    val defaultP2 = stringResource(R.string.player_2_default)
    var p1Name by remember { mutableStateOf(defaultP1) }
    var p2Name by remember { mutableStateOf(if (isSolo) "Checka AI" else defaultP2) }
    var difficulty by remember { mutableStateOf(Difficulty.Easy) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = p1Name,
                    onValueChange = { p1Name = it },
                    label = { Text(stringResource(R.string.player_1_name_label)) }
                )
                
                if (!isSolo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text(stringResource(R.string.player_2_name_label)) }
                    )
                }

                if (isSolo) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.difficulty))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Difficulty.values().forEach { d ->
                            Button(
                                onClick = { difficulty = d },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (difficulty == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (difficulty == d) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp), // Reduced padding
                                modifier = Modifier.weight(1f).padding(2.dp) // Reduced padding
                            ) {
                                Text(
                                    text = d.name,
                                    style = MaterialTheme.typography.bodySmall, // Smaller text
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onStart(p1Name, p2Name, difficulty) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.start_game))
                }
            }
        }
    }
}
