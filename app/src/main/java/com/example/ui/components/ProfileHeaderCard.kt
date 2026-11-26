package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BreakSage
import com.example.util.AppStrings
import com.example.util.ProfileLevelHelper
import com.example.util.ProfileLevelProgress
import com.example.util.bounceClick
import java.io.File
import java.io.FileOutputStream

val AVAILABLE_AVATARS = VECTOR_AVATAR_KEYS

val AVATAR_BG_COLORS = listOf(
    Color(0xFFE54D38), // Focus Red
    Color(0xFF3E8E7E), // Sage Green
    Color(0xFF3B82F6), // Sapphire Blue
    Color(0xFFFFB300), // Amber Gold
    Color(0xFF8B5CF6), // Royal Purple
    Color(0xFFEC4899)  // Rose Pink
)

@Composable
fun ProfileHeaderCard(
    userProfile: UserProfile,
    totalSessions: Int,
    unlockedBadges: Int,
    totalBadges: Int,
    totalFocusMinutes: Int,
    streakDays: Int,
    strings: AppStrings,
    onSaveProfile: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showLevelsDialog by remember { mutableStateOf(false) }

    val levelProgress = remember(totalSessions, strings) {
        ProfileLevelHelper.computeProgress(totalSessions, strings)
    }
    val currentTier = levelProgress.currentTier

    val selectedBgColor = AVATAR_BG_COLORS.getOrElse(userProfile.avatarColorIndex) {
        MaterialTheme.colorScheme.primary
    }

    val hasCustomPhoto = !userProfile.customPhotoPath.isNullOrBlank() && File(userProfile.customPhotoPath).exists()

    val profileCardShape = RoundedCornerShape(22.dp)
    Card(
        shape = profileCardShape,
        modifier = modifier
            .fillMaxWidth()
            .glassMorphism(
                shape = profileCardShape,
                elevated = true,
                borderWidth = 1.2.dp,
                shadowElevation = 12.dp
            )
            .testTag("user_profile_card"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Compact edit pencil icon pinned cleanly in the exact top-right corner
            val editInteraction = remember { MutableInteractionSource() }
            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = editInteraction,
                        indication = null
                    ) { showEditDialog = true }
                    .padding(top = 10.dp, end = 10.dp)
                    .testTag("edit_profile_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .glassMorphism(
                            shape = CircleShape,
                            elevated = true,
                            borderWidth = 1.dp,
                            shadowElevation = 5.dp,
                            interactionSource = editInteraction,
                            pressScale = 0.88f,
                            pressDepth = 2.dp
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = strings.profileEditButton,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Profile Info Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with border & background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(selectedBgColor.copy(alpha = 0.22f))
                        .border(
                            2.dp,
                            if (isGlassDarkTheme()) Color.White.copy(alpha = 0.42f) else Color.White.copy(alpha = 0.92f),
                            CircleShape
                        )
                        .clickable { showEditDialog = true }
                        .testTag("profile_avatar_badge")
                ) {
                    if (hasCustomPhoto) {
                        AsyncImage(
                            model = File(userProfile.customPhotoPath!!),
                            contentDescription = userProfile.nickname,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        ProfileAvatarVectorIcon(
                            avatarKey = userProfile.avatarEmoji,
                            tint = selectedBgColor,
                            size = 34.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 34.dp)
                    ) {
                        Text(
                            text = userProfile.nickname,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Clickable Level Badge & Progress Bar (opens sequential level tiers)
                    val levelBadgeShape = RoundedCornerShape(12.dp)
                    Surface(
                        shape = levelBadgeShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassMorphism(
                                shape = levelBadgeShape,
                                borderWidth = 1.dp,
                                shadowElevation = 4.dp
                            )
                            .bounceClick { showLevelsDialog = true }
                            .testTag("profile_level_badge")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ProfileLevelVectorIcon(
                                        level = currentTier.level,
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 14.dp
                                    )
                                    Text(
                                        text = "${strings.profileLevelLabel} ${currentTier.level} • ${currentTier.title}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = strings.profileLevelTapHint,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            LinearProgressIndicator(
                                progress = { levelProgress.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            val hintText = if (levelProgress.nextTier != null) {
                                "$totalSessions / ${levelProgress.nextTier.minSessions} ${strings.targetTypeSessions} • ${strings.profileLevelTapHint}"
                            } else {
                                "$totalSessions ${strings.targetTypeSessions} • ${strings.profileLevelTapHint}"
                            }
                            Text(
                                text = hintText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Grid Metric Stat Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileMetricPill(
                    value = "$totalSessions",
                    label = strings.profileSequencesStat,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                ProfileMetricPill(
                    value = "$unlockedBadges / $totalBadges",
                    label = strings.profileAchievementsStat,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val formattedTime = if (totalFocusMinutes >= 60) {
                    "${totalFocusMinutes / 60} ${strings.hoursShort} ${totalFocusMinutes % 60} ${strings.minutesShort}"
                } else {
                    "$totalFocusMinutes ${strings.minutesShort}"
                }

                ProfileMetricPill(
                    value = formattedTime,
                    label = strings.profileTotalFocusStat,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = BreakSage,
                        modifier = Modifier.size(20.dp)
                    )
                }
                ProfileMetricPill(
                    value = "$streakDays ${strings.statsStreak}",
                    label = strings.profileStreakStat,
                    accentColor = if (streakDays > 0) AmberAccent else null,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("profile_streak_pill")
                ) {
                    StreakFlameVectorIcon(
                        streakDays = streakDays,
                        size = 24.dp
                    )
                }
            }
        }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentProfile = userProfile,
            strings = strings,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onSaveProfile(updated)
                showEditDialog = false
            }
        )
    }

    if (showLevelsDialog) {
        ProfileLevelsModal(
            levelProgress = levelProgress,
            strings = strings,
            onDismiss = { showLevelsDialog = false }
        )
    }
}

@Composable
fun ProfileLevelsModal(
    levelProgress: ProfileLevelProgress,
    strings: AppStrings,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("profile_levels_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.profileLevelsTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.profileLevelsSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("close_levels_dialog_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.settingsDismiss
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Step-by-step vertical tier ladder (1 -> 5)
                levelProgress.allTiers.forEachIndexed { index, tier ->
                    val isCompleted = levelProgress.currentTier.level > tier.level
                    val isCurrent = levelProgress.currentTier.level == tier.level
                    val isLocked = levelProgress.currentTier.level < tier.level
                    val isLast = index == levelProgress.allTiers.lastIndex

                    val stepColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isCompleted -> BreakSage
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_level_tier_${tier.level}"),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left timeline indicator column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(34.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isCompleted -> BreakSage.copy(alpha = 0.22f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.2.dp,
                                        color = stepColor,
                                        shape = CircleShape
                                    )
                            ) {
                                when {
                                    isCompleted -> Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = BreakSage,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    isLocked -> Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    else -> Text(
                                        text = "${tier.level}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }

                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(if (isCurrent) 42.dp else 26.dp)
                                        .background(
                                            if (isCompleted) BreakSage.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Right tier card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isCurrent -> glassCardFillColor(
                                    accentColor = MaterialTheme.colorScheme.primary,
                                    accentAlpha = 0.08f,
                                    elevated = true
                                )
                                isCompleted -> glassCardFillColor(
                                    accentColor = BreakSage,
                                    accentAlpha = 0.06f
                                )
                                else -> glassCardFillColor()
                            },
                            border = glassBorderStroke(width = if (isCurrent) 1.2.dp else 0.9.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (isLast) 0.dp else 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        ProfileLevelVectorIcon(
                                            level = tier.level,
                                            tint = when {
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                isCompleted -> BreakSage
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            size = 16.dp
                                        )
                                        Text(
                                            text = "${strings.profileLevelLabel} ${tier.level} • ${tier.title}",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Requirement or Status pill
                                    val reqLabel = when {
                                        isCurrent -> strings.profileLevelCurrentBadge
                                        isCompleted -> strings.profileLevelCompletedBadge
                                        else -> try {
                                            String.format(strings.profileLevelRequirementFormat, tier.minSessions)
                                        } catch (_: Exception) {
                                            "${tier.minSessions}+"
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    isCompleted -> BreakSage.copy(alpha = 0.2f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = reqLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = when {
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                isCompleted -> BreakSage
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { levelProgress.progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val nextText = if (levelProgress.nextTier != null) {
                                        try {
                                            String.format(
                                                strings.profileLevelNextHint,
                                                levelProgress.remainingSessionsToNext
                                            )
                                        } catch (_: Exception) {
                                            "${levelProgress.remainingSessionsToNext} ${strings.targetTypeSessions}"
                                        }
                                    } else {
                                        strings.profileLevelMaxReached
                                    }
                                    Text(
                                        text = nextText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val okInteraction = remember { MutableInteractionSource() }
            val okShape = RoundedCornerShape(12.dp)
            Button(
                onClick = onDismiss,
                interactionSource = okInteraction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                shape = okShape,
                modifier = Modifier
                    .coloredGlassButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = okShape,
                        interactionSource = okInteraction,
                        shadowElevation = 8.dp
                    )
                    .testTag("levels_dialog_ok_button")
            ) {
                Text(strings.settingsDismiss, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ProfileMetricPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    iconContent: @Composable () -> Unit
) {
    val pillShape = RoundedCornerShape(14.dp)
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .glassMorphism(
                shape = pillShape,
                accentColor = accentColor,
                accentAlpha = if (accentColor != null) 0.06f else 0f,
                borderWidth = 1.dp,
                shadowElevation = 5.dp
            ),
        shape = pillShape,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(28.dp)
            ) {
                iconContent()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileDialog(
    currentProfile: UserProfile,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    var nicknameText by remember { mutableStateOf(currentProfile.nickname) }
    var selectedAvatar by remember { mutableStateOf(currentProfile.avatarEmoji) }
    var selectedColorIndex by remember { mutableIntStateOf(currentProfile.avatarColorIndex) }

    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isPhotoCleared by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedPhotoUri = uri
            isPhotoCleared = false
        }
    }

    val activeColor = AVATAR_BG_COLORS.getOrElse(selectedColorIndex) {
        MaterialTheme.colorScheme.primary
    }

    val currentHasPhoto = !isPhotoCleared && (pickedPhotoUri != null || (!currentProfile.customPhotoPath.isNullOrBlank() && File(currentProfile.customPhotoPath).exists()))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.profileEditDialogTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Avatar Preview
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(activeColor.copy(alpha = 0.25f))
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    if (pickedPhotoUri != null) {
                        AsyncImage(
                            model = pickedPhotoUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else if (!isPhotoCleared && !currentProfile.customPhotoPath.isNullOrBlank() && File(currentProfile.customPhotoPath).exists()) {
                        AsyncImage(
                            model = File(currentProfile.customPhotoPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        ProfileAvatarVectorIcon(
                            avatarKey = selectedAvatar,
                            tint = activeColor,
                            size = 40.dp
                        )
                    }
                }

                // Photo actions: Pick from gallery or remove photo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pickPhotoInteraction = remember { MutableInteractionSource() }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        interactionSource = pickPhotoInteraction,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .floatingPressEffect(
                                interactionSource = pickPhotoInteraction,
                                pressScale = 0.95f,
                                pressDepth = 2.dp
                            )
                            .testTag("pick_gallery_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.profilePickPhotoFromGallery, fontSize = 12.sp, maxLines = 1)
                    }

                    if (currentHasPhoto) {
                        IconButton(
                            onClick = {
                                pickedPhotoUri = null
                                isPhotoCleared = true
                            },
                            modifier = Modifier.testTag("remove_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = strings.profileRemovePhoto,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Nickname Input
                OutlinedTextField(
                    value = nicknameText,
                    onValueChange = { if (it.length <= 25) nicknameText = it },
                    label = { Text(strings.profileNicknameLabel) },
                    placeholder = { Text(strings.profileNicknamePlaceholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nickname_text_input")
                )

                // Avatar Emoji Selector (used as fallback or when no custom photo is set)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.profileSelectAvatarLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVAILABLE_AVATARS.forEach { avatarKey ->
                            val isSelected = resolveAvatarVectorIcon(avatarKey) == resolveAvatarVectorIcon(selectedAvatar) && !currentHasPhoto
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        selectedAvatar = avatarKey
                                        isPhotoCleared = true
                                        pickedPhotoUri = null
                                    }
                            ) {
                                ProfileAvatarVectorIcon(
                                    avatarKey = avatarKey,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    size = 22.dp
                                )
                            }
                        }
                    }
                }

                // Color Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.profileSelectColorLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AVATAR_BG_COLORS.forEachIndexed { index, color ->
                            val isSelected = index == selectedColorIndex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (isSelected) 2.5.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColorIndex = index }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val saveProfileInteraction = remember { MutableInteractionSource() }
            val saveProfileShape = RoundedCornerShape(12.dp)
            Button(
                onClick = {
                    val finalNickname = nicknameText.trim().ifEmpty { strings.profileDefaultNickname }
                    var finalPhotoPath = currentProfile.customPhotoPath

                    if (isPhotoCleared) {
                        if (currentProfile.customPhotoPath != null) {
                            try {
                                File(currentProfile.customPhotoPath).delete()
                            } catch (_: Exception) {}
                        }
                        finalPhotoPath = null
                    } else if (pickedPhotoUri != null) {
                        try {
                            val inputStream = context.contentResolver.openInputStream(pickedPhotoUri!!)
                            val file = File(context.filesDir, "user_profile_avatar.jpg")
                            val outputStream = FileOutputStream(file)
                            inputStream?.use { input ->
                                outputStream.use { output ->
                                    input.copyTo(output)
                                }
                            }
                            finalPhotoPath = file.absolutePath
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    onSave(
                        UserProfile(
                            nickname = finalNickname,
                            avatarEmoji = selectedAvatar,
                            avatarColorIndex = selectedColorIndex,
                            customPhotoPath = finalPhotoPath
                        )
                    )
                },
                interactionSource = saveProfileInteraction,
                shape = saveProfileShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier
                    .coloredGlassButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = saveProfileShape,
                        interactionSource = saveProfileInteraction,
                        shadowElevation = 8.dp
                    )
                    .testTag("save_profile_button")
            ) {
                Text(strings.settingsSave, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}
