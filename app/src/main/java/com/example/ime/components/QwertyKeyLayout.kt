package com.example.ime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ime.KeyMode

@Composable
fun QwertyKeyLayout(
    keyMode: KeyMode,
    onKeyTap: (String) -> Unit,
    onKeyLongPress: (String) -> Unit,
    onShiftTap: () -> Unit,
    onDeleteTap: () -> Unit,
    onEnterTap: () -> Unit,
    onSpaceSwipe: (Int) -> Unit,
    onGlobeTap: () -> Unit,
    onGlobeLongPress: () -> Unit,
    onSwitchMode: (KeyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUpper = keyMode == KeyMode.QWERTY_UPPER || keyMode == KeyMode.QWERTY_CAPS_LOCK

    val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
    val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
    val row3 = listOf("z", "x", "c", "v", "b", "n", "m")

    val keyBackground = MaterialTheme.colorScheme.surface
    val actionKeyBackground = MaterialTheme.colorScheme.surfaceVariant
    val activeShiftColor = if (keyMode == KeyMode.QWERTY_CAPS_LOCK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1 (Q - P)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            row1.forEach { char ->
                val displayChar = if (isUpper) char.uppercase() else char
                KeyButton(
                    label = displayChar,
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyBackground,
                    onTap = { onKeyTap(displayChar) },
                    onLongPress = { onKeyLongPress(char) }
                )
            }
        }

        // Row 2 (A - L)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            row2.forEach { char ->
                val displayChar = if (isUpper) char.uppercase() else char
                KeyButton(
                    label = displayChar,
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyBackground,
                    onTap = { onKeyTap(displayChar) },
                    onLongPress = { onKeyLongPress(char) }
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }

        // Row 3 (Shift, Z-M, Delete)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Shift Key
            KeyIconButton(
                modifier = Modifier.weight(1.4f),
                backgroundColor = if (isUpper) activeShiftColor else actionKeyBackground,
                contentColor = if (isUpper) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                onTap = onShiftTap
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Shift",
                    modifier = Modifier.height(20.dp)
                )
            }

            row3.forEach { char ->
                val displayChar = if (isUpper) char.uppercase() else char
                KeyButton(
                    label = displayChar,
                    modifier = Modifier.weight(1f),
                    backgroundColor = keyBackground,
                    onTap = { onKeyTap(displayChar) },
                    onLongPress = { onKeyLongPress(char) }
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

        // Row 4: Bottom Row -> [123 | Emoji | Space | . | Globe (🌐) | Enter]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 123 Button
            KeyButton(
                label = "123",
                modifier = Modifier.weight(1.2f),
                backgroundColor = actionKeyBackground,
                onTap = { onSwitchMode(KeyMode.NUMBERS) }
            )

            // Emoji Button
            KeyIconButton(
                modifier = Modifier.weight(1f),
                backgroundColor = actionKeyBackground,
                onTap = { onSwitchMode(KeyMode.EMOJI) }
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Emoji Picker",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(20.dp)
                )
            }

            // Spacebar (with swipe cursor detection)
            var totalDragX by remember { mutableFloatStateOf(0f) }
            Box(
                modifier = Modifier
                    .weight(4.2f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(keyBackground)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onKeyTap(" ") }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { totalDragX = 0f },
                            onDragEnd = { totalDragX = 0f },
                            onDragCancel = { totalDragX = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                                if (totalDragX > 30f) {
                                    onSpaceSwipe(1) // Cursor right
                                    totalDragX = 0f
                                } else if (totalDragX < -30f) {
                                    onSpaceSwipe(-1) // Cursor left
                                    totalDragX = 0f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "English",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Period Key
            KeyButton(
                label = ".",
                modifier = Modifier.weight(1f),
                backgroundColor = keyBackground,
                onTap = { onKeyTap(".") },
                onLongPress = { onKeyLongPress(".") }
            )

            // Globe / Switch Keyboard Key (🌐)
            KeyIconButton(
                modifier = Modifier.weight(1f),
                backgroundColor = actionKeyBackground,
                onTap = onGlobeTap,
                onLongPress = onGlobeLongPress
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Switch Keyboard",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.height(20.dp)
                )
            }

            // Enter Key
            KeyIconButton(
                modifier = Modifier.weight(1.3f),
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

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onTap: () -> Unit,
    onLongPress: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .pointerInput(label) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress?.invoke() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 19.sp,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
fun KeyIconButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onTap: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress?.invoke() }
                )
            },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

