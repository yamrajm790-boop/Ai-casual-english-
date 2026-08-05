package com.example.ime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ime.components.ClipboardView
import com.example.ime.components.EmojiPickerView
import com.example.ime.components.LongPressPopupView
import com.example.ime.components.NumberSymbolLayout
import com.example.ime.components.QwertyKeyLayout
import com.example.ime.components.SuggestionStrip
import com.example.ime.components.getLongPressOptionsForKey

import com.example.ui.theme.MyApplicationTheme

@Composable
fun KeyboardView(
    uiState: KeyboardUiState,
    onKeyTap: (String) -> Unit,
    onKeyLongPress: (String) -> Unit,
    onLongPressOptionSelected: (String) -> Unit,
    onDismissLongPress: () -> Unit,
    onShiftTap: () -> Unit,
    onDeleteTap: () -> Unit,
    onEnterTap: () -> Unit,
    onSpaceSwipe: (Int) -> Unit,
    onGlobeTap: () -> Unit,
    onGlobeLongPress: () -> Unit,
    onAiTranslateClick: () -> Unit,
    onSendEnglishPreview: () -> Unit,
    onClearInput: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onOpenSettingsClick: () -> Unit,
    onOpenClipboardClick: () -> Unit,
    onSwitchMode: (KeyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    MyApplicationTheme(darkTheme = uiState.darkTheme) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    // Suggestion & Action Strip (Section 1: Read-Only English Preview, Section 2: Editable Input Field)
                    SuggestionStrip(
                        currentComposingText = uiState.currentComposingText,
                        englishPreviewText = uiState.englishPreviewText,
                        isTranslating = uiState.isTranslating,
                        suggestions = uiState.activeSuggestions,
                        onSendEnglishPreview = onSendEnglishPreview,
                        onClearInput = onClearInput,
                        onAiTranslateClick = onAiTranslateClick,
                        onSuggestionClick = onSuggestionClick,
                        onOpenSettingsClick = onOpenSettingsClick,
                        onOpenClipboardClick = onOpenClipboardClick
                    )

                    // Keyboard Layout based on KeyMode
                    when (uiState.keyMode) {
                        KeyMode.QWERTY_LOWER, KeyMode.QWERTY_UPPER, KeyMode.QWERTY_CAPS_LOCK -> {
                            QwertyKeyLayout(
                                keyMode = uiState.keyMode,
                                onKeyTap = onKeyTap,
                                onKeyLongPress = { char ->
                                    val options = getLongPressOptionsForKey(char)
                                    if (options.isNotEmpty()) {
                                        onKeyLongPress(char)
                                    } else {
                                        onKeyTap(char)
                                    }
                                },
                                onShiftTap = onShiftTap,
                                onDeleteTap = onDeleteTap,
                                onEnterTap = onEnterTap,
                                onSpaceSwipe = onSpaceSwipe,
                                onGlobeTap = onGlobeTap,
                                onGlobeLongPress = onGlobeLongPress,
                                onSwitchMode = onSwitchMode
                            )
                        }
                        KeyMode.NUMBERS, KeyMode.SYMBOLS -> {
                            NumberSymbolLayout(
                                keyMode = uiState.keyMode,
                                onKeyTap = onKeyTap,
                                onDeleteTap = onDeleteTap,
                                onEnterTap = onEnterTap,
                                onSwitchMode = onSwitchMode
                            )
                        }
                        KeyMode.EMOJI -> {
                            EmojiPickerView(
                                onEmojiTap = onKeyTap,
                                onDeleteTap = onDeleteTap,
                                onSwitchMode = onSwitchMode
                            )
                        }
                        KeyMode.CLIPBOARD -> {
                            ClipboardView(
                                onClipTap = onKeyTap,
                                onSwitchMode = onSwitchMode
                            )
                        }
                    }
                }

                // Long Press Overlay Popup
                AnimatedVisibility(
                    visible = uiState.longPressKey != null && uiState.longPressOptions.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    LongPressPopupView(
                        options = uiState.longPressOptions,
                        onSelectOption = onLongPressOptionSelected,
                        onDismiss = onDismissLongPress
                    )
                }
            }
        }
    }
}
