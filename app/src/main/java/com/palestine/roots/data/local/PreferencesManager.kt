package com.palestine.roots.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.channels.awaitClose

class PreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    val isDarkMode: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DARK_MODE) {
                offer(prefs.getBoolean(KEY_DARK_MODE, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        offer(prefs.getBoolean(KEY_DARK_MODE, false))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val language: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_LANGUAGE) {
                offer(prefs.getString(KEY_LANGUAGE, "ar") ?: "ar")
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        offer(prefs.getString(KEY_LANGUAGE, "ar") ?: "ar")
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val isOnboardingCompleted: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ONBOARDING_COMPLETED) {
                offer(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        offer(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    suspend fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    suspend fun toggleDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    suspend fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }
}
