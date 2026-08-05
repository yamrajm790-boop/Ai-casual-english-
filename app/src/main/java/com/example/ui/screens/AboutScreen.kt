package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryIndigo

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "User Guide & Information",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

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
                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = PrimaryIndigo)
                    Text("Why Casual English?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "Textbook English often sounds overly rigid or unnatural in daily messaging. AI Casual English Keyboard instantly converts rigid or non-English text (Roman Hindi, Roman Odia, Hindi, Odia, Bengali, Tamil, Telugu, etc.) into modern, relaxed conversational English.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryIndigo)
                    Text("How To Use In Any App", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Enable and select 'AI Casual English Keyboard' in system input settings.", fontSize = 13.sp)
                    Text("2. Open WhatsApp, Telegram, Instagram, Messages, Notes, or any text field.", fontSize = 13.sp)
                    Text("3. Type your thoughts in Roman Hindi, Roman Odia, Hindi, Odia, Bengali, or formal English.", fontSize = 13.sp)
                    Text("4. Real-time translation preview shows spoken English directly above the keyboard.", fontSize = 13.sp)
                    Text("5. Pressing Send/Enter automatically inserts only the translated casual English!", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryIndigo)
                }
            }
        }

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
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = PrimaryIndigo)
                    Text("Supported Input Languages", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "• Roman Hindi ('kya kar rahe ho')\n• Roman Odia ('kana karucha')\n• Hindi ('क्या कर रहे हो')\n• Odia ('କଣ କରୁଛ')\n• Bengali, Tamil, Telugu, Kannada, Malayalam, Gujarati, Punjabi, Urdu & Mixed language input.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

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
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PrimaryIndigo)
                    Text("Privacy & Secure Backend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "All keystrokes are processed safely. Translations are routed securely to our dedicated backend service (ai-casual-english-backend.onrender.com). No API keys or sensitive configurations are exposed on device.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
