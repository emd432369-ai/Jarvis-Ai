package com.example.jarvis.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisCoreAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    isListening: Boolean = false,
    isSpeaking: Boolean = false,
    isThinking: Boolean = false,
    rmsLevel: Float = 0f,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core_trans")

    // Dynamic rotation speeds based on activity
    val speedMultiplier = when {
        isListening -> 2.5f
        isSpeaking -> 2.0f
        isThinking -> 3.0f
        else -> 1.0f
    }

    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((12000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_outer"
    )

    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween((8000 / speedMultiplier).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_inner"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening || isSpeaking) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening || isSpeaking) 500 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    val statusText = when {
        isListening -> "LISTENING // AUDIO INPUT"
        isSpeaking -> "VOCALIZING // SPEECH OUTPUT"
        isThinking -> "NEURAL CORE COMPUTING"
        else -> "AI CORE ONLINE // STANDBY"
    }

    val statusColor = when {
        isListening -> CyanPrimary
        isSpeaking -> CyanGlow
        isThinking -> NeonGreen
        else -> CyanPrimary.copy(alpha = 0.7f)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .testTag("jarvis_ai_core"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f

                // 1. Outer Holographic Tech Ring
                rotate(rotationOuter, pivot = center) {
                    drawOuterTechRing(center, radius * 0.92f)
                }

                // 2. Middle Counter-Rotating Ring with Arc segments
                rotate(rotationInner, pivot = center) {
                    drawMiddleArcSegments(center, radius * 0.78f, isListening || isSpeaking)
                }

                // 3. Inner Energy Reactor Ring
                rotate(rotationOuter * 1.5f, pivot = center) {
                    drawInnerEnergyRing(center, radius * 0.62f)
                }

                // 4. Central Glowing Arc Reactor Nucleus
                val reactiveScale = pulseScale + (rmsLevel * 0.25f)
                drawGlowingNucleus(
                    center = center,
                    baseRadius = radius * 0.40f * reactiveScale,
                    glowAlpha = glowAlpha,
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    isThinking = isThinking
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp
        )
    }
}

private fun DrawScope.drawOuterTechRing(center: Offset, radius: Float) {
    // Thin base circular grid
    drawCircle(
        color = CyanPrimary.copy(alpha = 0.25f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )

    // Segmented dash arcs
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f, 4f, 14f), 0f)
    drawCircle(
        color = CyanPrimary.copy(alpha = 0.6f),
        radius = radius + 3f,
        center = center,
        style = Stroke(width = 2.5f, pathEffect = dashEffect)
    )

    // 4 Cardinal tick nodes
    val cardinalAngles = listOf(0.0, 90.0, 180.0, 270.0)
    for (angle in cardinalAngles) {
        val rad = Math.toRadians(angle)
        val x = center.x + (radius * cos(rad)).toFloat()
        val y = center.y + (radius * sin(rad)).toFloat()
        drawCircle(
            color = CyanPrimary,
            radius = 3.5f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawMiddleArcSegments(center: Offset, radius: Float, active: Boolean) {
    val alpha = if (active) 0.9f else 0.5f
    val strokeWidth = if (active) 4.5f else 3f

    // Arc 1 (Top-Left)
    drawArc(
        color = CyanPrimary.copy(alpha = alpha),
        startAngle = 20f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Arc 2 (Top-Right)
    drawArc(
        color = ElectricBlue.copy(alpha = alpha),
        startAngle = 110f,
        sweepAngle = 50f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Arc 3 (Bottom-Right)
    drawArc(
        color = CyanPrimary.copy(alpha = alpha),
        startAngle = 200f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Arc 4 (Bottom-Left)
    drawArc(
        color = ElectricBlue.copy(alpha = alpha),
        startAngle = 290f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawInnerEnergyRing(center: Offset, radius: Float) {
    val fineDashes = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
    drawCircle(
        color = CyanGlow.copy(alpha = 0.5f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.8f, pathEffect = fineDashes)
    )

    // Glowing satellite points
    for (i in 0 until 8) {
        val rad = Math.toRadians((i * 45.0))
        val x = center.x + (radius * cos(rad)).toFloat()
        val y = center.y + (radius * sin(rad)).toFloat()
        drawCircle(
            color = CyanPrimary.copy(alpha = 0.7f),
            radius = 2f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawGlowingNucleus(
    center: Offset,
    baseRadius: Float,
    glowAlpha: Float,
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean
) {
    val coreColor = when {
        isListening -> CyanPrimary
        isSpeaking -> CyanGlow
        isThinking -> NeonGreen
        else -> CyanPrimary
    }

    // Outer aura halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coreColor.copy(alpha = glowAlpha * 0.6f),
                coreColor.copy(alpha = glowAlpha * 0.2f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 1.6f
        ),
        radius = baseRadius * 1.6f,
        center = center
    )

    // Inner bright core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = glowAlpha),
                coreColor.copy(alpha = 0.9f),
                coreColor.copy(alpha = 0.4f)
            ),
            center = center,
            radius = baseRadius
        ),
        radius = baseRadius,
        center = center
    )

    // Center focal point
    drawCircle(
        color = Color.White,
        radius = baseRadius * 0.32f,
        center = center
    )
}
