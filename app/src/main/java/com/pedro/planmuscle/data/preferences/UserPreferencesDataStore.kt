package com.pedro.planmuscle.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pedro.planmuscle.ui.theme.AppThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeOption: AppThemeOption = AppThemeOption.NEON_FITNESS,
    val units: String = "KG_CM",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val defaultRestSeconds: Int = 90,
    val notificationsEnabled: Boolean = false
)

class UserPreferencesDataStore(private val context: Context) {

    private object PreferenceKeys {
        val THEME_OPTION = stringPreferencesKey("theme_option")
        val UNITS = stringPreferencesKey("units")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DEFAULT_REST_SECONDS = intPreferencesKey("default_rest_seconds")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeName = preferences[PreferenceKeys.THEME_OPTION] ?: AppThemeOption.NEON_FITNESS.name
        val theme = try {
            AppThemeOption.valueOf(themeName)
        } catch (e: Exception) {
            AppThemeOption.NEON_FITNESS
        }

        UserPreferences(
            themeOption = theme,
            units = preferences[PreferenceKeys.UNITS] ?: "KG_CM",
            soundEnabled = preferences[PreferenceKeys.SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[PreferenceKeys.VIBRATION_ENABLED] ?: true,
            defaultRestSeconds = preferences[PreferenceKeys.DEFAULT_REST_SECONDS] ?: 90,
            notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: false
        )
    }

    suspend fun setThemeOption(theme: AppThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_OPTION] = theme.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDefaultRestSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_REST_SECONDS] = seconds
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
