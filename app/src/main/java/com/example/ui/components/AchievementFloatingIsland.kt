package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BadgeItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BreakSage
import com.example.ui.theme.FocusRed
import com.example.util.AppStrings

@Composable
fun AchievementFloatingIslandOverlay(
    badge: BadgeItem?,
    strings: AppStrings,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Soft backdrop scrim (tap anywhere outside to dismiss)
        AnimatedVisibility(
            visible = badge != null,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .testTag("floating_island_scrim")
            )
        }

        // Dynamic Floating Island pill container anchored dead-center on screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = badge != null,
                enter = fadeIn(tween(220)) +
                    scaleIn(
                        initialScale = 0.86f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                exit = fadeOut(tween(160)) +
                    scaleOut(targetScale = 0.90f, animationSpec = tween(160))
            ) {
                if (badge != null) {
                    FloatingIslandCardContent(
                        badge = badge,
                        strings = strings,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingIslandCardContent(
    badge: BadgeItem,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    val targetFraction = if (badge.maxProgress > 0) {
        (badge.currentProgress.toFloat() / badge.maxProgress.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "floating_island_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp)
            .glassMorphism(
                shape = RoundedCornerShape(30.dp),
                accentColor = if (badge.isUnlocked) AmberAccent else null,
                accentAlpha = if (badge.isUnlocked) 0.05f else 0f,
                elevated = true,
                borderWidth = 1.2.dp,
                shadowElevation = 14.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // absorb clicks inside the island
            )
            .testTag("achievement_floating_island"),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Top Island Drag/Pill Bar & Close Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f))
                )

                val closeIslandInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = onDismiss,
                    interactionSource = closeIslandInteraction,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp)
                        .testTag("floating_island_close_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .glassMorphism(
                                shape = CircleShape,
                                elevated = true,
                                borderWidth = 1.dp,
                                shadowElevation = 5.dp,
                                interactionSource = closeIslandInteraction,
                                pressScale = 0.90f,
                                pressDepth = 2.dp
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.badgeCloseDesc,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Main Hero Row: Large Badge Emblem (or shadow if locked) + Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (badge.isUnlocked) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        AmberAccent.copy(alpha = 0.18f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AchievementBadgeEmblem(
                        badgeId = badge.id,
                        isUnlocked = badge.isUnlocked,
                        size = 76.dp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Status Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (badge.isUnlocked) BreakSage.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (badge.isUnlocked) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (badge.isUnlocked) strings.badgeUnlockedLabel else strings.badgeLockedLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (badge.isUnlocked) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Capsule Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassMorphism(
                        shape = RoundedCornerShape(18.dp),
                        borderWidth = 0.9.dp,
                        shadowElevation = 0.dp
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.badgeProgressLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${badge.currentProgress} / ${badge.maxProgress} (${(targetFraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (badge.isUnlocked) BreakSage else AmberAccent
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (badge.isUnlocked) BreakSage else AmberAccent,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (badge.isUnlocked) strings.badgeEarnedHint else strings.badgeLockedHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
