package com.example.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ime.components.ClipboardView
import com.example.ime.components.EmojiPickerView
import com.example.ime.components.LongPressPopupView
import com.example.ime.components.NumberSymbolLayout
import com.example.ime.components.QwertyKeyLayout
import com.example.ime.components.SuggestionStrip

@Composable
fun KeyboardView(
    currentInputText: String,
    translatedText: String?,
    isLoading: Boolean,
    selectedTone: String,
    selectedSourceLanguage: String,
    autoTranslateEnabled: Boolean,
    realtimePreviewEnabled: Boolean,
    clipboardClips: List<String>,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onTranslateClick: () -> Unit,
    onApplyTranslation: (String) -> Unit,
    onToneSelect: (String) -> Unit,
    onLanguageSelect: (String) -> Unit,
    onToggleAutoTranslate: (Boolean) -> Unit,
    onSelectClip: (String) -> Unit
) {
    var shiftState by remember { mutableStateOf(ShiftState.OFF) }
    var mode by remember { mutableStateOf(KeyboardMode.QWERTY) }
    var activePopUpKey by remember { mutableStateOf<KeyPopUpInfo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 6.dp)
    ) {
        // AI Suggestion and Casual Speak Strip
        SuggestionStrip(
            currentInputText = currentInputText,
            translatedText = translatedText,
            isLoading = isLoading,
            selectedTone = selectedTone,
            selectedSourceLanguage = selectedSourceLanguage,
            autoTranslateEnabled = autoTranslateEnabled,
            realtimePreviewEnabled = realtimePreviewEnabled,
            onTranslateClick = onTranslateClick,
            onApplyTranslation = onApplyTranslation,
            onToneSelect = onToneSelect,
            onLanguageSelect = onLanguageSelect,
            onToggleAutoTranslate = onToggleAutoTranslate,
            onClipboardClick = { mode = KeyboardMode.CLIPBOARD }
        )

        // Long press popup alternatives
        activePopUpKey?.let { popup ->
            LongPressPopupView(
                alternatives = popup.alternatives,
                onSelectAlternative = { alt ->
                    onKeyPress(alt)
                    activePopUpKey = null
                }
            )
        }

        // Active Keyboard Mode Content
        when (mode) {
            KeyboardMode.QWERTY -> {
                QwertyKeyLayout(
                    shiftState = shiftState,
                    onKeyPress = { char ->
                        onKeyPress(char)
                        if (shiftState == ShiftState.ON) {
                            shiftState = ShiftState.OFF
                        }
                    },
                    onBackspace = onBackspace,
                    onSpace = onSpace,
                    onEnter = onEnter,
                    onShiftClick = {
                        shiftState = when (shiftState) {
                            ShiftState.OFF -> ShiftState.ON
                            ShiftState.ON -> ShiftState.CAPS_LOCK
                            ShiftState.CAPS_LOCK -> ShiftState.OFF
                        }
                    },
                    onModeChange = { mode = it },
                    onLongPressKey = { key ->
                        val alts = getAccentAlternatives(key)
                        if (alts.isNotEmpty()) {
                            activePopUpKey = KeyPopUpInfo(key, alts)
                        }
                    }
                )
            }
            KeyboardMode.NUMBERS -> {
                NumberSymbolLayout(
                    isSymbols = false,
                    onKeyPress = onKeyPress,
                    onBackspace = onBackspace,
                    onSpace = onSpace,
                    onEnter = onEnter,
                    onModeChange = { mode = it }
                )
            }
            KeyboardMode.SYMBOLS -> {
                NumberSymbolLayout(
                    isSymbols = true,
                    onKeyPress = onKeyPress,
                    onBackspace = onBackspace,
                    onSpace = onSpace,
                    onEnter = onEnter,
                    onModeChange = { mode = it }
                )
            }
            KeyboardMode.EMOJI -> {
                EmojiPickerView(
                    onEmojiSelect = onKeyPress,
                    onModeChange = { mode = it }
                )
            }
            KeyboardMode.CLIPBOARD -> {
                ClipboardView(
                    clipboardItems = clipboardClips,
                    onSelectClip = { clip ->
                        onSelectClip(clip)
                        mode = KeyboardMode.QWERTY
                    },
                    onModeChange = { mode = it }
                )
            }
        }
    }
}

private fun getAccentAlternatives(key: String): List<String> {
    return when (key.lowercase()) {
        "a" -> listOf("á", "à", "ä", "â", "ã")
        "e" -> listOf("é", "è", "ë", "ê")
        "i" -> listOf("í", "ì", "ï", "î")
        "o" -> listOf("ó", "ò", "ö", "ô", "õ")
        "u" -> listOf("ú", "ù", "ü", "û")
        "n" -> listOf("ñ")
        "c" -> listOf("ç")
        else -> emptyList()
    }
}
