package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom vector badge emblems for each achievement ID.
 * When [isUnlocked] is false, ONLY the dark translucent shadow silhouette of that badge's
 * distinctive emblem shape is drawn (no bright motif, no lock icon).
 */
@Composable
fun AchievementBadgeEmblem(
    badgeId: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val silhouetteBase = MaterialTheme.colorScheme.onSurface
    val shadowTint = Color.Black

    Box(
        modifier = modifier
            .size(size)
            .testTag("badge_emblem_${badgeId}_${if (isUnlocked) "unlocked" else "locked"}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = minOf(w, h) * 0.44f

            val framePath = createBadgeFramePath(badgeId, cx, cy, r)

            if (!isUnlocked) {
                // Locked: render ONLY the clean shadow silhouette of the badge shape
                translate(left = 0f, top = 2.5.dp.toPx()) {
                    drawPath(
                        path = framePath,
                        color = shadowTint.copy(alpha = 0.24f)
                    )
                }
                drawPath(
                    path = framePath,
                    color = silhouetteBase.copy(alpha = 0.14f)
                )
                drawPath(
                    path = framePath,
                    color = silhouetteBase.copy(alpha = 0.08f),
                    style = Stroke(width = 1.2.dp.toPx(), join = StrokeJoin.Round)
                )
            } else {
                val (primaryStart, primaryEnd, rimColor) = badgePalette(badgeId)

                // Soft ambient outer shadow
                translate(left = 0f, top = 2.dp.toPx()) {
                    drawPath(
                        path = framePath,
                        color = shadowTint.copy(alpha = 0.28f)
                    )
                }

                // Rich gradient body
                drawPath(
                    path = framePath,
                    brush = Brush.linearGradient(
                        colors = listOf(primaryStart, primaryEnd),
                        start = Offset(cx - r, cy - r),
                        end = Offset(cx + r, cy + r)
                    )
                )

                // Crisp metallic rim
                drawPath(
                    path = framePath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.72f),
                            rimColor,
                            rimColor.copy(alpha = 0.65f)
                        ),
                        start = Offset(cx - r, cy - r),
                        end = Offset(cx + r, cy + r)
                    ),
                    style = Stroke(width = 2.2.dp.toPx(), join = StrokeJoin.Round)
                )

                // Subtle upper sheen highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(cx - r * 0.28f, cy - r * 0.34f),
                        radius = r * 0.85f
                    ),
                    radius = r * 0.72f,
                    center = Offset(cx, cy)
                )

                // Bespoke central vector emblem artwork
                drawBadgeMotif(badgeId = badgeId, cx = cx, cy = cy, r = r)
            }
        }
    }
}

private data class BadgePalette(
    val start: Color,
    val end: Color,
    val rim: Color
)

private fun badgePalette(badgeId: String): BadgePalette = when (badgeId) {
    "FIRST_STEP" -> BadgePalette(
        start = Color(0xFFFF7A59),
        end = Color(0xFFC93B2B),
        rim = Color(0xFFFFD180)
    )
    "FOCUS_MASTER" -> BadgePalette(
        start = Color(0xFFFF9E00),
        end = Color(0xFFD84315),
        rim = Color(0xFFFFE082)
    )
    "MARATHON" -> BadgePalette(
        start = Color(0xFF38BDF8),
        end = Color(0xFF1D4ED8),
        rim = Color(0xFFBAE6FD)
    )
    "TASK_HUNTER" -> BadgePalette(
        start = Color(0xFF34D399),
        end = Color(0xFF047857),
        rim = Color(0xFFA7F3D0)
    )
    "STREAK_WARRIOR" -> BadgePalette(
        start = Color(0xFFFBBF24),
        end = Color(0xFFDB2777),
        rim = Color(0xFFFDE68A)
    )
    else -> BadgePalette( // ZEN_MIND
        start = Color(0xFF818CF8),
        end = Color(0xFF0D9488),
        rim = Color(0xFFC7D2FE)
    )
}

