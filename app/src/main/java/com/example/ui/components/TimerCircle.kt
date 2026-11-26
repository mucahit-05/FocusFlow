package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TimerMode
import com.example.ui.theme.BreakSage
import com.example.ui.theme.FocusRed
import com.example.ui.theme.LongBreakBlue
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TimerCircle(
    remainingSeconds: Int,
    totalSeconds: Int,
    mode: TimerMode,
    isRunning: Boolean,
    statusText: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 270.dp
) {
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "TimerArcProgress"
    )

    val primaryColor = when (mode) {
        TimerMode.FOCUS -> FocusRed
        TimerMode.SHORT_BREAK -> BreakSage
        TimerMode.LONG_BREAK -> LongBreakBlue
    }

    // Gentle breathing halo pulse when timer is active
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isRunning) 0.08f else 0.0f,
        targetValue = if (isRunning) 0.22f else 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    val tickBaseColor = MaterialTheme.colorScheme.onBackground

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("timer_circle_container")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.width - strokeWidth) / 2f

            // Background subtle track
            drawArc(
                color = primaryColor.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic breathing center glow when running
            if (isRunning && pulseAlpha > 0.005f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = pulseAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 0.88f
                    ),
                    radius = radius * 0.88f,
                    center = center
                )
            }

            // Tick marks on perimeter
            val totalTicks = 60
            for (i in 0 until totalTicks) {
                val angleRad = Math.toRadians((i * 6 - 90).toDouble())
                val isMajorTick = i % 5 == 0
                val tickLength = if (isMajorTick) 8.dp.toPx() else 4.dp.toPx()
                val tickAlpha = if (isMajorTick) 0.30f else 0.12f
                val innerR = radius - (strokeWidth / 2f) - 6.dp.toPx()
                val outerR = innerR - tickLength

                val startX = (center.x + innerR * cos(angleRad)).toFloat()
                val startY = (center.y + innerR * sin(angleRad)).toFloat()
                val endX = (center.x + outerR * cos(angleRad)).toFloat()
                val endY = (center.y + outerR * sin(angleRad)).toFloat()

                drawLine(
                    color = tickBaseColor.copy(alpha = tickAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajorTick) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // Foreground animated progress arc with subtle gradient
            val sweepAngle = animatedProgress * 360f
            if (sweepAngle > 0.1f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.8f),
                            primaryColor,
                            primaryColor
                        ),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Time Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.testTag("timer_countdown_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            val displayStatus = statusText ?: if (isRunning) {
                if (mode == TimerMode.FOCUS) "FOCUSING" else "ON BREAK"
            } else {
                "PAUSED"
            }

            Text(
                text = displayStatus,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                ),
                color = if (isRunning) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
