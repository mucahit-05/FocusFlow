package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TimerMode
import com.example.ui.TimerUiState
import com.example.ui.components.TimerCircle
import com.example.ui.components.coloredGlassButton
import com.example.ui.components.floatingPressEffect
import com.example.ui.components.glassCardFillColor
import com.example.ui.components.isGlassDarkTheme
import com.example.ui.theme.BreakSage
import com.example.ui.theme.FocusRed
import com.example.ui.theme.LongBreakBlue
import com.example.util.AppStrings
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val FOCUS_QUOTE_INTERVAL_MS = 120_000L

@Composable
fun ZenModeScreen(
    timerState: TimerUiState,
    strings: AppStrings,
    onExitZen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modeColor = when (timerState.currentMode) {
        TimerMode.FOCUS -> FocusRed
        TimerMode.SHORT_BREAK -> BreakSage
        TimerMode.LONG_BREAK -> LongBreakBlue
    }
    val haptic = LocalHapticFeedback.current
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val quotes = remember(strings) {
        strings.zenFocusQuotes.ifEmpty { strings.motivationalQuotes }
    }
    var quoteIndex by remember(quotes) { mutableIntStateOf(0) }
    var isContentEntered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isContentEntered = true
    }

    val timerScale by animateFloatAsState(
        targetValue = if (isContentEntered) 1f else 0.82f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 260f),
        label = "zen_timer_entry_scale"
    )
    val timerAlpha by animateFloatAsState(
        targetValue = if (isContentEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing),
        label = "zen_timer_entry_alpha"
    )
    val quoteAlpha by animateFloatAsState(
        targetValue = if (isContentEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 560, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "zen_quote_entry_alpha"
    )
    val quoteSlidePx = with(LocalDensity.current) { 24.dp.toPx() }
    val quoteOffsetY by animateFloatAsState(
        targetValue = if (isContentEntered) 0f else quoteSlidePx,
        animationSpec = tween(durationMillis = 560, delayMillis = 180, easing = FastOutSlowInEasing),
        label = "zen_quote_entry_offset"
    )

    // Rotate calming motivational messages every 2 minutes during focus mode
    LaunchedEffect(quotes) {
        if (quotes.size > 1) {
            while (true) {
                delay(FOCUS_QUOTE_INTERVAL_MS)
                quoteIndex = (quoteIndex + 1) % quotes.size
            }
        }
    }

    BackHandler(enabled = true) {
        showExitConfirmDialog = true
    }

    FocusShiftingGradientBackdrop(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showExitConfirmDialog = true
                    }
                )
            }
            .testTag("zen_mode_screen")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                val zenStatusText = if (timerState.isRunning) {
                    if (timerState.currentMode == TimerMode.FOCUS) {
                        strings.timerStatusFocusing
                    } else {
                        strings.timerStatusBreak
                    }
                } else {
                    strings.timerStatusPaused
                }

                TimerCircle(
                    remainingSeconds = timerState.remainingSeconds,
                    totalSeconds = timerState.totalSeconds,
                    mode = timerState.currentMode,
                    isRunning = timerState.isRunning,
                    statusText = zenStatusText,
                    size = 270.dp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = timerScale
                        scaleY = timerScale
                        alpha = timerAlpha
                    }
                )

                Spacer(modifier = Modifier.height(38.dp))

                if (quotes.isNotEmpty()) {
                    AnimatedContent(
                        targetState = quotes[quoteIndex % quotes.size],
                        transitionSpec = {
                            (fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 4 }) togetherWith
                                (fadeOut(tween(550)) + slideOutVertically(tween(550)) { -it / 4 })
                        },
                        label = "focus_quote_transition",
                        modifier = Modifier.graphicsLayer {
                            alpha = quoteAlpha
                            translationY = quoteOffsetY
                        }
                    ) { quote ->
                        Text(
                            text = quote,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(max = 340.dp)
                                .padding(horizontal = 12.dp)
                                .testTag("zen_motivational_quote")
                        )
                    }
                }
            }
        }
    }

    if (showExitConfirmDialog) {
        val yesInteraction = remember { MutableInteractionSource() }
        val noInteraction = remember { MutableInteractionSource() }
        val buttonShape = RoundedCornerShape(16.dp)

        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            containerColor = glassCardFillColor(elevated = true).copy(alpha = 0.96f),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = strings.zenExitConfirmTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = strings.zenExitConfirmMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        onExitZen()
                    },
                    interactionSource = yesInteraction,
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .coloredGlassButton(
                            containerColor = modeColor,
                            shape = buttonShape,
                            interactionSource = yesInteraction,
                            shadowElevation = 8.dp,
                            pressScale = 0.94f,
                            pressDepth = 2.5.dp
                        )
                        .testTag("zen_exit_confirm_yes_button")
                ) {
                    Text(
                        text = strings.zenExitConfirmYes,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmDialog = false },
                    interactionSource = noInteraction,
                    shape = buttonShape,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .floatingPressEffect(
                            interactionSource = noInteraction,
                            pressScale = 0.94f,
                            pressDepth = 2.dp
                        )
                        .testTag("zen_exit_confirm_no_button")
                ) {
                    Text(
                        text = strings.zenExitConfirmNo,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun FocusShiftingGradientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isGlassDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "focus_aurora_gradient")

    val colorCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_color_cycle"
    )
    val driftAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_drift_angle"
    )

    val darkKeyframes = remember {
        listOf(
            Triple(Color(0xFF0A0D18), Color(0xFF28101A), Color(0xFF101322)),
            Triple(Color(0xFF180C1E), Color(0xFF142138), Color(0xFF0B141D)),
            Triple(Color(0xFF0A1B24), Color(0xFF1F271E), Color(0xFF101119)),
            Triple(Color(0xFF221311), Color(0xFF2E1722), Color(0xFF0C0E16))
        )
    }
    val lightKeyframes = remember {
        listOf(
            Triple(Color(0xFFF9E5DF), Color(0xFFF3EBF8), Color(0xFFE1ECF8)),
            Triple(Color(0xFFE3ECFA), Color(0xFFE7F2EE), Color(0xFFF6E9EC)),
            Triple(Color(0xFFE6F4ED), Color(0xFFECE6F8), Color(0xFFFBE9E1)),
            Triple(Color(0xFFF5E6F0), Color(0xFFFBECE2), Color(0xFFE4ECF7))
        )
    }

    val frames = if (isDark) darkKeyframes else lightKeyframes
    val baseIdx = colorCycle.toInt().coerceIn(0, frames.lastIndex)
    val nextIdx = (baseIdx + 1) % frames.size
    val fraction = (colorCycle - baseIdx).coerceIn(0f, 1f)

    val topColor = lerp(frames[baseIdx].first, frames[nextIdx].first, fraction)
    val midColor = lerp(frames[baseIdx].second, frames[nextIdx].second, fraction)
    val bottomColor = lerp(frames[baseIdx].third, frames[nextIdx].third, fraction)

    val orbColorOne = if (isDark) {
        lerp(FocusRed, LongBreakBlue, 0.5f + 0.5f * sin(driftAngle))
    } else {
        lerp(Color(0xFFFFB5A7), Color(0xFFA0C4FF), 0.5f + 0.5f * sin(driftAngle))
    }
    val orbColorTwo = if (isDark) {
        lerp(BreakSage, Color(0xFFA855F7), 0.5f + 0.5f * cos(driftAngle * 0.75f))
    } else {
        lerp(Color(0xFFA8E6CF), Color(0xFFD0BFFF), 0.5f + 0.5f * cos(driftAngle * 0.75f))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val startOffset = Offset(
                x = w * (0.25f + 0.25f * cos(driftAngle)),
                y = 0f
            )
            val endOffset = Offset(
                x = w * (0.75f - 0.25f * sin(driftAngle)),
                y = h
            )

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(topColor, midColor, bottomColor),
                    start = startOffset,
                    end = endOffset
                )
            )

            // Drifting atmospheric aurora glow 1
            val orb1Center = Offset(
                x = w * (0.35f + 0.30f * cos(driftAngle)),
                y = h * (0.28f + 0.16f * sin(driftAngle * 1.2f))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orbColorOne.copy(alpha = if (isDark) 0.26f else 0.34f),
                        orbColorOne.copy(alpha = if (isDark) 0.08f else 0.12f),
                        Color.Transparent
                    ),
                    center = orb1Center,
                    radius = w * 0.90f
                ),
                radius = w * 0.90f,
                center = orb1Center
            )

            // Drifting atmospheric aurora glow 2
            val orb2Center = Offset(
                x = w * (0.65f - 0.28f * sin(driftAngle * 0.85f)),
                y = h * (0.74f + 0.14f * cos(driftAngle))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orbColorTwo.copy(alpha = if (isDark) 0.22f else 0.30f),
                        orbColorTwo.copy(alpha = if (isDark) 0.06f else 0.10f),
                        Color.Transparent
                    ),
                    center = orb2Center,
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = orb2Center
            )
        }

        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground
        ) {
            content()
        }
    }
}
