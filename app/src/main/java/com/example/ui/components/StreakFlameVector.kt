package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Maps consecutive streak days to progressive flame intensity levels:
 * 0 -> Extinguished ash/ember (0 days)
 * 1 -> Spark Flame (1-2 days)
 * 2 -> Steady Blaze (3-6 days)
 * 3 -> Roaring Fire (7-13 days)
 * 4 -> Master Inferno (14+ days)
 */
fun streakToFlameLevel(streakDays: Int): Int = when {
    streakDays <= 0 -> 0
    streakDays <= 2 -> 1
    streakDays <= 6 -> 2
    streakDays <= 13 -> 3
    else -> 4
}

/**
 * Custom animated vector flame icon with extinguished ember state (level 0)
 * and 4 escalating flame tiers (levels 1..4).
 */
@Composable
fun StreakFlameVectorIcon(
    streakDays: Int,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    overrideLevel: Int? = null
) {
    val level = (overrideLevel ?: streakToFlameLevel(streakDays)).coerceIn(0, 4)
    val isDark = isGlassDarkTheme()

    val infiniteTransition = rememberInfiniteTransition(label = "streak_flame_anim")
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (level >= 3) 950 else 1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_flicker"
    )
    val sparkRise by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spark_rise"
    )

    Canvas(modifier = modifier.size(size)) {
        if (level == 0) {
            drawExtinguishedEmber(isDark = isDark, pulse = flicker)
        } else {
            drawActiveFlame(level = level, flicker = flicker, sparkRise = sparkRise, isDark = isDark)
        }
    }
}

