package com.example.jarvis.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.ChatMessage
import com.example.jarvis.model.CommandType
import com.example.jarvis.model.MessageSender
import com.example.jarvis.model.MessageStatus
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatConsole(
    messages: List<ChatMessage>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onSpeakMessage: (String) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_console_container")
    ) {
        if (messages.isEmpty()) {
            EmptyChatPlaceholder()
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                    ) {
                        ChatMessageBubble(
                            message = message,
                            onSpeak = { onSpeakMessage(message.text) },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("JARVIS", message.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender and Timestamp header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 3.dp, start = 4.dp, end = 4.dp)
        ) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.Memory,
                contentDescription = null,
                tint = if (isUser) TextSecondary else CyanPrimary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "COMMANDER" else "J.A.R.V.I.S.",
                color = if (isUser) TextSecondary else CyanPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeStr,
                color = TextTertiary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Message Box
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(
                    CutCornerShape(
                        topStart = if (isUser) 10.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 10.dp,
                        topEnd = 6.dp,
                        bottomStart = 6.dp
                    )
                )
                .background(if (isUser) SurfaceElevated else SurfaceCard)
                .border(
                    BorderStroke(
                        1.dp,
                        if (isUser) ElectricBlue.copy(alpha = 0.5f) else CyanPrimary.copy(alpha = 0.4f)
                    ),
                    CutCornerShape(
                        topStart = if (isUser) 10.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 10.dp,
                        topEnd = 6.dp,
                        bottomStart = 6.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                // Command badge if applicable
                if (message.commandType != CommandType.NONE && !isUser) {
                    val badgeText = when (message.commandType) {
                        CommandType.TORCH_ON, CommandType.TORCH_OFF -> "HARDWARE // TORCH PROTOCOL"
                        CommandType.BATTERY_CHECK -> "DIAGNOSTIC // POWER CELLS"
                        CommandType.CAMERA_LAUNCH -> "OPTICAL // CAMERA SENSORS"
                        CommandType.APP_OPEN -> "PROCESS // APPLICATION DISPATCH"
                        CommandType.SETTINGS_WIFI, CommandType.SETTINGS_BLUETOOTH, CommandType.SETTINGS_SYSTEM -> "SYSTEM // CONFIG PROTOCOLS"
                        CommandType.GEMINI_QUERY -> "NEURAL // GEMINI INTELLIGENCE"
                        CommandType.GREETING -> "IDENTITY // JARVIS CORE"
                        else -> "SYSTEM NOTIFICATION"
                    }

                    Row(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyanPrimary.copy(alpha = 0.12f))
                            .border(BorderStroke(0.8.dp, CyanPrimary.copy(alpha = 0.4f)), RoundedCornerShape(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = badgeText,
                            color = CyanPrimary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (message.status == MessageStatus.THINKING) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyanPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Synthesizing neural response...",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    // Message content text
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Default
                    )

                    // Action buttons (Copy and TTS Replay for JARVIS responses)
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "JARVIS HUD TERMINAL ONLINE",
                color = CyanPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the AI Core or Microphone to issue a voice command in Bengali, Banglish, or English. You can also type commands below.",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp
            )
        }
    }
}
