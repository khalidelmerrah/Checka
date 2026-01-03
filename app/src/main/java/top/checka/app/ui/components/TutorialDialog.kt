package top.checka.app.ui.components

import androidx.compose.ui.res.stringResource
import top.checka.app.R
import top.checka.app.data.UserPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialScreen(
    onDismiss: () -> Unit
) {
    val pages = listOf(
        TutorialPage(stringResource(R.string.tut_goal_title), stringResource(R.string.tut_goal_desc)),
        TutorialPage(stringResource(R.string.tut_turn_title), stringResource(R.string.tut_turn_desc)),
        TutorialPage(stringResource(R.string.tut_move_title), stringResource(R.string.tut_move_desc)),
        TutorialPage(stringResource(R.string.tut_walls_title), stringResource(R.string.tut_walls_desc)),
        TutorialPage(stringResource(R.string.tut_win_title), stringResource(R.string.tut_win_desc))
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val analyticsHelper = remember { top.checka.app.utils.AnalyticsHelper(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header / Close button for Screen version
             Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                 IconButton(onClick = onDismiss) {
                     Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close")
                 }
             }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    HorizontalPager(state = pagerState) { pageIndex ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = pages[pageIndex].title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${pageIndex + 1}",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                                )
                            }

                            Text(
                                text = pages[pageIndex].description,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                minLines = 3
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        Modifier.wrapContentHeight().fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pagerState.pageCount) { iteration ->
                            val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                         // .... Same buttons
                        TextButton(onClick = {
                            analyticsHelper.logTutorialAction("Skipped")
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.skip))
                        }
                        
                        Button(
                            onClick = {
                                if (pagerState.currentPage < pages.size - 1) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                } else {
                                    analyticsHelper.logTutorialAction("Finished")
                                    onDismiss()
                                }
                            }
                        ) {
                            Text(if (pagerState.currentPage < pages.size - 1) stringResource(R.string.next) else stringResource(R.string.start))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
         // Reusing Screen logic but adapting simplisticly or just verify the Screen composable works in Dialog
         // For now, let's just KEEP TutorialDialog as is (duplicate logic or extracted content) to avoid breaking existing calls
         // Actually I will REPLACE the content of Screen with extracted content.
         // ... For this task, to be safe, I will just COPY the lists and logic to TutorialScreen and leave Dialog alone.
         // Or better, define TutorialContent.
         TutorialScreen(onDismiss = onDismiss)
    }
}

data class TutorialPage(val title: String, val description: String)
