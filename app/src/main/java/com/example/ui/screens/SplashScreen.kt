package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberAccent
import com.example.util.AppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppIntroSplashScreen(
    strings: AppStrings,
    onAnimationFinished: () -> Unit
) {
    val density = LocalDensity.current
    val slideStartPx = with(density) { 24.dp.toPx() }

    // 1. Warm dark ambient amber glow reveal
    val ambientGlowAlpha = remember { Animatable(0f) }

    // 2. Logo fades in from slight blur & scales gently into place
    val logoAlpha = remember { Animatable(0f) }
    val logoBlur = remember { Animatable(14f) }
    val logoScale = remember { Animatable(0.92f) }
    val wordmarkAlpha = remember { Animatable(0f) }

    // 3. Staggered glassmorphism cards (fade + slide-up with restrained spring)
    val card1Alpha = remember { Animatable(0f) }
    val card1OffsetY = remember { Animatable(slideStartPx) }
    val card2Alpha = remember { Animatable(0f) }
    val card2OffsetY = remember { Animatable(slideStartPx) }
    val card3Alpha = remember { Animatable(0f) }
    val card3OffsetY = remember { Animatable(slideStartPx) }

    // 4. Delicate light sweep across glass surfaces
    val lightSweepProgress = remember { Animatable(-0.5f) }

    // 5. Smooth restrained exit
    val containerAlpha = remember { Animatable(1f) }
    val containerScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Stage 1: Warm dark background & soft amber glow
        launch {
            ambientGlowAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing)
            )
        }

        // Stage 2: Logo fades in from slight blur and scales gently into place
        launch {
            delay(70)
            launch {
                logoAlpha.animateTo(1f, tween(durationMillis = 360, easing = LinearOutSlowInEasing))
            }
            launch {
                logoBlur.animateTo(0f, tween(durationMillis = 440, easing = FastOutSlowInEasing))
            }
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.92f, stiffness = 320f)
                )
            }
        }
        launch {
            delay(180)
            wordmarkAlpha.animateTo(1f, tween(durationMillis = 340, easing = LinearOutSlowInEasing))
        }

        // Stage 3: Sequential glassmorphism cards with subtle spring easing
        val cardSpring = spring<Float>(dampingRatio = 0.90f, stiffness = 360f)
        launch {
            delay(270)
            launch { card1Alpha.animateTo(1f, tween(300, easing = LinearOutSlowInEasing)) }
            card1OffsetY.animateTo(0f, cardSpring)
        }
        launch {
            delay(360)
            launch { card2Alpha.animateTo(1f, tween(300, easing = LinearOutSlowInEasing)) }
            card2OffsetY.animateTo(0f, cardSpring)
        }
        launch {
            delay(450)
            launch { card3Alpha.animateTo(1f, tween(300, easing = LinearOutSlowInEasing)) }
            card3OffsetY.animateTo(0f, cardSpring)
        }

        // Stage 4: Delicate light sweep across the glass surfaces
        launch {
            delay(740)
            lightSweepProgress.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }

        // Stage 5: Smooth, restrained fade-out into app
        delay(1320)
        launch {
            containerScale.animateTo(1.015f, tween(240, easing = FastOutSlowInEasing))
        }
        containerAlpha.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = containerAlpha.value
                scaleX = containerScale.value
                scaleY = containerScale.value
            }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0807),
                        Color(0xFF130E0B),
                        Color(0xFF0A0807)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onAnimationFinished() }
            .testTag("app_intro_splash"),
        contentAlignment = Alignment.Center
    ) {
        // Soft amber ambient glow layer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(ambientGlowAlpha.value)
        ) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF7B00).copy(alpha = 0.20f),
                        AmberAccent.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.36f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.5f, h * 0.36f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AmberAccent.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.25f, h * 0.72f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.25f, h * 0.72f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(horizontal = 28.dp)
        ) {
            // Logo badge fading in from slight blur and scaling gently into place
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = logoAlpha.value
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
                    .blur(radius = logoBlur.value.dp)
            ) {
                val logoShape = RoundedCornerShape(24.dp)
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(logoShape)
                        .border(
                            BorderStroke(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD8A8).copy(alpha = 0.42f),
                                        Color.White.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            ),
                            shape = logoShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.focus_uploaded_logo),
                        contentDescription = strings.appTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Delicate light sweep reflection across logo surface
                    GlassLightSweepOverlay(
                        progress = lightSweepProgress.value,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Brand wordmark & subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(wordmarkAlpha.value)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.8.sp,
                            fontSize = 28.sp
                        ),
                        color = Color(0xFFF6EFEA)
                    )
                    Text(
                        text = "Flow",
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFF7B00), AmberAccent)
                            ),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 28.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.splashSubtitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFB5ABA4)
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Sequential staggered Glassmorphism cards
            SplashGlassCard(
                icon = Icons.Rounded.Timer,
                label = strings.modeFocus,
                accentColor = Color(0xFFFF7B00),
                alpha = card1Alpha.value,
                offsetY = card1OffsetY.value,
                sweepProgress = lightSweepProgress.value
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SplashGlassCard(
                    icon = Icons.Rounded.CheckCircleOutline,
                    label = strings.tabTasks,
                    accentColor = AmberAccent,
                    alpha = card2Alpha.value,
                    offsetY = card2OffsetY.value,
                    sweepProgress = lightSweepProgress.value,
                    modifier = Modifier.weight(1f)
                )
                SplashGlassCard(
                    icon = Icons.Rounded.AutoGraph,
                    label = strings.tabStats,
                    accentColor = Color(0xFFFF9E44),
                    alpha = card3Alpha.value,
                    offsetY = card3OffsetY.value,
                    sweepProgress = lightSweepProgress.value,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SplashGlassCard(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    alpha: Float,
    offsetY: Float,
    sweepProgress: Float,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = offsetY
            }
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF281F1A).copy(alpha = 0.62f),
                        Color(0xFF191412).copy(alpha = 0.48f)
                    )
                )
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            accentColor.copy(alpha = 0.14f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                ),
                shape = cardShape
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFFF3ECE7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.12f)
                                )
                            )
                        )
                )
            }
        }

        GlassLightSweepOverlay(
            progress = sweepProgress,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Composable
private fun GlassLightSweepOverlay(
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (progress <= -0.49f || progress >= 1.49f) return

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bandWidth = w * 0.45f
        val startX = (progress * (w + bandWidth)) - bandWidth

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFFFE0B2).copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.18f),
                    Color(0xFFFFE0B2).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + bandWidth, h)
            )
        )
    }
}
