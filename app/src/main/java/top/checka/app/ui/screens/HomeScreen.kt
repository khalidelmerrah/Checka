package top.checka.app.ui.screens

import androidx.compose.ui.res.stringResource
import top.checka.app.R
import top.checka.app.ui.components.ProfileSection
import androidx.compose.ui.platform.LocalContext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import top.checka.app.domain.Difficulty
import top.checka.app.domain.GameMode
import top.checka.app.ui.components.TutorialDialog

import top.checka.app.ui.components.RightModalDrawer
import top.checka.app.ui.components.ProfileSection
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import com.google.android.gms.games.Player

@Composable
fun HomeScreen(
    isAuthenticated: Boolean,
    currentPlayer: Player?,
    userStats: top.checka.app.data.UserStats?, // New Param
    onSignIn: () -> Unit,
    onUpdateProfile: (String?, String?) -> Unit,
    onStartGame: (GameMode, Difficulty, String, String) -> Unit
) {
    var showPassPlayDialog by remember { mutableStateOf(false) }
    var showSoloDialog by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    var isDrawerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }
    
    RightModalDrawer(
        isOpen = isDrawerOpen,
        onClose = { isDrawerOpen = false },
        drawerContent = {
            Column {
                ProfileSection(
                    player = currentPlayer,
                    userStats = userStats,
                    isAuthenticated = isAuthenticated, 
                    onSignIn = onSignIn,
                    onUpdateProfile = onUpdateProfile
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                // Navigation items moved to home screen
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Top Right Icons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(), 
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.IconButton(onClick = { isDrawerOpen = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "Menu",
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
                            top.checka.app.ui.theme.Terracotta,
                            top.checka.app.ui.theme.Terracotta.copy(alpha = 0.6f)
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

                // Bottom Buttons (Menu List)
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn(),
                    modifier = Modifier
                        .align(Alignment.Center) // Move to center/bottom area
                        .padding(top = 200.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HomeMenuCard(
                            title = stringResource(R.string.pass_and_play),
                            subtitle = "2 players, one device",
                            icon = Icons.Default.Group,
                            iconTint = Color(0xFFFF7043), // Orange-ish
                            onClick = { showPassPlayDialog = true }
                        )

                        HomeMenuCard(
                            title = stringResource(R.string.solo_vs_ai),
                            subtitle = "Challenge the computer",
                            icon = Icons.Default.SmartToy,
                            iconTint = Color(0xFF66BB6A), // Green-ish
                            onClick = { showSoloDialog = true }
                        )

                        HomeMenuCard(
                            title = "Ranked Play (Online)",
                            subtitle = "Compete for World Rank",
                            icon = Icons.Default.EmojiEvents,
                            iconTint = Color(0xFFE91E63), // Pink/Magenta for "Special/Ranked"
                            onClick = { 
                                if (isAuthenticated) {
                                    val p1Name = currentPlayer?.displayName ?: "Player 1"
                                    onStartGame(GameMode.Ranked, Difficulty.Hard, p1Name, "Searching...") // Will be updated by match found logic later
                                } else {
                                    onSignIn()
                                }
                            }
                        )

                    }
                }
            }
        }
    )



    if (showPassPlayDialog) {
        GameSetupDialog(
            title = stringResource(R.string.pass_and_play),
            isSolo = false,
            currentPlayer = currentPlayer,
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
            currentPlayer = currentPlayer,
            onDismiss = { showSoloDialog = false },
            onStart = { p1, p2, diff ->
                onStartGame(GameMode.Solo, diff, p1, p2) 
                showSoloDialog = false
            }
        )
    }
}

@Composable
fun HomeMenuCard(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle != null) 80.dp else 60.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF252525), // Dark Card Background
        border = BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
// Removed old MenuButton and NavigationItem if no longer used, or keep for compatibility if referenced elsewhere (unlikely in this file)


@Composable
fun GameSetupDialog(
    title: String,
    isSolo: Boolean,
    currentPlayer: Player? = null,
    onDismiss: () -> Unit,
    onStart: (String, String, Difficulty) -> Unit
) {
    val defaultP1 = currentPlayer?.displayName ?: stringResource(R.string.player_1_default)
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
