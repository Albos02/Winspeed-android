package com.winspeed.app

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LAYOUT_MODE_KEY = stringPreferencesKey("layout_mode")
        val WIND_MODE_KEY = stringPreferencesKey("wind_mode")
        val MANUAL_WIND_DIRECTION_KEY = floatPreferencesKey("manual_wind_direction")
    }

    val themeFlow: Flow<Theme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: Theme.LIGHT.name
        Theme.valueOf(themeName)
    }

    val layoutModeFlow: Flow<LayoutMode> = context.dataStore.data.map { preferences ->
        val layoutName = preferences[LAYOUT_MODE_KEY] ?: LayoutMode.TWO_S.name
        LayoutMode.valueOf(layoutName)
    }

    val windModeFlow: Flow<WindMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[WIND_MODE_KEY] ?: WindMode.MANUAL.name
        WindMode.valueOf(modeName)
    }

    val manualWindDirectionFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MANUAL_WIND_DIRECTION_KEY] ?: 0f
    }

    suspend fun saveTheme(theme: Theme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun saveLayoutMode(layoutMode: LayoutMode) {
        context.dataStore.edit { preferences ->
            preferences[LAYOUT_MODE_KEY] = layoutMode.name
        }
    }

    suspend fun saveWindMode(windMode: WindMode) {
        context.dataStore.edit { preferences ->
            preferences[WIND_MODE_KEY] = windMode.name
        }
    }

    suspend fun saveManualWindDirection(direction: Float) {
        context.dataStore.edit { preferences ->
            preferences[MANUAL_WIND_DIRECTION_KEY] = direction
        }
    }
}
