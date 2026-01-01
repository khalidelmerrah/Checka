package com.example.checka2.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object UserPreferences {
    private val THEME_KEY = stringPreferencesKey("theme")
    // Values: "Light", "Dark", "System"

    suspend fun setTheme(context: Context, theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    fun getTheme(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[THEME_KEY] ?: "System"
        }
    }
    private val TUTORIAL_SEEN_KEY = booleanPreferencesKey("tutorial_seen")

    suspend fun setTutorialSeen(context: Context, seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TUTORIAL_SEEN_KEY] = seen
        }
    }

    fun getTutorialSeen(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[TUTORIAL_SEEN_KEY] ?: false
        }
    }
}
