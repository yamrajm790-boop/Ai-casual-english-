package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "keyboard_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val KEY_AUTO_TRANSLATE = booleanPreferencesKey("auto_translate_enabled")
        val KEY_REALTIME_PREVIEW = booleanPreferencesKey("realtime_preview_enabled")
        val KEY_SOURCE_LANGUAGE = stringPreferencesKey("selected_source_language")
        val KEY_SELECTED_TONE = stringPreferencesKey("selected_casual_tone")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback_enabled")
        val KEY_SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback_enabled")
        val KEY_AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize_enabled")
        val KEY_AUTO_UPDATE = booleanPreferencesKey("auto_update_enabled")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DEBOUNCE_DELAY = intPreferencesKey("debounce_delay_ms")
    }

    val autoTranslateEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_TRANSLATE] ?: true
    }

    val realtimePreviewEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_REALTIME_PREVIEW] ?: true
    }

    val selectedSourceLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOURCE_LANGUAGE] ?: "Auto-detect"
    }

    val selectedTone: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_TONE] ?: "Casual & Natural"
    }

    val hapticFeedback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_FEEDBACK] ?: true
    }

    val soundFeedback: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_FEEDBACK] ?: true
    }

    val autoCapitalize: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_CAPITALIZE] ?: true
    }

    val autoUpdateEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_UPDATE] ?: true
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "System"
    }

    val debounceDelayMs: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEBOUNCE_DELAY] ?: 350
    }

    suspend fun setAutoTranslateEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_TRANSLATE] = enabled }
    }

    suspend fun setRealtimePreviewEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_REALTIME_PREVIEW] = enabled }
    }

    suspend fun setSelectedSourceLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[KEY_SOURCE_LANGUAGE] = language }
    }

    suspend fun saveSelectedTone(tone: String) {
        context.dataStore.edit { prefs -> prefs[KEY_SELECTED_TONE] = tone }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setSoundFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_SOUND_FEEDBACK] = enabled }
    }

    suspend fun setAutoCapitalize(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_CAPITALIZE] = enabled }
    }

    suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[KEY_AUTO_UPDATE] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[KEY_THEME_MODE] = mode }
    }

    suspend fun setDebounceDelayMs(delayMs: Int) {
        context.dataStore.edit { prefs -> prefs[KEY_DEBOUNCE_DELAY] = delayMs }
    }
}
