package com.example.jarvis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jarvis.model.LanguageMode
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

@Composable
fun QuickActionsSection(
    modifier: Modifier = Modifier,
    isTorchOn: Boolean,
    batteryPercentage: Int,
    isCharging: Boolean,
    activeLanguage: LanguageMode,
    onToggleTorch: () -> Unit,
    onCheckBattery: () -> Unit,
    onLaunchCamera: () -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenWifi: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectVoicePrompt: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_actions_section")
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "// QUICK COMMAND MATRIX",
                color = CyanPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "SUB-SYSTEMS READY",
                color = TextTertiary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hardware Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Torch Action Button
            HudActionButton(
                title = if (isTorchOn) "TORCH ON" else "TORCH OFF",
                subtitle = if (isTorchOn) "ACTIVE" else "DISENGAGED",
                icon = if (isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                isActive = isTorchOn,
                accentColor = if (isTorchOn) CyanGlow else CyanPrimary,
                testTag = "btn_quick_torch",
                onClick = onToggleTorch
            )

            // Battery Action Button
            HudActionButton(
                title = if (batteryPercentage >= 0) "$batteryPercentage%" else "BATTERY",
                subtitle = if (isCharging) "CHARGING" else "DIAGNOSTIC",
                icon = Icons.Default.BatteryChargingFull,
                isActive = isCharging,
                accentColor = if (isCharging) NeonGreen else CyanPrimary,
                testTag = "btn_quick_battery",
                onClick = onCheckBattery
            )

            // Camera Action Button
            HudActionButton(
                title = "CAMERA",
                subtitle = "OPTICAL VIEW",
                icon = Icons.Default.CameraAlt,
                isActive = false,
                accentColor = CyanPrimary,
                testTag = "btn_quick_camera",
                onClick = onLaunchCamera
            )

            // Wi-Fi Action Button
            HudActionButton(
                title = "WI-FI",
                subtitle = "LINK CONFIG",
                icon = Icons.Default.Wifi,
                isActive = false,
                accentColor = CyanPrimary,
                testTag = "btn_quick_wifi",
                onClick = onOpenWifi
            )

            // Bluetooth Action Button
            HudActionButton(
                title = "BLUETOOTH",
                subtitle = "SHORT-RANGE",
                icon = Icons.Default.Bluetooth,
                isActive = false,
                accentColor = CyanPrimary,
                testTag = "btn_quick_bluetooth",
                onClick = onOpenBluetooth
            )

            // System Settings Action Button
            HudActionButton(
                title = "SETTINGS",
                subtitle = "OS PROTOCOLS",
                icon = Icons.Default.Settings,
                isActive = false,
                accentColor = CyanPrimary,
                testTag = "btn_quick_settings",
                onClick = onOpenSettings
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App Launchers Grid Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppLauncherChip(
                name = "YouTube",
                icon = Icons.Default.PlayArrow,
                accent = Color(0xFFFF3333),
                onClick = { onOpenApp("youtube") }
            )
            AppLauncherChip(
                name = "WhatsApp",
                icon = Icons.Default.Share,
                accent = Color(0xFF25D366),
                onClick = { onOpenApp("whatsapp") }
            )
            AppLauncherChip(
                name = "Facebook",
                icon = Icons.Default.Public,
                accent = Color(0xFF1877F2),
                onClick = { onOpenApp("facebook") }
            )
            AppLauncherChip(
                name = "Chrome",
                icon = Icons.Default.Language,
                accent = CyanPrimary,
                onClick = { onOpenApp("chrome") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-Language Prompt Shortcuts (Bengali, Banglish, English)
        Text(
            text = "// SAMPLE VOICE COMMANDS",
            color = TextTertiary,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val prompts = when (activeLanguage) {
                LanguageMode.BENGALI -> listOf(
                    "ফ্ল্যাশলাইট অন করো",
                    "ব্যাটারি কত আছে?",
                    "ইউটিউব খোলো",
                    "ক্যামেরা চালু করো",
                    "হোয়াটসঅ্যাপ ওপেন করো",
                    "কে তুমি?"
                )
                LanguageMode.BANGLISH -> listOf(
                    "Flashlight on koro",
                    "Battery koto ache?",
                    "YouTube kholo",
                    "Camera open koro",
                    "WhatsApp open koro",
                    "Tumi ke?"
                )
                LanguageMode.ENGLISH -> listOf(
                    "Turn on flashlight",
                    "What is my battery level?",
                    "Open YouTube",
                    "Launch Camera",
                    "Open WhatsApp",
                    "Who are you, JARVIS?"
                )
            }

            for (prompt in prompts) {
                VoicePromptChip(
                    text = prompt,
                    onClick = { onSelectVoicePrompt(prompt) }
                )
            }
        }
    }
}

@Composable
fun HudActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(112.dp)
            .height(58.dp)
            .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
            .background(if (isActive) SurfaceElevated else SurfaceCard)
            .border(
                BorderStroke(
                    1.dp,
                    if (isActive) accentColor else SurfaceCardBorder
                ),
                CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) accentColor else TextSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = title,
                    color = if (isActive) accentColor else TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AppLauncherChip(
    name: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceCard)
            .border(BorderStroke(1.dp, accent.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VoicePromptChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceElevated)
            .border(BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "\"$text\"",
            color = CyanPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal
        )
    }
}
