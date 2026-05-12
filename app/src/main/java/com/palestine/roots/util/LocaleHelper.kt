package com.palestine.roots.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Helper class to apply and persist the app's display language.
 * Used by every Activity and the Application class to ensure
 * the correct locale is active throughout the app lifecycle.
 */
object LocaleHelper {

    private const val PREFS_NAME = "settings"
    private const val KEY_LANGUAGE = "language"

    /** Read the saved language code from SharedPreferences (default = "ar"). */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "ar") ?: "ar"
    }

    /**
     * Wrap the given [context] with the saved locale.
     * Call this inside `attachBaseContext` of every Activity
     * and inside `Application.attachBaseContext`.
     */
    fun applyLocale(context: Context): Context {
        return setLocale(context, getSavedLanguage(context))
    }

    /**
     * Set a specific locale on the given [context].
     * Returns a new context with the updated configuration.
     */
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}