private fun DrawScope.drawExtinguishedEmber(
    isDark: Boolean,
    pulse: Float
) {
    val w = size.width
    val h = size.height

    val ashDark = if (isDark) Color(0xFF3F434C) else Color(0xFF64748B)
    val ashLight = if (isDark) Color(0xFF5A606D) else Color(0xFF94A3B8)
    val coalCrack = Color(0xFF9A3412).copy(alpha = 0.35f + 0.18f * pulse)

    // Faint dormant ash smoke curl above the extinguished coals
    val smokeColor = if (isDark) Color.White.copy(alpha = 0.14f + 0.05f * pulse) else Color(0xFF64748B).copy(alpha = 0.22f)
    val smokePath = Path().apply {
        moveTo(w * 0.46f, h * 0.56f)
        cubicTo(
            w * 0.34f, h * 0.44f,
            w * 0.62f, h * 0.32f,
            w * 0.48f, h * 0.18f
        )
    }
    drawPath(
        path = smokePath,
        color = smokeColor,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = w * 0.065f,
            cap = StrokeCap.Round
        )
    )

    // Subtle dim residual warmth under the coal bed
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFEA580C).copy(alpha = 0.16f + 0.06f * pulse),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.72f),
            radius = w * 0.34f
        ),
        radius = w * 0.34f,
        center = Offset(w * 0.5f, h * 0.72f)
    )

    // Crossed extinguished charcoal embers
    rotate(degrees = -14f, pivot = Offset(w * 0.48f, h * 0.74f)) {
        drawRoundRect(
            color = ashDark,
            topLeft = Offset(w * 0.16f, h * 0.66f),
            size = Size(w * 0.64f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
    }
    rotate(degrees = 15f, pivot = Offset(w * 0.52f, h * 0.74f)) {
        drawRoundRect(
            color = ashLight,
            topLeft = Offset(w * 0.20f, h * 0.66f),
            size = Size(w * 0.62f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.075f, w * 0.075f)
        )
    }

    // Center dormant coal mound with faint ember crack
    drawOval(
        color = ashDark,
        topLeft = Offset(w * 0.32f, h * 0.57f),
        size = Size(w * 0.36f, h * 0.22f)
    )
    drawLine(
        color = coalCrack,
        start = Offset(w * 0.40f, h * 0.69f),
        end = Offset(w * 0.58f, h * 0.67f),
        strokeWidth = w * 0.045f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawActiveFlame(
    level: Int,
    flicker: Float,
    sparkRise: Float,
    isDark: Boolean
) {
    val w = size.width
    val h = size.height

    val sway = (flicker - 0.5f) * w * (0.04f + level * 0.012f)
    val heightScale = when (level) {
        1 -> 0.72f
        2 -> 0.86f
        3 -> 0.96f
        else -> 1.0f
    } + flicker * 0.04f

    // Radiant background halo intensifying per flame level
    val auraAlpha = when (level) {
        1 -> 0.18f
        2 -> 0.26f
        3 -> 0.34f
        else -> 0.42f
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFF8A00).copy(alpha = if (isDark) auraAlpha else auraAlpha * 0.85f),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.58f),
            radius = w * (0.40f + level * 0.05f)
        ),
        radius = w * (0.40f + level * 0.05f),
        center = Offset(w * 0.5f, h * 0.58f)
    )

    val bottomY = h * 0.88f
    val topY = bottomY - (h * 0.76f * heightScale)

    // Level 3 & 4 side roaring tongues
    if (level >= 3) {
        val leftTongue = Path().apply {
            moveTo(w * 0.36f, bottomY - h * 0.08f)
            cubicTo(
                w * 0.12f, h * 0.66f,
                w * 0.15f, h * 0.44f,
                w * 0.28f - sway * 0.7f, h * (0.34f - (level - 3) * 0.05f)
            )
            cubicTo(
                w * 0.28f, h * 0.48f,
                w * 0.38f, h * 0.56f,
                w * 0.44f, bottomY - h * 0.06f
            )
            close()
        }
        drawPath(
            path = leftTongue,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFA726), Color(0xFFE53935)),
                startY = h * 0.28f,
                endY = bottomY
            )
        )

        val rightTongue = Path().apply {
            moveTo(w * 0.64f, bottomY - h * 0.08f)
            cubicTo(
                w * 0.88f, h * 0.68f,
                w * 0.85f, h * 0.46f,
                w * 0.72f + sway * 0.8f, h * (0.36f - (level - 3) * 0.05f)
            )
            cubicTo(
                w * 0.72f, h * 0.50f,
                w * 0.62f, h * 0.58f,
                w * 0.56f, bottomY - h * 0.06f
            )
            close()
        }
        drawPath(
            path = rightTongue,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFA726), Color(0xFFE53935)),
                startY = h * 0.30f,
                endY = bottomY
            )
        )
    }

    // Outer sculpted main flame silhouette
    val widthFactor = when (level) {
        1 -> 0.23f
        2 -> 0.29f
        3 -> 0.32f
        else -> 0.35f
    }
    val outerPath = Path().apply {
        moveTo(w * 0.50f, bottomY)
        cubicTo(
            w * (0.50f - widthFactor * 1.15f), bottomY,
            w * (0.50f - widthFactor * 1.05f), bottomY - h * 0.34f * heightScale,
            w * 0.50f + sway, topY
        )
        cubicTo(
            w * (0.50f + widthFactor * 0.55f), topY + h * 0.22f * heightScale,
            w * (0.50f + widthFactor * 1.18f), bottomY - h * 0.32f * heightScale,
            w * 0.50f, bottomY
        )
        close()
    }
    val outerBrush = when (level) {
        1 -> Brush.verticalGradient(listOf(Color(0xFFFFB74D), Color(0xFFE54D38)), startY = topY, endY = bottomY)
        2 -> Brush.verticalGradient(listOf(Color(0xFFFFCA28), Color(0xFFFF7043), Color(0xFFD84315)), startY = topY, endY = bottomY)
        3 -> Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF6D00), Color(0xFFD50000)), startY = topY, endY = bottomY)
        else -> Brush.verticalGradient(listOf(Color(0xFFFFEE58), Color(0xFFFF7043), Color(0xFFC62828)), startY = topY, endY = bottomY)
    }
    drawPath(path = outerPath, brush = outerBrush)

    // Inner bright flame core
    val coreScale = 0.54f + level * 0.03f
    val coreBottom = bottomY - h * 0.05f
    val coreTop = bottomY - (bottomY - topY) * coreScale
    val innerPath = Path().apply {
        moveTo(w * 0.50f, coreBottom)
        cubicTo(
            w * (0.50f - widthFactor * 0.58f), coreBottom,
            w * (0.50f - widthFactor * 0.52f), coreBottom - (coreBottom - coreTop) * 0.45f,
            w * 0.50f + sway * 0.6f, coreTop
        )
        cubicTo(
            w * (0.50f + widthFactor * 0.48f), coreBottom - (coreBottom - coreTop) * 0.45f,
            w * (0.50f + widthFactor * 0.58f), coreBottom,
            w * 0.50f, coreBottom
        )
        close()
    }
    val innerBrush = if (level >= 4) {
        // Level 4 hottest blue-white-gold core
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFE082), Color(0xFFBAE6FD)),
            startY = coreTop,
            endY = coreBottom
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F)),
            startY = coreTop,
            endY = coreBottom
        )
    }
    drawPath(path = innerPath, brush = innerBrush)

    // Rising ember sparks for Level 2+
    if (level >= 2) {
        val sparkAlpha = (1f - sparkRise).coerceIn(0f, 1f) * 0.85f
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = sparkAlpha),
            radius = w * 0.032f,
            center = Offset(w * 0.36f + sway, topY - h * 0.04f - h * 0.14f * sparkRise)
        )
    }
    if (level >= 3) {
        val spark2Phase = (sparkRise + 0.5f) % 1f
        val spark2Alpha = (1f - spark2Phase).coerceIn(0f, 1f) * 0.85f
        drawCircle(
            color = Color(0xFFFF9100).copy(alpha = spark2Alpha),
            radius = w * 0.028f,
            center = Offset(w * 0.65f - sway, topY - h * 0.02f - h * 0.16f * spark2Phase)
        )
    }
    if (level >= 4) {
        val spark3Phase = (sparkRise + 0.25f) % 1f
        val spark3Alpha = (1f - spark3Phase).coerceIn(0f, 1f) * 0.9f
        drawCircle(
            color = Color(0xFFE0F2FE).copy(alpha = spark3Alpha),
            radius = w * 0.025f,
            center = Offset(w * 0.52f + sway * 1.2f, topY - h * 0.06f - h * 0.18f * spark3Phase)
        )
    }
}

