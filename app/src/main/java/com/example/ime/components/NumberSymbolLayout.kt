package com.example.ime.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ime.KeyMode

@Composable
fun NumberSymbolLayout(
    keyMode: KeyMode,
    onKeyTap: (String) -> Unit,
    onDeleteTap: () -> Unit,
    onEnterTap: () -> Unit,
    onSwitchMode: (KeyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSymbols = keyMode == KeyMode.SYMBOLS

    val row1 = if (!isSymbols) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
               else listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")

    val row2 = if (!isSymbols) listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
               else listOf("£", "¢", "€", "¥", "^", "°", "=", "{", "}", "\\")

    val row3 = if (!isSymbols) listOf("*", "\"", "'", ":", ";", "!", "?", "%")
               else listOf("%", "©", "®", "™", "✓", "[", "]", "¡", "¿")

    val keyBackground = MaterialTheme.colorScheme.surface
    val actionKeyBackground = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { char ->
                KeyButton(
                    label = char,
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyBackground,
                    onTap = { onKeyTap(char) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { char ->
                KeyButton(
                    label = char,
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyBackground,
                    onTap = { onKeyTap(char) }
                )
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Toggle between 123 and =\<
            KeyButton(
                label = if (isSymbols) "123" else "=\\<",
                modifier = Modifier.weight(1.4f),
                backgroundColor = actionKeyBackground,
                onTap = { onSwitchMode(if (isSymbols) KeyMode.NUMBERS else KeyMode.SYMBOLS) }
            )

            row3.forEach { char ->
                KeyButton(
                    label = char,
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyBackground,
                    onTap = { onKeyTap(char) }
                )
            }

            // Delete Key
            KeyIconButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = actionKeyBackground,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onTap = onDeleteTap
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    modifier = Modifier.height(20.dp)
                )
            }
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ABC Switcher
            KeyButton(
                label = "ABC",
                modifier = Modifier.weight(1.3f),
                backgroundColor = actionKeyBackground,
                onTap = { onSwitchMode(KeyMode.QWERTY_LOWER) }
            )

            KeyButton(
                label = ",",
                modifier = Modifier.weight(1f),
                backgroundColor = keyBackground,
                onTap = { onKeyTap(",") }
            )

            // Spacebar
            KeyButton(
                label = "space",
                modifier = Modifier.weight(4.5f),
                backgroundColor = keyBackground,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                onTap = { onKeyTap(" ") }
            )

            KeyButton(
                label = ".",
                modifier = Modifier.weight(1f),
                backgroundColor = keyBackground,
                onTap = { onKeyTap(".") }
            )

            // Enter Key
            KeyIconButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onTap = onEnterTap
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                    contentDescription = "Enter",
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}
