package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BadgeItem
import com.example.data.model.SessionLogEntity
import com.example.ui.DayFocusStat
import com.example.ui.PomodoroViewModel
import com.example.ui.components.AchievementBadgeEmblem
import com.example.ui.components.AchievementFloatingIslandOverlay
import com.example.ui.components.ProfileHeaderCard
import com.example.ui.components.StreakFlameVectorIcon
import com.example.ui.components.glassMorphism
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BreakSage
import com.example.util.bounceClick
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val appStrings by viewModel.appStrings.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val weeklyStats = remember(allSessions) { viewModel.getWeeklyStats(allSessions) }
    val streakDays = remember(allSessions) { viewModel.getStreakDays(allSessions) }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = calendar.timeInMillis

    val todayFocusMinutes = allSessions
        .filter { it.sessionType == "FOCUS" && it.completedAt >= startOfToday }
        .sumOf { it.durationMinutes }

    val totalFocusMinutes = allSessions
        .filter { it.sessionType == "FOCUS" }
        .sumOf { it.durationMinutes }

    val todayCompletedSessions = allSessions
        .count { it.sessionType == "FOCUS" && it.completedAt >= startOfToday }

    val totalCompletedSessions = remember(allSessions) {
        allSessions.count { it.sessionType == "FOCUS" }
    }

    val unlockedBadgesCount = remember(badges) {
        badges.count { it.isUnlocked }
    }

    val completedTasksCount = allTasks.count { it.isCompleted }

    var selectedBadge by remember { mutableStateOf<BadgeItem?>(null) }
    val activeSelectedBadge = selectedBadge?.let { current ->
        badges.firstOrNull { it.id == current.id } ?: current
    }

    BackHandler(enabled = activeSelectedBadge != null) {
        selectedBadge = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen")
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header Card (Avatar, Nickname, Sequences, Badges, Level)
        item {
            ProfileHeaderCard(
                userProfile = userProfile,
                totalSessions = totalCompletedSessions,
                unlockedBadges = unlockedBadgesCount,
                totalBadges = badges.size,
                totalFocusMinutes = totalFocusMinutes,
                streakDays = streakDays,
                strings = appStrings,
                onSaveProfile = { updated ->
                    viewModel.updateUserProfile(updated)
                }
            )
        }

        // Daily 4-Flame Goal Card
        item {
            val dailyGoalShape = RoundedCornerShape(18.dp)
            val litFlames = todayCompletedSessions.coerceAtMost(4)
            Card(
                shape = dailyGoalShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassMorphism(
                        shape = dailyGoalShape,
                        elevated = true,
                        borderWidth = 1.1.dp,
                        shadowElevation = 10.dp
                    )
                    .testTag("daily_goal_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = appStrings.statsDailyGoal,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (litFlames >= 4) AmberAccent.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "$litFlames / 4",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (litFlames >= 4) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (index in 0 until 4) {
                            val isLit = index < todayCompletedSessions
                            val flameTier = if (isLit) (index + 1).coerceIn(1, 4) else 0
                            val slotShape = RoundedCornerShape(14.dp)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .glassMorphism(
                                        shape = slotShape,
                                        accentColor = if (isLit) AmberAccent else null,
                                        accentAlpha = if (isLit) 0.08f else 0f,
                                        elevated = isLit,
                                        borderWidth = 1.dp,
                                        shadowElevation = if (isLit) 6.dp else 3.dp
                                    )
                                    .testTag("daily_flame_slot_$index")
                            ) {
                                StreakFlameVectorIcon(
                                    streakDays = 0,
                                    overrideLevel = flameTier,
                                    size = if (isLit) 34.dp else 30.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary 3-Card Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = appStrings.statsTodayFocus,
                    value = formatMinutes(todayFocusMinutes),
                    icon = Icons.Default.AccessTime,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                StatMetricCard(
                    title = appStrings.statsTodaySessions,
                    value = "$todayCompletedSessions ${appStrings.targetTypeSessions}",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = AmberAccent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                StatMetricCard(
                    title = appStrings.statsCompletedTasks,
                    value = "$completedTasksCount",
                    icon = Icons.Default.CheckCircleOutline,
                    iconTint = BreakSage,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // Achievements / Badges Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appStrings.statsAchievements,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val unlockedCount = badges.count { it.isUnlocked }
                    Text(
                        text = "$unlockedCount/${badges.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(badges, key = { it.id }) { badge ->
                        BadgeCard(
                            badge = badge,
                            strings = appStrings,
                            onClick = { selectedBadge = badge }
                        )
                    }
                }
            }
        }

        // Weekly Bar Chart Card
        item {
            val chartCardShape = RoundedCornerShape(18.dp)
            Card(
                shape = chartCardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassMorphism(
                        shape = chartCardShape,
                        borderWidth = 1.1.dp,
                        shadowElevation = 10.dp
                    )
                    .testTag("weekly_chart_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = appStrings.statsWeeklyChart,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${appStrings.statsTotalLabel}: ${formatMinutes(totalFocusMinutes)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    WeeklyFocusCanvasChart(
                        stats = weeklyStats,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // Session History Header
        item {
            Text(
                text = appStrings.statsHistory,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (allSessions.isEmpty()) {
            item {
                val emptyHistoryShape = RoundedCornerShape(18.dp)
                Card(
                    shape = emptyHistoryShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassMorphism(
                            shape = emptyHistoryShape,
                            borderWidth = 1.1.dp,
                            shadowElevation = 8.dp
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = appStrings.statsNoHistory,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(allSessions.take(20), key = { it.id }) { session ->
                SessionHistoryItemCard(
                    session = session,
                    strings = appStrings,
                    languageCode = settings.language
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

        AchievementFloatingIslandOverlay(
            badge = activeSelectedBadge,
            strings = appStrings,
            onDismiss = { selectedBadge = null }
        )
    }
}

@Composable
fun BadgeCard(
    badge: BadgeItem,
    strings: com.example.util.AppStrings,
    onClick: () -> Unit
) {
    val badgeShape = RoundedCornerShape(18.dp)
    Card(
        shape = badgeShape,
        modifier = Modifier
            .width(136.dp)
            .height(156.dp)
            .glassMorphism(
                shape = badgeShape,
                accentColor = if (badge.isUnlocked) AmberAccent else null,
                accentAlpha = if (badge.isUnlocked) 0.06f else 0f,
                elevated = badge.isUnlocked,
                borderWidth = 1.1.dp,
                shadowElevation = 8.dp
            )
            .bounceClick(onClick = onClick)
            .testTag("badge_card_${badge.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AchievementBadgeEmblem(
                badgeId = badge.id,
                isUnlocked = badge.isUnlocked,
                size = 54.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (badge.isUnlocked) 0.9f else 0.72f
                    ),
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Text(
                text = if (badge.isUnlocked) strings.badgeUnlockedLabel else "${badge.currentProgress}/${badge.maxProgress}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (badge.isUnlocked) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val metricShape = RoundedCornerShape(16.dp)
    Card(
        shape = metricShape,
        modifier = modifier
            .fillMaxHeight()
            .glassMorphism(
                shape = metricShape,
                borderWidth = 1.1.dp,
                shadowElevation = 8.dp
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WeeklyFocusCanvasChart(
    stats: List<DayFocusStat>,
    modifier: Modifier = Modifier
) {
    val primaryThemeColor = MaterialTheme.colorScheme.primary
    val maxMins = (stats.maxOfOrNull { it.focusMinutes } ?: 60).coerceAtLeast(60)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = stats.size
            if (barCount == 0) return@Canvas

            val barSpacing = 16.dp.toPx()
            val availableWidthForBars = canvasWidth - (barSpacing * (barCount + 1))
            val barWidth = availableWidthForBars / barCount

            // Baseline guide line
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )

            stats.forEachIndexed { index, stat ->
                val x = barSpacing + index * (barWidth + barSpacing)
                val barFraction = (stat.focusMinutes.toFloat() / maxMins.toFloat()).coerceIn(0.04f, 1f)
                val barHeight = canvasHeight * barFraction
                val y = canvasHeight - barHeight

                val isToday = index == stats.lastIndex
                val barColor = if (isToday) primaryThemeColor else primaryThemeColor.copy(alpha = 0.45f)

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day labels below bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stats.forEachIndexed { index, stat ->
                val isToday = index == stats.lastIndex
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${stat.focusMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = stat.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SessionHistoryItemCard(
    session: SessionLogEntity,
    strings: com.example.util.AppStrings,
    languageCode: String
) {
    val locale = Locale(languageCode)
    val timeFormat = SimpleDateFormat("HH:mm", locale)
    val dateFormat = SimpleDateFormat("dd MMM", locale)
    val formattedTime = timeFormat.format(Date(session.completedAt))
    val formattedDate = dateFormat.format(Date(session.completedAt))

    val isFocus = session.sessionType == "FOCUS"
    val accentColor = if (isFocus) MaterialTheme.colorScheme.primary else BreakSage
    val typeLabel = when (session.sessionType) {
        "FOCUS" -> strings.modeFocus
        "SHORT_BREAK" -> strings.modeShortBreak
        else -> strings.modeLongBreak
    }

    val historyShape = RoundedCornerShape(14.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassMorphism(
                shape = historyShape,
                borderWidth = 1.dp,
                shadowElevation = 6.dp
            ),
        shape = historyShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (session.taskTitle != null) session.taskTitle else typeLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$formattedDate, $formattedTime • $typeLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+${session.durationMinutes}m",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
