package top.checka.app.ui.components

import androidx.compose.ui.res.stringResource
import top.checka.app.R

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.checka.app.domain.Orientation
import top.checka.app.domain.Player

@Composable
fun PlayerPanel(
    player: Player,
    name: String,
    wallsLeft: Int,
    isActive: Boolean,
    avatarUrl: String? = null, // New
    onClick: (() -> Unit)? = null, // New
    modifier: Modifier = Modifier
) {
    val bgColor = animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    )
    val color = if (player == Player.P1) top.checka.app.ui.theme.Terracotta else top.checka.app.ui.theme.Sage
    val wallsColor = if (player == Player.P1) top.checka.app.ui.theme.Terracotta.copy(alpha=0.6f) else top.checka.app.ui.theme.Sage.copy(alpha=0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor.value, RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() } // Clickable main container
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(2.dp, if (isActive) Color.White else Color.Transparent, CircleShape)
            ) {
                 if (avatarUrl != null) {
                     coil.compose.AsyncImage(
                         model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                             .data(avatarUrl)
                             .crossfade(true)
                             .build(),
                         contentDescription = "Avatar",
                         modifier = Modifier.fillMaxSize(),
                         contentScale = androidx.compose.ui.layout.ContentScale.Crop
                     )
                 }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                if (isActive) {
                    Text(
                        text = stringResource(R.string.your_turn),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Walls "Sticks" Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp) // Tighter spacing
        ) {
             val maxWalls = 10
             // Draw placeholders or just the active ones?
             // Reference image shows empty slots. Let's draw slots + filled.
             
             // Just show remaining walls as "Wood" sticks
             val woodColor = Color(0xFFD28C45) // Wood color hardcoded for now, or use theme
             
             for (i in 0 until wallsLeft) {
                 Box(
                     modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .background(woodColor, RoundedCornerShape(1.dp))
                        .border(0.5.dp, Color.Black.copy(alpha=0.3f), RoundedCornerShape(1.dp))
                 )
             }
             // Optional: Show empty slots for used walls?
             for (i in wallsLeft until maxWalls) {
                  Box(
                     modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .background(Color.Black.copy(alpha=0.2f), RoundedCornerShape(1.dp))
                 )
             }
        }
    }
}

@Composable
fun GameControls(
    isPlaceWallMode: Boolean,
    wallsLeft: Int,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Smart Segmented Switch Full Width
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Move Segment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(
                            if (!isPlaceWallMode) top.checka.app.ui.theme.Stone800 else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = { if (isPlaceWallMode) onToggleMode() }),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         // Optional Icon?
                         Text(
                            text = stringResource(R.string.move),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!isPlaceWallMode) top.checka.app.ui.theme.Cream else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Wall Segment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(
                            if (isPlaceWallMode) top.checka.app.ui.theme.Stone800 else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(enabled = wallsLeft > 0, onClick = { if (!isPlaceWallMode) onToggleMode() }),
                    contentAlignment = Alignment.Center
                ) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        // Optional Icon
                        Text(
                            text = stringResource(R.string.place_wall),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaceWallMode) top.checka.app.ui.theme.Cream else if (wallsLeft > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
