package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.repository.TranslationRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStoreManager = remember { DataStoreManager(context) }
    val repository = remember { TranslationRepository(context) }

    val themeMode by dataStoreManager.themeMode.collectAsState(initial = "DARK")
    val vibrationEnabled by dataStoreManager.vibrationEnabled.collectAsState(initial = true)
    val soundEnabled by dataStoreManager.soundEnabled.collectAsState(initial = false)
    val autoTranslate by dataStoreManager.autoTranslate.collectAsState(initial = true)
    val realTimeTranslate by dataStoreManager.realTimeTranslate.collectAsState(initial = true)
    val autoCapitalize by dataStoreManager.autoCapitalize.collectAsState(initial = true)

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var activeInfoDialog by remember { mutableStateOf<String?>(null) } // "PRIVACY", "TERMS", "ABOUT", "SUPPORT"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Settings Title Header
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = "SETTINGS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Customize your AI Keyboard preferences",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Appearance & Theme
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Appearance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = "Choose preferred keyboard theme display mode.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val options = listOf("DARK", "LIGHT")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = themeMode == option,
                            onClick = { scope.launch { dataStoreManager.setThemeMode(option) } },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                        ) {
                            Text(if (option == "DARK") "Dark Mode" else "Light Mode")
                        }
                    }
                }
            }
        }

        // Section 2: Keyboard & AI Translation Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Typing & AI Preferences",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Auto Translate (On/Off)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Auto Translate", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "Automatically rewrite typed text into casual English",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoTranslate,
                        onCheckedChange = { scope.launch { dataStoreManager.setAutoTranslate(it) } }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Real-Time Translation (On/Off)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Real-Time Translation", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = "Show live English preview as you stop typing",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = realTimeTranslate,
                        onCheckedChange = { scope.launch { dataStoreManager.setRealTimeTranslate(it) } }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Key Press Vibration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Key Press Vibration",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    Switch(
                        checked = vibrationEnabled,
                        onCheckedChange = { scope.launch { dataStoreManager.setVibrationEnabled(it) } }
                    )
                }

                // Key Click Sound
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Key Click Sound",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { scope.launch { dataStoreManager.setSoundEnabled(it) } }
                    )
                }

                // Auto Capitalize
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Auto-Capitalize First Letter", fontSize = 14.sp)
                    Switch(
                        checked = autoCapitalize,
                        onCheckedChange = { scope.launch { dataStoreManager.setAutoCapitalize(it) } }
                    )
                }
            }
        }

        // Section 3: Translation Data & History Management
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "History & Storage",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showClearHistoryDialog = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Clear Translation History",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
        }

        // Section 4: About, Legal & Support
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Information & Legal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                // About
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeInfoDialog = "ABOUT" }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "About AI Keyboard",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Privacy Policy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeInfoDialog = "PRIVACY" }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Privacy Policy",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Terms of Service
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeInfoDialog = "TERMS" }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Terms of Service",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Contact Support
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@aicasualkeyboard.app")
                                    putExtra(Intent.EXTRA_SUBJECT, "AI Casual Keyboard Support")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                activeInfoDialog = "SUPPORT"
                            }
                        }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Contact Support",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }

        // Section 5: App Version Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI Casual English Keyboard",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Version 1.0.0 (Build 100)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ==========================================
    // Clear History Confirmation Dialog
    // ==========================================
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Are you sure you want to delete all saved translation history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.clearHistory()
                            Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                            showClearHistoryDialog = false
                        }
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // Interactive Info Dialogs (Privacy, Terms, About, Support)
    // ==========================================
    activeInfoDialog?.let { dialogType ->
        val title = when (dialogType) {
            "PRIVACY" -> "Privacy Policy"
            "TERMS" -> "Terms of Service"
            "ABOUT" -> "About AI Casual Keyboard"
            else -> "Contact Support"
        }

        val text = when (dialogType) {
            "PRIVACY" -> "Privacy Policy:\n\n• Zero Personal Data Storage: We do not store or track any of your keystrokes or typed messages.\n• Encrypted AI Translation: All text sent for AI rewriting is transmitted via HTTPS and processed securely.\n• No Third-Party Tracking: Your data is never sold or shared with advertisers."
            "TERMS" -> "Terms of Service:\n\n• Service Scope: AI Casual Keyboard provides real-time casual English rewriting for personal communications.\n• Acceptable Use: You agree not to use this application to generate illegal, harmful, or dangerous content.\n• Service Availability: AI translation service requires an active internet connection for real-time processing."
            "ABOUT" -> "About AI Casual Keyboard:\n\nAI Casual Keyboard transforms natural text in any language or dialect (Hinglish, Hindi, Odia, Bengali, etc.) into fluent, natural casual spoken English.\n\nDesigned to make chatting effortless, natural, and human-sounding across WhatsApp, Instagram, Telegram, and any Android application."
            else -> "Need help or have suggestions?\n\nContact our support team anytime at:\nsupport@aicasualkeyboard.app\n\nWe respond within 24 hours."
        }

        AlertDialog(
            onDismissRequest = { activeInfoDialog = null },
            title = { Text(title) },
            text = { Text(text, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { activeInfoDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}
