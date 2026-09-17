package com.example.jarvis.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.jarvis.model.LanguageMode
import com.example.jarvis.ui.components.ChatConsole
import com.example.jarvis.ui.components.JarvisCoreAnimation
import com.example.jarvis.ui.components.QuickActionsSection
import com.example.jarvis.ui.components.VoiceWaveform
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavyBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemState by viewModel.systemState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val chatListState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }

    // Scroll to latest message automatically
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            chatListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    fun requestVoiceInput() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.toggleListening()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        containerColor = SpaceBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            // 1. Top HUD Navigation / Telemetry Bar
            JarvisHudTopBar(
                batteryPercentage = systemState.batteryPercentage,
                isCharging = systemState.isBatteryCharging,
                isTorchOn = systemState.isTorchOn,
                ttsEnabled = systemState.ttsEnabled,
                activeLanguage = systemState.activeLanguage,
                onToggleTorch = {
                    if (systemState.isTorchOn) {
                        viewModel.processUserInput("turn off flashlight")
                    } else {
                        viewModel.processUserInput("turn on flashlight")
                    }
                },
                onToggleTts = { viewModel.toggleTts() },
                onSelectLanguage = { viewModel.setLanguage(it) },
                onClearChat = { viewModel.clearChat() }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Central Arc Reactor AI Core
            JarvisCoreAnimation(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                isListening = systemState.isListening,
                isSpeaking = systemState.isSpeaking,
                isThinking = systemState.isThinking,
                rmsLevel = systemState.voiceRmsLevel,
                onClick = { requestVoiceInput() }
            )

            // 3. Audio Waveform Reactive Visualizer
            VoiceWaveform(
                voiceRms = systemState.voiceRmsLevel,
                isActive = systemState.isListening || systemState.isSpeaking,
                label = systemState.statusText,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 4. Quick Actions Ribbon
            QuickActionsSection(
                isTorchOn = systemState.isTorchOn,
                batteryPercentage = systemState.batteryPercentage,
                isCharging = systemState.isBatteryCharging,
                activeLanguage = systemState.activeLanguage,
                onToggleTorch = {
                    if (systemState.isTorchOn) {
                        viewModel.processUserInput("turn off flashlight")
                    } else {
                        viewModel.processUserInput("turn on flashlight")
                    }
                },
                onCheckBattery = { viewModel.processUserInput("battery status") },
                onLaunchCamera = { viewModel.processUserInput("open camera") },
                onOpenApp = { appName -> viewModel.processUserInput("open $appName") },
                onOpenWifi = { viewModel.processUserInput("open wifi settings") },
                onOpenBluetooth = { viewModel.processUserInput("open bluetooth settings") },
                onOpenSettings = { viewModel.processUserInput("open settings") },
                onSelectVoicePrompt = { prompt -> viewModel.processUserInput(prompt) },
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Chat History Console (takes remaining vertical weight)
            ChatConsole(
                messages = messages,
                listState = chatListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onSpeakMessage = { viewModel.speakMessage(it) }
            )

            // 6. Bottom Voice & Text Command Input Bar
            JarvisInputBar(
                text = textInput,
                onTextChanged = { textInput = it },
                isListening = systemState.isListening,
                onVoiceClick = { requestVoiceInput() },
                onSendClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.processUserInput(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
fun JarvisHudTopBar(
    batteryPercentage: Int,
    isCharging: Boolean,
    isTorchOn: Boolean,
    ttsEnabled: Boolean,
    activeLanguage: LanguageMode,
    onToggleTorch: () -> Unit,
    onToggleTts: () -> Unit,
    onSelectLanguage: (LanguageMode) -> Unit,
    onClearChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Branding with glowing status dot
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "J.A.R.V.I.S.",
                    color = CyanPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "SYS M.K. 85 // ONLINE",
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Right: Telemetry Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Battery Telemetry Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceElevated)
                    .border(BorderStroke(0.8.dp, SurfaceCardBorder), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = if (isCharging) NeonGreen else CyanPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (batteryPercentage >= 0) "$batteryPercentage%" else "--",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Language Selector Dropdown / Cycling Chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceElevated)
                    .border(BorderStroke(0.8.dp, CyanPrimary.copy(alpha = 0.5f)), RoundedCornerShape(4.dp))
                    .clickable {
                        val next = when (activeLanguage) {
                            LanguageMode.ENGLISH -> LanguageMode.BENGALI
                            LanguageMode.BENGALI -> LanguageMode.BANGLISH
                            LanguageMode.BANGLISH -> LanguageMode.ENGLISH
                        }
                        onSelectLanguage(next)
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeLanguage.displayName.uppercase(),
                    color = CyanPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Torch Toggle Icon
            IconButton(
                onClick = onToggleTorch,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Toggle Torch",
                    tint = if (isTorchOn) CyanGlow else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // TTS Toggle Icon
            IconButton(
                onClick = onToggleTts,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Toggle Voice Speech",
                    tint = if (ttsEnabled) CyanPrimary else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Clear Chat Log Icon
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Chat",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun JarvisInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    isListening: Boolean,
    onVoiceClick: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    val micBgColor by animateColorAsState(
        targetValue = if (isListening) CyanPrimary else SurfaceElevated,
        label = "mic_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("command_input_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Futuristic Voice Mic Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(if (isListening) micScale else 1.0f)
                .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                .background(micBgColor)
                .border(
                    BorderStroke(
                        1.5.dp,
                        if (isListening) Color.White else CyanPrimary.copy(alpha = 0.6f)
                    ),
                    CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                )
                .clickable(onClick = onVoiceClick)
                .testTag("voice_command_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = "Voice Command",
                tint = if (isListening) SpaceBlack else CyanPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Text Input Field
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("command_text_input"),
            placeholder = {
                Text(
                    text = if (isListening) "Listening to voice input..." else "Enter command or ask JARVIS...",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Default
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = DarkNavyBg,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = SurfaceCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = CyanPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendClick() })
        )

        // Send Command Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                .background(SurfaceElevated)
                .border(
                    BorderStroke(1.2.dp, CyanPrimary.copy(alpha = 0.7f)),
                    CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                )
                .clickable(onClick = onSendClick)
                .testTag("send_command_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = CyanPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
