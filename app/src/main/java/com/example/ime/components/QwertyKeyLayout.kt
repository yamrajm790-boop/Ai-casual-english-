package com.example.ime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.KeyboardMode
import com.example.ime.ShiftState

@Composable
fun QwertyKeyLayout(
    shiftState: ShiftState,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onShiftClick: () -> Unit,
    onModeChange: (KeyboardMode) -> Unit,
    onLongPressKey: (String) -> Unit
) {
    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

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
                val displayChar = if (shiftState != ShiftState.OFF) char.uppercase() else char
                KeyButton(
                    text = displayChar,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPress(displayChar) },
                    onLongPress = { onLongPressKey(char) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row2.forEach { char ->
                val displayChar = if (shiftState != ShiftState.OFF) char.uppercase() else char
                KeyButton(
                    text = displayChar,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPress(displayChar) },
                    onLongPress = { onLongPressKey(char) }
                )
            }
        }

        // Row 3 (Shift, Z-M, Backspace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Shift key
            KeySpecialButton(
                modifier = Modifier.weight(1.3f),
                onClick = onShiftClick,
                isHighlight = shiftState != ShiftState.OFF
            ) {
                Icon(
                    imageVector = when (shiftState) {
                        ShiftState.CAPS_LOCK -> Icons.Default.Lock
                        ShiftState.ON -> Icons.Default.ArrowUpward
                        ShiftState.OFF -> Icons.Default.ArrowUpward
                    },
                    contentDescription = "Shift",
                    tint = if (shiftState != ShiftState.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            row3.forEach { char ->
                val displayChar = if (shiftState != ShiftState.OFF) char.uppercase() else char
                KeyButton(
                    text = displayChar,
                    modifier = Modifier.weight(1f),
                    onPress = { onKeyPress(displayChar) },
                    onLongPress = { onLongPressKey(char) }
                )
            }

            // Backspace key
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

        // Row 4 (Mode Switch, Emoji, Space, Period, Enter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ?123
            KeySpecialButton(
                modifier = Modifier.weight(1.3f),
                onClick = { onModeChange(KeyboardMode.NUMBERS) }
            ) {
                Text("?123", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // Emoji
            KeySpecialButton(
                modifier = Modifier.weight(1f),
                onClick = { onModeChange(KeyboardMode.EMOJI) }
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Emoji",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Spacebar
            Box(
                modifier = Modifier
                    .weight(4.2f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSpace() },
                contentAlignment = Alignment.Center
            ) {
                Text("English (AI Casual)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Period
            KeyButton(
                text = ".",
                modifier = Modifier.weight(1f),
                onPress = { onKeyPress(".") },
                onLongPress = { onKeyPress(",") }
            )

            // Enter / Action
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

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onPress() },
                    onLongPress = { onLongPress?.invoke() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun KeySpecialButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isHighlight: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isHighlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
