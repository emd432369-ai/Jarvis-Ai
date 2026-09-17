package com.example.jarvis.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.TextTertiary
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    modifier: Modifier = Modifier,
    voiceRms: Float = 0f,
    isActive: Boolean = false,
    label: String = "AUDIO SENSORY BUS"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_waveform_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            val barCount = 28
            val totalWidth = size.width
            val totalHeight = size.height
            val centerY = totalHeight / 2f

            val barSpacing = 4f
            val barWidth = ((totalWidth - ((barCount - 1) * barSpacing)) / barCount).coerceAtLeast(3f)

            val activeMultiplier = if (isActive) {
                (voiceRms.coerceIn(0.15f, 1f) * 1.5f).coerceAtMost(1f)
            } else {
                0.08f
            }

            for (i in 0 until barCount) {
                // Symmetrical wave envelope: tallest in center
                val distFromCenter = abs(i - (barCount / 2f)) / (barCount / 2f)
                val envelope = (1f - (distFromCenter * 0.7f)).coerceIn(0.2f, 1f)

                // Sine wave variation with phase
                val waveVal = sin(Math.toRadians((phase + (i * 24.0)))).toFloat()
                val normalizedWave = (waveVal * 0.4f + 0.6f)

                val barHeight = (totalHeight * 0.85f * envelope * normalizedWave * activeMultiplier)
                    .coerceIn(4f, totalHeight)

                val startX = i * (barWidth + barSpacing)
                val topY = centerY - (barHeight / 2f)

                val brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(CyanGlow, CyanPrimary, ElectricBlue)
                    } else {
                        listOf(
                            CyanPrimary.copy(alpha = 0.3f),
                            ElectricBlue.copy(alpha = 0.2f)
                        )
                    },
                    startY = topY,
                    endY = topY + barHeight
                )

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(startX, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STATUS: ${if (isActive) "STREAMING" else "IDLE"}",
                color = if (isActive) CyanPrimary else TextTertiary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
            Text(
                text = label,
                color = TextTertiary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "RATE: 44.1 kHz",
                color = TextTertiary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }
    }
}