val VECTOR_AVATAR_KEYS = listOf(
    "zen", "fox", "bolt", "rocket", "owl", "target",
    "crown", "lion", "coffee", "code", "palette", "star"
)

fun resolveAvatarVectorIcon(key: String): ImageVector = when (key) {
    "zen", "🧘" -> Icons.Rounded.SelfImprovement
    "fox", "🦊" -> Icons.Rounded.Pets
    "bolt", "⚡" -> Icons.Rounded.Bolt
    "rocket", "🚀" -> Icons.Rounded.RocketLaunch
    "owl", "🦉" -> Icons.Rounded.Visibility
    "target", "🎯" -> Icons.Rounded.TrackChanges
    "crown", "👑" -> Icons.Rounded.WorkspacePremium
    "lion", "🦁" -> Icons.Rounded.LocalFireDepartment
    "coffee", "☕" -> Icons.Rounded.Coffee
    "code", "💻" -> Icons.Rounded.Terminal
    "palette", "🎨" -> Icons.Rounded.Palette
    "star", "🌟" -> Icons.Rounded.AutoAwesome
    else -> Icons.Rounded.SelfImprovement
}

@Composable
fun ProfileAvatarVectorIcon(
    avatarKey: String,
    tint: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = resolveAvatarVectorIcon(avatarKey),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size)
    )
}

fun resolveLevelVectorIcon(level: Int): ImageVector = when (level) {
    1 -> Icons.Rounded.Spa
    2 -> Icons.Rounded.Bolt
    3 -> Icons.Rounded.LocalFireDepartment
    4 -> Icons.Rounded.HourglassTop
    else -> Icons.Rounded.WorkspacePremium
}

@Composable
fun ProfileLevelVectorIcon(
    level: Int,
    tint: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = resolveLevelVectorIcon(level),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size)
    )
}