private fun createBadgeFramePath(
    badgeId: String,
    cx: Float,
    cy: Float,
    r: Float
): Path = when (badgeId) {
    "FIRST_STEP" -> {
        // 4-point Compass Crest Medallion
        createStarBurstPath(cx, cy, outerR = r * 1.04f, innerR = r * 0.86f, points = 8)
    }
    "FOCUS_MASTER" -> {
        // Pointed Hexagonal Shield
        createPolygonPath(cx, cy, r * 1.02f, sides = 6, rotationDeg = -90f)
    }
    "MARATHON" -> {
        // Sleek Octagonal Speed Crest
        createPolygonPath(cx, cy, r * 1.02f, sides = 8, rotationDeg = -67.5f)
    }
    "TASK_HUNTER" -> {
        // Classic Heraldic Heater Shield
        Path().apply {
            moveTo(cx, cy - r * 0.98f)
            lineTo(cx + r * 0.88f, cy - r * 0.72f)
            cubicTo(
                cx + r * 0.92f, cy + r * 0.12f,
                cx + r * 0.56f, cy + r * 0.74f,
                cx, cy + r * 1.02f
            )
            cubicTo(
                cx - r * 0.56f, cy + r * 0.74f,
                cx - r * 0.92f, cy + r * 0.12f,
                cx - r * 0.88f, cy - r * 0.72f
            )
            close()
        }
    }
    "STREAK_WARRIOR" -> {
        // 12-point Royal Sunburst Seal
        createStarBurstPath(cx, cy, outerR = r * 1.05f, innerR = r * 0.82f, points = 12)
    }
    else -> {
        // ZEN_MIND: Scalloped Lotus Halo Seal
        createScallopedCirclePath(cx, cy, r * 0.98f, lobes = 10)
    }
}

