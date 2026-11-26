package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.TimerMode
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BreakSage
import com.example.ui.theme.FocusRed
import com.example.ui.theme.LongBreakBlue

/**
 * Full-bleed atmospheric backdrop behind transparent Scaffolds.
 * In Light mode, renders a soft pearlescent sky-alabaster canvas with pastel ambient bokeh orbs
 * so frosted translucent white cards visibly refract and float above the surface.
 * In Dark / Pure Black modes, preserves the deep canvas with luminous mode ambient orbs.
 */
@Composable
fun GlassAmbientBackdrop(
    mode: TimerMode,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val baseBg = MaterialTheme.colorScheme.background
    val isDark = baseBg.luminance() < 0.5f

    val primaryGlow = when (mode) {
        TimerMode.FOCUS -> FocusRed
        TimerMode.SHORT_BREAK -> BreakSage
        TimerMode.LONG_BREAK -> LongBreakBlue
    }
    val secondaryGlow = when (mode) {
        TimerMode.FOCUS -> AmberAccent
        TimerMode.SHORT_BREAK -> LongBreakBlue
        TimerMode.LONG_BREAK -> BreakSage
    }
    val lightCoolOrb = Color(0xFF60A5FA)
    val lightLavenderOrb = Color(0xFFA78BFA)

    val backdropBrush = if (isDark) {
        Brush.verticalGradient(colors = listOf(baseBg, baseBg))
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFE7ECF5),
                Color(0xFFF2EDF3),
                Color(0xFFE2E9F4)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backdropBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Top-left primary ambient orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = if (isDark) 0.24f else 0.28f),
                        primaryGlow.copy(alpha = if (isDark) 0.07f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.14f, h * 0.12f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.14f, h * 0.12f)
            )

            // Mid-right secondary orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryGlow.copy(alpha = if (isDark) 0.17f else 0.24f),
                        secondaryGlow.copy(alpha = if (isDark) 0.05f else 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.44f),
                    radius = w * 0.76f
                ),
                radius = w * 0.76f,
                center = Offset(w * 0.88f, h * 0.44f)
            )

            if (!isDark) {
                // Cool sky/lavender pastel depth behind lower cards & navbar in Light mode
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            lightCoolOrb.copy(alpha = 0.22f),
                            lightLavenderOrb.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.20f, h * 0.84f),
                        radius = w * 0.82f
                    ),
                    radius = w * 0.82f,
                    center = Offset(w * 0.20f, h * 0.84f)
                )
            } else {
                // Bottom-left soft counter-glow behind navigation/bottom controls
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryGlow.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.22f, h * 0.90f),
                        radius = w * 0.72f
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * 0.22f, h * 0.90f)
                )
            }
        }

        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onBackground
        ) {
            content()
        }
    }
}

@Composable
fun isGlassDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun glassCardFillColor(
    accentColor: Color? = null,
    accentAlpha: Float = 0.05f,
    elevated: Boolean = false
): Color {
    val isDark = isGlassDarkTheme()
    val baseGlass = if (isDark) {
        val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (elevated) 0.62f else 0.46f)
        Color.White.copy(alpha = if (elevated) 0.07f else 0.04f).compositeOver(base)
    } else {
        Color.White.copy(alpha = if (elevated) 0.68f else 0.52f)
    }

    return if (accentColor != null) {
        accentColor.copy(alpha = accentAlpha.coerceAtMost(0.08f)).compositeOver(baseGlass)
    } else {
        baseGlass
    }
}

/**
 * Pure neutral frosted-glass specular rim border (bright top-left light catch fading to a
 * crisp frosted edge at bottom-right). Never draws orange/colored outlines around cards.
 */
@Composable
fun glassBorderStroke(
    width: Dp = 1.1.dp,
    highlightBoost: Float = 1f
): BorderStroke {
    val isDark = isGlassDarkTheme()
    val colors = if (isDark) {
        listOf(
            Color.White.copy(alpha = (0.30f * highlightBoost).coerceIn(0f, 0.75f)),
            Color.White.copy(alpha = (0.14f * highlightBoost).coerceIn(0f, 0.40f)),
            Color.White.copy(alpha = 0.05f)
        )
    } else {
        listOf(
            Color.White.copy(alpha = (0.96f * highlightBoost).coerceIn(0f, 1f)),
            Color.White.copy(alpha = 0.68f),
            Color(0xFF94A3B8).copy(alpha = 0.26f)
        )
    }

    return BorderStroke(
        width = width,
        brush = Brush.linearGradient(colors = colors)
    )
}

/**
 * Tactile floating-island Press-in / Press-down physics:
 * Scales inward slightly and translates downward along Y while pressing, then springs cleanly back.
 * Works safely alongside Material buttons, chips, and clickable surfaces using a shared InteractionSource.
 */
