package com.example.checka2.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checka2.domain.Orientation
import com.example.checka2.domain.Player

@Composable
fun PlayerPanel(
    player: Player,
    name: String,
    wallsLeft: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = animateColorAsState(
        if (isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    )
    val color = if (player == Player.P1) com.example.checka2.ui.theme.Terracotta else com.example.checka2.ui.theme.Sage
    val wallsColor = if (player == Player.P1) com.example.checka2.ui.theme.Terracotta.copy(alpha=0.6f) else com.example.checka2.ui.theme.Sage.copy(alpha=0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor.value, RoundedCornerShape(16.dp))
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
                 if (isActive) { // Optional visual indicator for turn, maybe pulse
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
                        text = "Your Turn",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Walls "Sticks" Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
             // Trophy if winner? Handled by dialog usually.
             // Just walls here.
             
             // Max 10 walls. Draw generic sticks.
             for (i in 0 until 10) {
                 Box(
                     modifier = Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .background(
                            if (i < wallsLeft) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), 
                            RoundedCornerShape(2.dp)
                        )
                 )
             }
        }
    }
}

@Composable
fun GameControls(
    isPlaceWallMode: Boolean,
    orientation: Orientation,
    wallsLeft: Int,
    onToggleMode: () -> Unit,
    onToggleOrientation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Using Stone800 -> WarmCharcoal from our theme for active states if needed, or stick to Material Theme mapping
        val moveContainer = if (!isPlaceWallMode) com.example.checka2.ui.theme.Stone800 else MaterialTheme.colorScheme.surface
        val moveContent = if (!isPlaceWallMode) com.example.checka2.ui.theme.WarmCream else com.example.checka2.ui.theme.Stone800
        
        val wallContainer = if (isPlaceWallMode) com.example.checka2.ui.theme.Stone800 else MaterialTheme.colorScheme.surface
        val wallContent = if (isPlaceWallMode) com.example.checka2.ui.theme.WarmCream else com.example.checka2.ui.theme.Stone800

        Button(
            onClick = { if (isPlaceWallMode) onToggleMode() },
            colors = ButtonDefaults.buttonColors(containerColor = moveContainer, contentColor = moveContent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Move")
        }
        
        Button(
            onClick = { if (!isPlaceWallMode) onToggleMode() },
            colors = ButtonDefaults.buttonColors(containerColor = wallContainer, contentColor = wallContent),
            shape = RoundedCornerShape(12.dp),
            enabled = wallsLeft > 0
        ) {
            Text("Place Wall")
        }
        
        if (isPlaceWallMode) {
             androidx.compose.animation.AnimatedContent(targetState = orientation, label = "rotation") { targetOrientation ->
                 IconButton(onClick = onToggleOrientation) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rotate Wall",
                        modifier = Modifier.rotate(if (targetOrientation == Orientation.Vertical) 90f else 0f)
                    )
                }
             }
        } else {
             // Show Trophy/Info or just spacer
             Icon(
                 imageVector = androidx.compose.material.icons.Icons.Filled.EmojiEvents,
                 contentDescription = "Trophy",
                 tint = com.example.checka2.ui.theme.Stone800
             )
        }
    }
}
