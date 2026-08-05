package com.example.ime

enum class KeyMode {
    QWERTY_LOWER,
    QWERTY_UPPER,
    QWERTY_CAPS_LOCK,
    NUMBERS,
    SYMBOLS,
    EMOJI,
    CLIPBOARD
}

data class KeyboardUiState(
    val keyMode: KeyMode = KeyMode.QWERTY_LOWER,
    val isTranslating: Boolean = false,
    val translationError: String? = null,
    val currentComposingText: String = "",
    val englishPreviewText: String = "",
    val activeSuggestions: List<String> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val darkTheme: Boolean = true,
    val backendUrl: String = "https://ai-casual-english-backend.onrender.com",
    val geminiKey: String = "",
    val longPressKey: String? = null,
    val longPressOptions: List<String> = emptyList()
)
