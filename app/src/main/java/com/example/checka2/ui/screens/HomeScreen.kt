package com.example.checka2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CHECKA",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(48.dp))

        MenuButton("Pass & Play") { showPassPlayDialog = true }
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton("Solo vs AI") { showSoloDialog = true }
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton("Leaderboard") { onNavigateLeaderboard() }
        Spacer(modifier = Modifier.height(16.dp))
        MenuButton("Settings") { onNavigateSettings() }
    }

    if (showPassPlayDialog) {
        GameSetupDialog(
            title = "Pass & Play",
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
            title = "Solo vs AI",
            isSolo = true,
            onDismiss = { showSoloDialog = false },
            onStart = { p1, p2, diff ->
                onStartGame(GameMode.Solo, diff, p1, p2) // p2 is AI name usually
                showSoloDialog = false
            }
        )
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun GameSetupDialog(
    title: String,
    isSolo: Boolean,
    onDismiss: () -> Unit,
    onStart: (String, String, Difficulty) -> Unit
) {
    var p1Name by remember { mutableStateOf("Player 1") }
    var p2Name by remember { mutableStateOf(if (isSolo) "Checka AI" else "Player 2") }
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
                    label = { Text("Player 1 Name") }
                )
                
                if (!isSolo) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text("Player 2 Name") }
                    )
                }

                if (isSolo) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Difficulty")
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
                                modifier = Modifier.weight(1f).padding(4.dp)
                            ) {
                                Text(d.name) // Use substring for short?
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onStart(p1Name, p2Name, difficulty) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Game")
                }
            }
        }
    }
}
