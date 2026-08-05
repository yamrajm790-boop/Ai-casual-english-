package com.example.ime

enum class ShiftState {
    OFF,
    ON,
    CAPS_LOCK
}

enum class KeyboardMode {
    QWERTY,
    NUMBERS,
    SYMBOLS,
    EMOJI,
    CLIPBOARD
}

data class KeyPopUpInfo(
    val keyLabel: String,
    val alternatives: List<String>
)
