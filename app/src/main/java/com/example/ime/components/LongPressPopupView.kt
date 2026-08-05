package com.example.ime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun getLongPressOptionsForKey(char: String): List<String> {
    val lower = char.lowercase()
    return when (lower) {
        "q" -> listOf("1")
        "w" -> listOf("2")
        "e" -> listOf("3", "é", "è", "ê", "ë")
        "r" -> listOf("4")
        "t" -> listOf("5")
        "y" -> listOf("6", "ÿ")
        "u" -> listOf("7", "ú", "ù", "û", "ü")
        "i" -> listOf("8", "í", "ì", "î", "ï")
        "o" -> listOf("9", "ó", "ò", "ô", "ö", "õ")
        "p" -> listOf("0")
        "a" -> listOf("@", "á", "à", "â", "ä", "ã", "å")
        "s" -> listOf("#", "ß", "ś")
        "d" -> listOf("$")
        "f" -> listOf("%")
        "g" -> listOf("&")
        "h" -> listOf("-")
        "j" -> listOf("+")
        "k" -> listOf("(")
        "l" -> listOf(")")
        "z" -> listOf("*", "ž", "ź")
        "x" -> listOf("/")
        "c" -> listOf("ç", "ć")
        "v" -> listOf("=")
        "b" -> listOf("!")
        "n" -> listOf("?", "ñ")
        "m" -> listOf("_")
        "." -> listOf(".", ",", "?", "!", "-", "@", "'")
        else -> emptyList()
    }
}

@Composable
fun LongPressPopupView(
    options: List<String>,
    onSelectOption: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (options.isEmpty()) return

    Surface(
        modifier = modifier.padding(bottom = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onSelectOption(option)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