private fun createPolygonPath(
    cx: Float,
    cy: Float,
    radius: Float,
    sides: Int,
    rotationDeg: Float
): Path = Path().apply {
    for (i in 0 until sides) {
        val angle = (rotationDeg + i * (360f / sides)) * (PI / 180.0)
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun createStarBurstPath(
    cx: Float,
    cy: Float,
    outerR: Float,
    innerR: Float,
    points: Int
): Path = Path().apply {
    val total = points * 2
    for (i in 0 until total) {
        val rad = if (i % 2 == 0) outerR else innerR
        val angle = (-90.0 + i * (360.0 / total)) * (PI / 180.0)
        val x = cx + (rad * cos(angle)).toFloat()
        val y = cy + (rad * sin(angle)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

private fun createScallopedCirclePath(
    cx: Float,
    cy: Float,
    radius: Float,
    lobes: Int
): Path = Path().apply {
    val step = 360.0 / lobes
    for (i in 0 until lobes) {
        val startDeg = -90.0 + i * step
        val midDeg = startDeg + step * 0.5
        val endDeg = startDeg + step
        val startX = cx + (radius * 0.88f * cos(startDeg * PI / 180.0)).toFloat()
        val startY = cy + (radius * 0.88f * sin(startDeg * PI / 180.0)).toFloat()
        val ctrlX = cx + (radius * 1.12f * cos(midDeg * PI / 180.0)).toFloat()
        val ctrlY = cy + (radius * 1.12f * sin(midDeg * PI / 180.0)).toFloat()
        val endX = cx + (radius * 0.88f * cos(endDeg * PI / 180.0)).toFloat()
        val endY = cy + (radius * 0.88f * sin(endDeg * PI / 180.0)).toFloat()
        if (i == 0) moveTo(startX, startY)
        quadraticTo(ctrlX, ctrlY, endX, endY)
    }
    close()
}

private fun DrawScope.drawBadgeMotif(
    badgeId: String,
    cx: Float,
    cy: Float,
    r: Float
) {
    when (badgeId) {
        "FIRST_STEP" -> {
            // Summit Peak + Guiding North Spark
            val peak = Path().apply {
                moveTo(cx, cy - r * 0.42f)
                lineTo(cx + r * 0.46f, cy + r * 0.38f)
                lineTo(cx - r * 0.46f, cy + r * 0.38f)
                close()
            }
            drawPath(peak, color = Color.White.copy(alpha = 0.94f))
            // Snowcap accent
            val cap = Path().apply {
                moveTo(cx, cy - r * 0.42f)
                lineTo(cx + r * 0.18f, cy - r * 0.10f)
                lineTo(cx, cy - r * 0.18f)
                lineTo(cx - r * 0.18f, cy - r * 0.10f)
                close()
            }
            drawPath(cap, color = Color(0xFFFFE082))
            // Small flag/spark at peak
            drawCircle(
                color = Color(0xFFFFF8E1),
                radius = r * 0.09f,
                center = Offset(cx, cy - r * 0.56f)
            )
        }

        "FOCUS_MASTER" -> {
            // Layered Flame Emblem matching FocusFlow's fire motif
            val outerFlame = Path().apply {
                moveTo(cx + r * 0.04f, cy - r * 0.56f)
                cubicTo(
                    cx + r * 0.52f, cy - r * 0.16f,
                    cx + r * 0.54f, cy + r * 0.36f,
                    cx, cy + r * 0.54f
                )
                cubicTo(
                    cx - r * 0.54f, cy + r * 0.36f,
                    cx - r * 0.48f, cy - r * 0.18f,
                    cx + r * 0.04f, cy - r * 0.56f
                )
                close()
            }
            drawPath(outerFlame, color = Color(0xFFFFF8E1))

            val innerFlame = Path().apply {
                moveTo(cx, cy - r * 0.14f)
                cubicTo(
                    cx + r * 0.24f, cy + r * 0.08f,
                    cx + r * 0.24f, cy + r * 0.38f,
                    cx, cy + r * 0.48f
                )
                cubicTo(
                    cx - r * 0.24f, cy + r * 0.38f,
                    cx - r * 0.24f, cy + r * 0.08f,
                    cx, cy - r * 0.14f
                )
                close()
            }
            drawPath(innerFlame, color = Color(0xFFFF9100))
        }

        "MARATHON" -> {
            // Bold Electric Lightning Bolt
            val bolt = Path().apply {
                moveTo(cx + r * 0.10f, cy - r * 0.54f)
                lineTo(cx - r * 0.28f, cy + r * 0.04f)
                lineTo(cx + r * 0.02f, cy + r * 0.04f)
                lineTo(cx - r * 0.12f, cy + r * 0.54f)
                lineTo(cx + r * 0.32f, cy - r * 0.06f)
                lineTo(cx + r * 0.02f, cy - r * 0.06f)
                close()
            }
            drawPath(bolt, color = Color(0xFFFEF08A))
        }

        "TASK_HUNTER" -> {
            // Crisp Shield Checkmark Ribbon
            val check = Path().apply {
                moveTo(cx - r * 0.36f, cy + r * 0.02f)
                lineTo(cx - r * 0.10f, cy + r * 0.28f)
                lineTo(cx + r * 0.40f, cy - r * 0.26f)
            }
            drawPath(
                path = check,
                color = Color.White,
                style = Stroke(
                    width = (r * 0.22f).coerceAtLeast(3.dp.toPx()),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        "STREAK_WARRIOR" -> {
            // Crowned 5-Point Star
            val star = createStarBurstPath(
                cx = cx,
                cy = cy + r * 0.04f,
                outerR = r * 0.48f,
                innerR = r * 0.21f,
                points = 5
            )
            drawPath(star, color = Color(0xFFFFFBEB))
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = r * 0.12f,
                center = Offset(cx, cy + r * 0.04f)
            )
        }

        else -> {
            // ZEN_MIND: Symmetrical 3-Petal Lotus + Calm Water Ripple
            val centerPetal = Path().apply {
                moveTo(cx, cy - r * 0.46f)
                cubicTo(
                    cx + r * 0.22f, cy - r * 0.18f,
                    cx + r * 0.20f, cy + r * 0.16f,
                    cx, cy + r * 0.24f
                )
                cubicTo(
                    cx - r * 0.20f, cy + r * 0.16f,
                    cx - r * 0.22f, cy - r * 0.18f,
                    cx, cy - r * 0.46f
                )
                close()
            }
            val leftPetal = Path().apply {
                moveTo(cx - r * 0.44f, cy - r * 0.12f)
                cubicTo(
                    cx - r * 0.18f, cy - r * 0.12f,
                    cx - r * 0.08f, cy + r * 0.10f,
                    cx - r * 0.04f, cy + r * 0.24f
                )
                cubicTo(
                    cx - r * 0.26f, cy + r * 0.24f,
                    cx - r * 0.44f, cy + r * 0.06f,
                    cx - r * 0.44f, cy - r * 0.12f
                )
                close()
            }
            val rightPetal = Path().apply {
                moveTo(cx + r * 0.44f, cy - r * 0.12f)
                cubicTo(
                    cx + r * 0.18f, cy - r * 0.12f,
                    cx + r * 0.08f, cy + r * 0.10f,
                    cx + r * 0.04f, cy + r * 0.24f
                )
                cubicTo(
                    cx + r * 0.26f, cy + r * 0.24f,
                    cx + r * 0.44f, cy + r * 0.06f,
                    cx + r * 0.44f, cy - r * 0.12f
                )
                close()
            }
            drawPath(leftPetal, color = Color.White.copy(alpha = 0.80f))
            drawPath(rightPetal, color = Color.White.copy(alpha = 0.80f))
            drawPath(centerPetal, color = Color.White)
            // Calm water ripple arc
            drawArc(
                color = Color(0xFF99F6E4),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.46f, cy + r * 0.16f),
                size = Size(r * 0.92f, r * 0.30f),
                style = Stroke(width = (r * 0.08f).coerceAtLeast(1.6.dp.toPx()), cap = StrokeCap.Round)
            )
        }
    }
}
