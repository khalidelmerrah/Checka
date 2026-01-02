package top.checka.app.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logAppOpen() {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        // Custom event if needed, but APP_OPEN is standard
        firebaseAnalytics.logEvent("app_launched", null)
    }

    fun logGameStart(mode: String, difficulty: String? = null) {
        val bundle = Bundle().apply {
            putString("game_mode", mode)
            if (difficulty != null) {
                putString("difficulty", difficulty)
            }
        }
        firebaseAnalytics.logEvent("game_started", bundle)
    }

    fun logThemeChanged(theme: String) {
        val bundle = Bundle().apply {
            putString("theme_selected", theme)
        }
        firebaseAnalytics.logEvent("theme_changed", bundle)
    }

    fun setUserLanguage(language: String) {
        firebaseAnalytics.setUserProperty("app_language", language)
    }

    fun logFirstGame() {
        firebaseAnalytics.logEvent("first_game_started", null)
    }

    fun logGameEnd(mode: String, result: String) {
        val bundle = Bundle().apply {
            putString("game_mode", mode)
            putString("result", result)
        }
        firebaseAnalytics.logEvent("game_ended", bundle)
    }

    fun logTutorialAction(action: String) {
        val bundle = Bundle().apply {
            putString("action", action)
        }
        firebaseAnalytics.logEvent("tutorial_interaction", bundle)
    }
}
