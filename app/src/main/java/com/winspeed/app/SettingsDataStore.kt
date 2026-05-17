package com.winspeed.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
        val SPEED_UNIT_KEY = stringPreferencesKey("speed_unit")
        val MANUAL_WIND_DIRECTION_KEY = floatPreferencesKey("manual_wind_direction")
        val RECORDING_KEY = booleanPreferencesKey("recording")
        val PAUSED_KEY = booleanPreferencesKey("paused")
        val SESSION_ID_KEY = longPreferencesKey("session_id")
    }

    val themeFlow: Flow<Theme> = context.dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: Theme.LIGHT.name
        try { Theme.valueOf(themeName) } catch (e: Exception) { Theme.LIGHT }
    }

    val layoutModeFlow: Flow<LayoutMode> = context.dataStore.data.map { preferences ->
        val layoutName = preferences[LAYOUT_MODE_KEY] ?: LayoutMode.TWO_S.name
        try { LayoutMode.valueOf(layoutName) } catch (e: Exception) { LayoutMode.TWO_S }
    }

    val windModeFlow: Flow<WindMode> = context.dataStore.data.map { preferences ->
        val modeName = preferences[WIND_MODE_KEY] ?: WindMode.MANUAL.name
        try { WindMode.valueOf(modeName) } catch (e: Exception) { WindMode.MANUAL }
    }

    val speedUnitFlow: Flow<SpeedUnit> = context.dataStore.data.map { preferences ->
        val unitName = preferences[SPEED_UNIT_KEY] ?: SpeedUnit.KNOTS.name
        try { SpeedUnit.valueOf(unitName) } catch (e: Exception) { SpeedUnit.KNOTS }
    }

    val manualWindDirectionFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[MANUAL_WIND_DIRECTION_KEY] ?: 0f
    }

    val recordingFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[RECORDING_KEY] ?: false
    }
    
    val pausedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PAUSED_KEY] ?: false
    }
    
    val sessionIdFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[SESSION_ID_KEY]
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

    suspend fun saveSpeedUnit(speedUnit: SpeedUnit) {
        context.dataStore.edit { preferences ->
            preferences[SPEED_UNIT_KEY] = speedUnit.name
        }
    }

    suspend fun saveManualWindDirection(direction: Float) {
        context.dataStore.edit { preferences ->
            preferences[MANUAL_WIND_DIRECTION_KEY] = direction
        }
    }

    suspend fun saveRecording(recording: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[RECORDING_KEY] = recording
        }
    }
    
    suspend fun savePaused(paused: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PAUSED_KEY] = paused
        }
    }
    
    suspend fun saveSessionId(sessionId: Long?) {
        context.dataStore.edit { preferences ->
            if (sessionId != null) {
                preferences[SESSION_ID_KEY] = sessionId
            } else {
                preferences.remove(SESSION_ID_KEY)
            }
        }
    }
}
