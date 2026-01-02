package top.checka.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.games.Player
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Edit

@Composable
fun ProfileSection(
    player: Player?,
    userStats: top.checka.app.data.UserStats?,
    isAuthenticated: Boolean,
    onSignIn: () -> Unit,
    onUpdateProfile: (String?, String?) -> Unit // Updated Callback
) {
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onAvatarSelected = { url ->
                onUpdateProfile(url, null)
                showAvatarDialog = false
            }
        )
    }
    
    if (showNameDialog) {
        var name by remember { mutableStateOf(userStats?.username ?: player?.displayName ?: "") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit Display Name") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    label = { Text("Display Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                         onUpdateProfile(null, name)
                         showNameDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAuthenticated && player != null) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userStats?.avatarUrl ?: player.iconImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop, // Or Fit for emojis
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showAvatarDialog = true }
                )
                
                // Edit Icon Badge
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showAvatarDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                        contentDescription = "Edit Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Name Row with Edit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showNameDialog = true }
            ) {
                Text(
                    text = userStats?.username ?: player.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Stats Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 // Level / Stars
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     androidx.compose.material3.Icon(
                         imageVector = androidx.compose.material.icons.Icons.Default.Star,
                         contentDescription = "Level",
                         tint = Color(0xFFFFC107) // Amber
                     )
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                         text = "Lvl ${userStats?.level ?: 1}",
                         style = MaterialTheme.typography.bodyLarge,
                         fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                     )
                 }
                 
                 // Elo
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     androidx.compose.material3.Icon(
                         imageVector = androidx.compose.material.icons.Icons.Default.EmojiEvents,
                         contentDescription = "Elo",
                         tint = Color(0xFFFFA726) // Orange
                     )
                     Spacer(modifier = Modifier.width(4.dp))
                     Text(
                         text = "${userStats?.elo ?: 1200}",
                         style = MaterialTheme.typography.bodyLarge,
                         fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                     )
                 }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // XP Text
            Text(
                text = "${userStats?.xp ?: 0} XP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

        } else {
            Box(
                 modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                 contentAlignment = Alignment.Center
            ) {
                 Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                 )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onSignIn) {
                Text("Sign In using Google")
            }
        }
    }
}
