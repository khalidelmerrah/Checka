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
    private val FIRST_GAME_KEY = booleanPreferencesKey("first_game_started")

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

    suspend fun setFirstGamePlayed(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[FIRST_GAME_KEY] = true
        }
    }

    fun isFirstGame(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            // If key is missing, it IS the first game (return true).
            // Once set to true (played), we return false.
            // Wait, logic: key missing -> not played -> isFirstGame = true.
            // setFirstGamePlayed -> stores true.
            // So logic: if !prefs[KEY], then true.
            // Simplify: Store "has_played_game".
            // If has_played == false/null -> isFirstGame = true.
            !(prefs[FIRST_GAME_KEY] ?: false)
        }
    }
}
