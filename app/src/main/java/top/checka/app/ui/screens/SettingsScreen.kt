package top.checka.app.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.res.stringResource
import top.checka.app.R

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.room.Room
import top.checka.app.data.AppDatabase
import top.checka.app.data.UserPreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeChanged: (String) -> Unit = {},
    onLanguageChanged: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val analyticsHelper = remember { top.checka.app.utils.AnalyticsHelper(context) }
    val scope = rememberCoroutineScope()
    
    // Theme State
    val themePref = UserPreferences.getTheme(context).collectAsState(initial = "System")
    
    // Language State
    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]?.language ?: "en"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            SettingsGroupCard(title = stringResource(R.string.theme), icon = Icons.Default.DarkMode) {
                val themeOptions = listOf(
                    "Light" to stringResource(R.string.light_mode),
                    "Dark" to stringResource(R.string.dark_mode),
                    "System" to stringResource(R.string.system_default)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    themeOptions.forEach { (key, label) ->
                        val selected = key == themePref.value
                        FilterChip(
                            selected = selected,
                            onClick = { 
                                scope.launch { UserPreferences.setTheme(context, key) }
                                onThemeChanged(key)
                                analyticsHelper.logThemeChanged(key)
                            },
                            label = { Text(label) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Language Section
            SettingsGroupCard(title = stringResource(R.string.language), icon = Icons.Default.Language) {
                 val langOptions = listOf(
                    "en" to stringResource(R.string.english),
                    "es" to stringResource(R.string.spanish),
                    "fr" to stringResource(R.string.french),
                    "ar" to stringResource(R.string.arabic)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    langOptions.forEach { (code, label) ->
                        val selected = code == currentLocale
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    onLanguageChanged(code)
                                    analyticsHelper.setUserLanguage(code)
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                                }
                            },
                            label = { Text(label) },
                            leadingIcon = if (selected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Reset Data Button (Styled nicely)
            Button(
                onClick = {
                    scope.launch {
                        val db = Room.databaseBuilder(context, AppDatabase::class.java, "checka-db").build()
                        db.matchDao().deleteAll()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)), // Red
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                 Icon(Icons.Default.Delete, null, tint = Color.White)
                 Spacer(modifier = Modifier.width(8.dp))
                 Text(stringResource(R.string.reset_leaderboard), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsGroupCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF252525), // Dark Card
        border = BorderStroke(1.dp, Color(0xFF333333)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFFBDBDBD))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
