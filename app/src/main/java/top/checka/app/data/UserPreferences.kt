package top.checka.app.data

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
    // User Stats
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val ELO_KEY = androidx.datastore.preferences.core.intPreferencesKey("elo")
    private val XP_KEY = androidx.datastore.preferences.core.intPreferencesKey("xp")
    private val LEVEL_KEY = androidx.datastore.preferences.core.intPreferencesKey("level")
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val AVATAR_URL_KEY = stringPreferencesKey("avatar_url") // New Key

    suspend fun saveUserStats(context: Context, userId: String, username: String, elo: Int, xp: Int, level: Int, avatarUrl: String?) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
            prefs[USERNAME_KEY] = username
            prefs[ELO_KEY] = elo
            prefs[XP_KEY] = xp
            prefs[LEVEL_KEY] = level
            if (avatarUrl != null) {
                prefs[AVATAR_URL_KEY] = avatarUrl
            }
        }
    }

    fun getUserStats(context: Context): Flow<UserStats> {
        return context.dataStore.data.map { prefs ->
            UserStats(
                userId = prefs[USER_ID_KEY],
                username = prefs[USERNAME_KEY],
                elo = prefs[ELO_KEY] ?: 1200,
                xp = prefs[XP_KEY] ?: 0,
                level = prefs[LEVEL_KEY] ?: 1,
                avatarUrl = prefs[AVATAR_URL_KEY]
            )
        }
    }
}

data class UserStats(
    val userId: String?,
    val username: String?,
    val elo: Int,
    val xp: Int,
    val level: Int,
    val avatarUrl: String?
)