fun Modifier.floatingPressEffect(
    interactionSource: InteractionSource,
    pressScale: Float = 0.945f,
    pressDepth: Dp = 3.dp
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val density = LocalDensity.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressScale else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 620f),
        label = "floating_press_scale"
    )
    val depthPx by animateFloatAsState(
        targetValue = if (isPressed) with(density) { pressDepth.toPx() } else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 680f),
        label = "floating_press_depth"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationY = depthPx
    }
}

/**
 * Draws a soft outer ambient floating shadow clipped strictly OUTSIDE [shape] via ClipOp.Difference,
 * so the translucent glass card interior stays luminous and crystal clear without gray muddiness.
 */
private fun Modifier.drawFloatingGlassShadow(
    shape: Shape,
    shadowElevation: Dp,
    isDark: Boolean,
    tintColor: Color? = null,
    tintAlphaBoost: Float = 0f
): Modifier = this.drawBehind {
    if (shadowElevation <= 0.dp) return@drawBehind
    val maxSpread = shadowElevation.toPx()
    val yShift = (shadowElevation * 0.45f).toPx()
    val outline = shape.createOutline(size, layoutDirection, this)

    val innerPath = Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded -> addRoundRect(outline.roundRect)
            is Outline.Generic -> addPath(outline.path)
        }
    }

    clipPath(path = innerPath, clipOp = ClipOp.Difference) {
        val steps = 5
        val shadowBaseColor = if (tintColor != null && tintAlphaBoost > 0f) {
            tintColor.copy(alpha = tintAlphaBoost).compositeOver(
                if (isDark) Color.Black else Color(0xFF1E293B)
            )
        } else {
            if (isDark) Color.Black else Color(0xFF1E293B)
        }
        val peakAlpha = (if (isDark) 0.28f else 0.08f) + tintAlphaBoost * 0.35f

        for (i in steps downTo 1) {
            val fraction = i.toFloat() / steps.toFloat()
            val spread = maxSpread * fraction
            val layerAlpha = peakAlpha * (1f - fraction * 0.72f)
            val left = -spread * 0.45f
            val top = -spread * 0.12f + yShift * fraction
            val right = size.width + spread * 0.45f
            val bottom = size.height + spread * 0.78f + yShift * fraction * 0.4f

            when (outline) {
                is Outline.Rounded -> {
                    val rr = outline.roundRect
                    val extraR = spread * 0.55f
                    val expandedPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(left, top, right, bottom),
                                topLeft = CornerRadius(
                                    rr.topLeftCornerRadius.x + extraR,
                                    rr.topLeftCornerRadius.y + extraR
                                ),
                                topRight = CornerRadius(
                                    rr.topRightCornerRadius.x + extraR,
                                    rr.topRightCornerRadius.y + extraR
                                ),
                                bottomRight = CornerRadius(
                                    rr.bottomRightCornerRadius.x + extraR,
                                    rr.bottomRightCornerRadius.y + extraR
                                ),
                                bottomLeft = CornerRadius(
                                    rr.bottomLeftCornerRadius.x + extraR,
                                    rr.bottomLeftCornerRadius.y + extraR
                                )
                            )
                        )
                    }
                    drawPath(
                        path = expandedPath,
                        color = shadowBaseColor.copy(alpha = layerAlpha)
                    )
                }
                else -> {
                    drawRect(
                        color = shadowBaseColor.copy(alpha = layerAlpha),
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top)
                    )
                }
            }
        }
    }
}

/**
 * Floating Glassmorphism surface modifier:
 * 1. Soft outer ambient drop shadow outside the shape boundary (compresses on press when interactionSource provided)
 * 2. Directional translucent frosted fill + top-left glass reflection sheen
 * 3. Pure neutral specular white/ice rim border (no orange/colored outlines)
 */
fun Modifier.glassMorphism(
    shape: Shape,
    accentColor: Color? = null,
    accentAlpha: Float = 0.05f,
    elevated: Boolean = false,
    borderWidth: Dp = 1.1.dp,
    shadowElevation: Dp = 10.dp,
    interactionSource: InteractionSource? = null,
    pressScale: Float = 0.945f,
    pressDepth: Dp = 2.5.dp
): Modifier = composed {
    val isDark = isGlassDarkTheme()
    val isPressed = if (interactionSource != null) {
        val pressed by interactionSource.collectIsPressedAsState()
        pressed
    } else {
        false
    }

    val animatedShadow by animateDpAsState(
        targetValue = if (isPressed) (shadowElevation * 0.25f) else shadowElevation,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 650f),
        label = "glass_shadow_press"
    )

    val topLeftFill = if (isDark) {
        val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (elevated) 0.64f else 0.48f)
        val frost = Color.White.copy(alpha = if (elevated) 0.11f else 0.07f).compositeOver(base)
        if (accentColor != null) accentColor.copy(alpha = accentAlpha.coerceAtMost(0.08f)).compositeOver(frost) else frost
    } else {
        val frost = Color.White.copy(alpha = if (elevated) 0.78f else 0.64f)
        if (accentColor != null) accentColor.copy(alpha = accentAlpha.coerceAtMost(0.07f)).compositeOver(frost) else frost
    }

    val bottomRightFill = if (isDark) {
        val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (elevated) 0.50f else 0.36f)
        val frost = Color.White.copy(alpha = if (elevated) 0.03f else 0.02f).compositeOver(base)
        if (accentColor != null) accentColor.copy(alpha = (accentAlpha * 0.6f).coerceAtMost(0.05f)).compositeOver(frost) else frost
    } else {
        val frost = Color.White.copy(alpha = if (elevated) 0.46f else 0.32f)
        if (accentColor != null) accentColor.copy(alpha = (accentAlpha * 0.5f).coerceAtMost(0.04f)).compositeOver(frost) else frost
    }

    val glassBrush = Brush.linearGradient(
        colors = listOf(topLeftFill, bottomRightFill)
    )
    val rimStroke = glassBorderStroke(width = borderWidth)

    val pressModifier = if (interactionSource != null) {
        this.floatingPressEffect(
            interactionSource = interactionSource,
            pressScale = pressScale,
            pressDepth = pressDepth
        )
    } else {
        this
    }

    pressModifier
        .drawFloatingGlassShadow(
            shape = shape,
            shadowElevation = animatedShadow,
            isDark = isDark
        )
        .clip(shape)
        .background(glassBrush)
        .border(rimStroke, shape)
}

