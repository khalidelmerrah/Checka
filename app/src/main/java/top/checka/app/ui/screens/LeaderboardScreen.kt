package top.checka.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.checka.app.data.RankedRepository
import top.checka.app.data.UserStats
import top.checka.app.data.api.LeaderboardEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    userStats: UserStats?, 
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var myRankEntry by remember { mutableStateOf<LeaderboardEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val repository = remember { RankedRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            val result = repository.getLeaderboard(limit = 50, userId = userStats?.userId)
            
            result.onSuccess { response ->
                leaderboard = response.leaderboard
                myRankEntry = response.userRank
                isLoading = false
            }.onFailure {
                error = it.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(top.checka.app.R.string.leaderboard)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // 1. My Rank Section (if available)
                    if (myRankEntry != null) {
                        Text("My Ranking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LeaderboardCard(entry = myRankEntry!!, index = myRankEntry!!.rank?.minus(1) ?: -1, isMe = true)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 2. Global List
                    Text("Global Top 50", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(leaderboard) { index, entry ->
                            LeaderboardCard(entry = entry, index = index, isMe = entry.id == userStats?.userId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(entry: LeaderboardEntry, index: Int, isMe: Boolean) {
    val rank = if (entry.rank != null) entry.rank else index + 1
    
    val cardColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else Color(0xFF252525)
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = if (isMe) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color(0xFF333333)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF333333), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = rankColor
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                )
                Text(
                    text = "Lvl ${entry.level} • ${entry.rankTitle ?: "Unranked"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                 Text(
                    text = "${entry.eloRating}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${entry.totalWins} Wins",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}
