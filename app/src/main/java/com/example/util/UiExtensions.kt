package com.example.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tactile floating-island spring press-in / press-down effect with ripple micro-interaction.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.945f,
    pressDepth: Dp = 2.5.dp,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val density = LocalDensity.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
        label = "bounceScale"
    )
    val depthPx by animateFloatAsState(
        targetValue = if (isPressed) with(density) { pressDepth.toPx() } else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 650f),
        label = "bounceDepth"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationY = depthPx
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
}

/**
 * Subtle shimmer loading / highlight sweep animation.
 */
fun Modifier.shimmerHighlight(
    highlightColor: Color = Color.White.copy(alpha = 0.08f),
    durationMillis: Int = 1800
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing)
        ),
        label = "shimmer_anim"
    )

    this.drawWithContent {
        drawContent()
        val width = size.width
        val height = size.height
        val startX = width * translateAnim
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                highlightColor,
                Color.Transparent
            ),
            start = Offset(startX, 0f),
            end = Offset(startX + width * 0.45f, height)
        )
        drawRect(brush = brush)
    }
}
