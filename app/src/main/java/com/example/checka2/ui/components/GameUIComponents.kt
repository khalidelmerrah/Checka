package com.example.checka2.ui.components

import androidx.compose.ui.res.stringResource
import com.example.checka2.R

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
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
             val visibleSticks = minOf(wallsLeft, 5)
             val overflow = if (wallsLeft > 5) wallsLeft - 5 else 0
             
             for (i in 0 until visibleSticks) {
                 Box(
                     modifier = Modifier
                        .width(4.dp)
                        .height(20.dp)
                        .background(color, RoundedCornerShape(2.dp))
                 )
             }
             
             if (overflow > 0) {
                 Text(
                     text = "+$overflow",
                     style = MaterialTheme.typography.labelSmall,
                     fontWeight = FontWeight.Bold,
                     color = com.example.checka2.ui.theme.Stone800.copy(alpha=0.6f)
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
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Smart Segmented Switch
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
                            if (!isPlaceWallMode) com.example.checka2.ui.theme.Stone800 else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        ) // Animate this ideally, but simple state switch first
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(onClick = { if (isPlaceWallMode) onToggleMode() }), // Switch to Move
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.move),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!isPlaceWallMode) com.example.checka2.ui.theme.Cream else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Wall Segment
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(4.dp)
                        .background(
                            if (isPlaceWallMode) com.example.checka2.ui.theme.Stone800 else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(enabled = wallsLeft > 0, onClick = { if (!isPlaceWallMode) onToggleMode() }), // Switch to Wall
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.place_wall),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaceWallMode) com.example.checka2.ui.theme.Cream else if (wallsLeft > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Rotation Button (Dynamic)
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
             androidx.compose.animation.AnimatedVisibility(
                 visible = isPlaceWallMode,
                 enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                 exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
             ) {
                 Button(
                     onClick = onToggleOrientation,
                     modifier = Modifier.size(56.dp),
                     shape = CircleShape,
                     colors = ButtonDefaults.buttonColors(
                         containerColor = MaterialTheme.colorScheme.secondaryContainer,
                         contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                     ),
                     contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                 ) {
                     Icon(
                         imageVector = Icons.Default.Refresh,
                         contentDescription = "Rotate",
                         modifier = Modifier
                             .size(24.dp)
                             .rotate(if (orientation == Orientation.Vertical) 90f else 0f)
                     )
                 }
             }
        }
    }
}
