package com.example.ime.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.KeyboardMode

@Composable
fun NumberSymbolLayout(
    isSymbols: Boolean,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onModeChange: (KeyboardMode) -> Unit
) {
    val row1 = if (!isSymbols) listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    else listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")

    val row2 = if (!isSymbols) listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
    else listOf("£", "¥", "€", "¢", "^", "°", "=", "{", "}", "\\")

    val row3 = if (!isSymbols) listOf("*", "\"", "'", ":", ";", "!", "?", "%")
    else listOf("%", "©", "®", "™", "✓", "[", "]", "…")

    Column(
        modifier = Modifier
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
                    text = char,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPress(char) }
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
                    text = char,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPress(char) }
                )
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Toggle Symbol / Numbers
            KeySpecialButton(
                modifier = Modifier.weight(1.3f),
                onClick = {
                    if (isSymbols) onModeChange(KeyboardMode.NUMBERS)
                    else onModeChange(KeyboardMode.SYMBOLS)
                }
            ) {
                Text(if (isSymbols) "123" else "=\\<", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            row3.forEach { char ->
                KeyButton(
                    text = char,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPress(char) }
                )
            }

            KeySpecialButton(
                modifier = Modifier.weight(1.3f),
                onClick = onBackspace
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeySpecialButton(
                modifier = Modifier.weight(1.3f),
                onClick = { onModeChange(KeyboardMode.QWERTY) }
            ) {
                Text("ABC", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            KeyButton(
                text = ",",
                modifier = Modifier.weight(1f),
                onPress = { onKeyPress(",") }
            )

            KeySpecialButton(
                modifier = Modifier.weight(4.2f),
                onClick = onSpace
            ) {
                Text("space", fontSize = 13.sp)
            }

            KeyButton(
                text = ".",
                modifier = Modifier.weight(1f),
                onPress = { onKeyPress(".") }
            )

            KeySpecialButton(
                modifier = Modifier.weight(1.5f),
                onClick = onEnter,
                isHighlight = true
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardReturn,
                    contentDescription = "Enter",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
