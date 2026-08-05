package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DataStoreManager
import com.example.ui.theme.PrimaryIndigo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()

    var autoTranslate by remember { mutableStateOf(true) }
    var realtimePreview by remember { mutableStateOf(true) }
    var sourceLanguage by remember { mutableStateOf("Auto-detect") }
    var selectedTone by remember { mutableStateOf("Casual & Natural") }
    var hapticFeedback by remember { mutableStateOf(true) }
    var soundFeedback by remember { mutableStateOf(true) }
    var autoCapitalize by remember { mutableStateOf(true) }
    var autoUpdate by remember { mutableStateOf(true) }
    var debounceDelay by remember { mutableStateOf(350) }

    var isLangMenuExpanded by remember { mutableStateOf(false) }

    val languages = listOf(
        "Auto-detect",
        "Roman Hindi",
        "Roman Odia",
        "Hindi",
        "Odia",
        "Bengali",
        "Tamil",
        "Telugu",
        "Kannada",
        "Malayalam",
        "Gujarati",
        "Punjabi",
        "Urdu",
        "Mixed"
    )

    val tones = listOf(
        "Casual & Natural" to "Everyday spoken English used by native speakers.",
        "Friendly Chat" to "Warm, conversational, and approachable.",
        "Slang & Chill" to "Relaxed slang, contractions, and modern expressions.",
        "Gen Z Vibe" to "Trending youth phrases, vibe-check, and modern slang.",
        "Work Casual" to "Polite yet modern non-stiff business English."
    )

    LaunchedEffect(Unit) {
        autoTranslate = dataStoreManager.autoTranslateEnabled.first()
        realtimePreview = dataStoreManager.realtimePreviewEnabled.first()
        sourceLanguage = dataStoreManager.selectedSourceLanguage.first()
        selectedTone = dataStoreManager.selectedTone.first()
        hapticFeedback = dataStoreManager.hapticFeedback.first()
        soundFeedback = dataStoreManager.soundFeedback.first()
        autoCapitalize = dataStoreManager.autoCapitalize.first()
        autoUpdate = dataStoreManager.autoUpdateEnabled.first()
        debounceDelay = dataStoreManager.debounceDelayMs.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Keyboard Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Translation Automation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryIndigo
                    )
                    Text(
                        text = "AI Translation Features",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Translate on Send/Enter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Replaces typed text with translated English when Enter is pressed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoTranslate,
                        onCheckedChange = {
                            autoTranslate = it
                            scope.launch { dataStoreManager.setAutoTranslateEnabled(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Real-Time Preview", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Shows live English translation as you type in any app", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = realtimePreview,
                        onCheckedChange = {
                            realtimePreview = it
                            scope.launch { dataStoreManager.setRealtimePreviewEnabled(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
                    )
                }
            }
        }

        // Default Source Language Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = PrimaryIndigo
                    )
                    Text(
                        text = "Default Input Language",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Select your primary typing language or leave on Auto-detect for multi-language support (Roman Hindi, Roman Odia, Hindi, Odia, Bengali, Tamil, etc.)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isLangMenuExpanded = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected Language: $sourceLanguage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryIndigo
                    )

                    DropdownMenu(
                        expanded = isLangMenuExpanded,
                        onDismissRequest = { isLangMenuExpanded = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    sourceLanguage = lang
                                    scope.launch { dataStoreManager.setSelectedSourceLanguage(lang) }
                                    isLangMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Casual Tone Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = PrimaryIndigo
                    )
                    Text(
                        text = "Default Casual Tone",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                tones.forEach { (toneName, toneDesc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTone = toneName
                                scope.launch { dataStoreManager.saveSelectedTone(toneName) }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTone == toneName,
                            onClick = {
                                selectedTone = toneName
                                scope.launch { dataStoreManager.saveSelectedTone(toneName) }
                            }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = toneName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = toneDesc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Feedback & Behavior Switches Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = PrimaryIndigo)
                        Column {
                            Text("Haptic Feedback", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Vibrate gently on key press", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = {
                            hapticFeedback = it
                            scope.launch { dataStoreManager.setHapticFeedback(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = PrimaryIndigo)
                        Column {
                            Text("Key Click Sound", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Play keypress sound effect", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = soundFeedback,
                        onCheckedChange = {
                            soundFeedback = it
                            scope.launch { dataStoreManager.setSoundFeedback(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PrimaryIndigo)
                        Column {
                            Text("Auto Update Checks", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Check for keyboard improvements automatically", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = autoUpdate,
                        onCheckedChange = {
                            autoUpdate = it
                            scope.launch { dataStoreManager.setAutoUpdateEnabled(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo)
                    )
                }
            }
        }

        // App Theme Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = PrimaryIndigo)
                    Text("App Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                listOf("System", "Light", "Dark").forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onThemeChange(mode)
                                scope.launch { dataStoreManager.setThemeMode(mode) }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == mode,
                            onClick = {
                                onThemeChange(mode)
                                scope.launch { dataStoreManager.setThemeMode(mode) }
                            }
                        )
                        Text(text = "$mode Theme", modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
