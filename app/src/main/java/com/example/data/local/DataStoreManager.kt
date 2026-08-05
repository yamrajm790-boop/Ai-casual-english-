package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "keyboard_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_AUTO_TRANSLATE = booleanPreferencesKey("auto_translate")
        val KEY_REALTIME_TRANSLATE = booleanPreferencesKey("realtime_translate")
        val KEY_BACKEND_URL = stringPreferencesKey("backend_url")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")

        const val DEFAULT_BACKEND_URL = "https://ai-casual-english-backend.onrender.com"
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "DARK"
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIBRATION_ENABLED] ?: true
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: false
    }

    val autoTranslate: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_TRANSLATE] ?: true
    }

    val realTimeTranslate: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REALTIME_TRANSLATE] ?: true
    }

    val backendUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_BACKEND_URL] ?: DEFAULT_BACKEND_URL
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_GEMINI_API_KEY] ?: ""
    }

    val autoCapitalize: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CAPITALIZE] ?: true
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setAutoTranslate(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_TRANSLATE] = enabled }
    }

    suspend fun setRealTimeTranslate(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_REALTIME_TRANSLATE] = enabled }
    }

    suspend fun setBackendUrl(url: String) {
        context.dataStore.edit { prefs -> prefs[KEY_BACKEND_URL] = url }
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { prefs -> prefs[KEY_GEMINI_API_KEY] = key }
    }

    suspend fun setAutoCapitalize(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_CAPITALIZE] = enabled }
    }
}