/**
 * Subtle Glassmorphism for vibrant colored primary buttons (Play/Pause, FAB, Save, Confirm).
 * Keeps the button's rich color saturation intact (~90%+) while adding a delicate top-left
 * specular glass sheen, crisp translucent rim, floating shadow, and press-in/down physics.
 */
fun Modifier.coloredGlassButton(
    containerColor: Color,
    shape: Shape,
    interactionSource: InteractionSource,
    shadowElevation: Dp = 9.dp,
    pressScale: Float = 0.945f,
    pressDepth: Dp = 3.dp
): Modifier = composed {
    val isDark = isGlassDarkTheme()
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedShadow by animateDpAsState(
        targetValue = if (isPressed) (shadowElevation * 0.25f) else shadowElevation,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 650f),
        label = "colored_glass_shadow"
    )

    // Keep color vibrant: very gentle top-left specular gleam, deep rich core, faint jewel translucency
    val topHighlight = Color.White.copy(alpha = if (isPressed) 0.08f else 0.18f)
        .compositeOver(containerColor.copy(alpha = 0.90f))
    val midBody = containerColor.copy(alpha = 0.94f)
    val bottomEdge = Color.Black.copy(alpha = if (isDark) 0.08f else 0.04f)
        .compositeOver(containerColor.copy(alpha = 0.88f))

    val rimBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isPressed) 0.28f else 0.46f),
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    this
        .floatingPressEffect(
            interactionSource = interactionSource,
            pressScale = pressScale,
            pressDepth = pressDepth
        )
        .drawFloatingGlassShadow(
            shape = shape,
            shadowElevation = animatedShadow,
            isDark = isDark,
            tintColor = containerColor,
            tintAlphaBoost = if (isPressed) 0.06f else 0.18f
        )
        .clip(shape)
        .background(Brush.linearGradient(colors = listOf(topHighlight, midBody, bottomEdge)))
        .border(BorderStroke(0.95.dp, rimBrush), shape)
}

/**
 * Lightly tinted floating glass surface for secondary colored action buttons
 * (e.g., Quick +1 session, inactive Focus button, secondary colored icon buttons).
 */
fun Modifier.tintedGlassButton(
    tintColor: Color,
    shape: Shape,
    interactionSource: InteractionSource,
    shadowElevation: Dp = 5.dp,
    pressScale: Float = 0.945f,
    pressDepth: Dp = 2.5.dp
): Modifier = composed {
    val isDark = isGlassDarkTheme()
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedShadow by animateDpAsState(
        targetValue = if (isPressed) (shadowElevation * 0.25f) else shadowElevation,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 650f),
        label = "tinted_glass_shadow"
    )

    val topFill = tintColor.copy(alpha = if (isDark) 0.24f else 0.16f)
        .compositeOver(Color.White.copy(alpha = if (isDark) 0.06f else 0.48f))
    val bottomFill = tintColor.copy(alpha = if (isDark) 0.15f else 0.10f)
        .compositeOver(Color.White.copy(alpha = if (isDark) 0.02f else 0.28f))

    val rimBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.28f else 0.86f),
            tintColor.copy(alpha = 0.22f),
            Color.White.copy(alpha = if (isDark) 0.06f else 0.30f)
        )
    )

    this
        .floatingPressEffect(
            interactionSource = interactionSource,
            pressScale = pressScale,
            pressDepth = pressDepth
        )
        .drawFloatingGlassShadow(
            shape = shape,
            shadowElevation = animatedShadow,
            isDark = isDark
        )
        .clip(shape)
        .background(Brush.linearGradient(colors = listOf(topFill, bottomFill)))
        .border(BorderStroke(0.9.dp, rimBrush), shape)
}

