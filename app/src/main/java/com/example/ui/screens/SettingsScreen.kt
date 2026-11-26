package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PomodoroSettings
import com.example.ui.TimerMode
import com.example.ui.components.GlassAmbientBackdrop
import com.example.ui.components.coloredGlassButton
import com.example.ui.components.floatingPressEffect
import com.example.ui.components.glassBorderStroke
import com.example.ui.components.glassCardFillColor
import com.example.ui.components.glassMorphism
import com.example.ui.components.isGlassDarkTheme
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.BreakSage
import com.example.ui.theme.LongBreakBlue
import com.example.util.LocalizationManager
import com.example.util.AppLanguage
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    currentSettings: PomodoroSettings,
    onDismiss: () -> Unit,
    onLanguageSelected: ((String) -> Unit)? = null,
    onThemeSelected: ((String) -> Unit)? = null,
    onSave: (PomodoroSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLanguage by remember(currentSettings.language) { mutableStateOf(currentSettings.language) }
    var selectedThemeMode by remember(currentSettings.themeMode) { mutableStateOf(currentSettings.themeMode) }
    var focusMinutes by remember { mutableFloatStateOf(currentSettings.focusMinutes.toFloat()) }
    var shortBreakMinutes by remember { mutableFloatStateOf(currentSettings.shortBreakMinutes.toFloat()) }
    var longBreakMinutes by remember { mutableFloatStateOf(currentSettings.longBreakMinutes.toFloat()) }
    var sessionsBeforeLongBreak by remember { mutableIntStateOf(currentSettings.sessionsBeforeLongBreak) }
    var dailyGoalPomodoros by remember { mutableIntStateOf(currentSettings.dailyGoalPomodoros) }
    var autoStartNext by remember { mutableStateOf(currentSettings.autoStartNext) }
    var soundEnabled by remember { mutableStateOf(currentSettings.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(currentSettings.vibrationEnabled) }

    val strings = LocalizationManager.getStrings(selectedLanguage)

    BackHandler {
        onDismiss()
    }

    GlassAmbientBackdrop(
        mode = TimerMode.FOCUS,
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_screen"),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                SettingsGlassTopBar(
                    title = strings.settingsTitle,
                    dismissLabel = strings.settingsDismiss,
                    onDismiss = onDismiss
                )
            },
            bottomBar = {
                val bottomBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                Surface(
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = bottomBarShape,
                    shadowElevation = 0.dp,
                    modifier = Modifier.glassMorphism(
                        shape = bottomBarShape,
                        elevated = true,
                        borderWidth = 1.1.dp,
                        shadowElevation = 10.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dismissInteraction = remember { MutableInteractionSource() }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .glassMorphism(
                                    shape = RoundedCornerShape(14.dp),
                                    borderWidth = 1.dp,
                                    shadowElevation = 5.dp,
                                    interactionSource = dismissInteraction,
                                    pressScale = 0.95f,
                                    pressDepth = 2.5.dp
                                )
                                .clickable(
                                    interactionSource = dismissInteraction,
                                    indication = LocalIndication.current
                                ) { onDismiss() }
                                .testTag("dismiss_settings_button")
                        ) {
                            Text(
                                text = strings.settingsDismiss,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val saveInteraction = remember { MutableInteractionSource() }
                        val saveShape = RoundedCornerShape(14.dp)
                        Button(
                            onClick = {
                                onSave(
                                    currentSettings.copy(
                                        language = selectedLanguage,
                                        themeMode = selectedThemeMode,
                                        focusMinutes = focusMinutes.roundToInt(),
                                        shortBreakMinutes = shortBreakMinutes.roundToInt(),
                                        longBreakMinutes = longBreakMinutes.roundToInt(),
                                        sessionsBeforeLongBreak = sessionsBeforeLongBreak,
                                        dailyGoalPomodoros = dailyGoalPomodoros,
                                        autoStartNext = autoStartNext,
                                        soundEnabled = soundEnabled,
                                        vibrationEnabled = vibrationEnabled
                                    )
                                )
                            },
                            interactionSource = saveInteraction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                            shape = saveShape,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp)
                                .coloredGlassButton(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    shape = saveShape,
                                    interactionSource = saveInteraction,
                                    shadowElevation = 10.dp,
                                    pressScale = 0.95f,
                                    pressDepth = 3.dp
                                )
                                .testTag("save_settings_button")
                        ) {
                            Text(
                                text = strings.settingsSave,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SECTION 1: THEME SELECTION (LIGHT / BLACK / SYSTEM)
                    SettingsSectionHeader(
                        icon = Icons.Default.Palette,
                        title = strings.settingsThemeSection,
                        tint = AmberAccent
                    )

                    SettingsGlassCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val themeOptions = listOf(
                                Triple(AppThemeMode.LIGHT, strings.themeLight, Icons.Default.LightMode),
                                Triple(AppThemeMode.BLACK, strings.themeBlack, Icons.Default.DarkMode),
                                Triple(AppThemeMode.SYSTEM, strings.themeSystem, Icons.Default.BrightnessAuto)
                            )
                            themeOptions.forEach { (mode, label, icon) ->
                                val isSelected = selectedThemeMode.equals(mode.code, ignoreCase = true)
                                val themeInteraction = remember { MutableInteractionSource() }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(74.dp)
                                        .floatingPressEffect(
                                            interactionSource = themeInteraction,
                                            pressScale = 0.945f,
                                            pressDepth = 2.dp
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) glassCardFillColor(
                                                accentColor = MaterialTheme.colorScheme.primary,
                                                accentAlpha = 0.14f,
                                                elevated = true
                                            ) else glassCardFillColor()
                                        )
                                        .border(
                                            glassBorderStroke(
                                                width = if (isSelected) 1.2.dp else 0.9.dp,
                                                highlightBoost = if (isSelected) 1.2f else 0.9f
                                            ),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable(
                                            interactionSource = themeInteraction,
                                            indication = LocalIndication.current
                                        ) {
                                            selectedThemeMode = mode.code
                                            onThemeSelected?.invoke(mode.code)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 8.dp)
                                        .testTag("theme_option_${mode.code}")
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: LANGUAGE SELECTION
                    SettingsSectionHeader(
                        icon = Icons.Default.Language,
                        title = strings.settingsLanguageSection,
                        tint = LongBreakBlue
                    )

                    SettingsGlassCard {
                        Column(modifier = Modifier.padding(12.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = selectedLanguage.equals(lang.code, ignoreCase = true)
                                    val langInteraction = remember { MutableInteractionSource() }
                                    Box(
                                        modifier = Modifier
                                            .floatingPressEffect(
                                                interactionSource = langInteraction,
                                                pressScale = 0.945f,
                                                pressDepth = 2.dp
                                            )
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) glassCardFillColor(
                                                    accentColor = MaterialTheme.colorScheme.primary,
                                                    accentAlpha = 0.14f,
                                                    elevated = true
                                                ) else glassCardFillColor()
                                            )
                                            .border(
                                                glassBorderStroke(
                                                    width = if (isSelected) 1.2.dp else 0.85.dp,
                                                    highlightBoost = if (isSelected) 1.15f else 0.9f
                                                ),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable(
                                                interactionSource = langInteraction,
                                                indication = LocalIndication.current
                                            ) {
                                                selectedLanguage = lang.code
                                                onLanguageSelected?.invoke(lang.code)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 9.dp)
                                            .testTag("language_option_${lang.code}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = lang.flag,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = lang.displayName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: TIMER DURATIONS & PRESETS (GROUPED IN GLASS CARD)
                    SettingsSectionHeader(
                        icon = Icons.Default.Speed,
                        title = strings.settingsTimerSection,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    SettingsGlassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Focus Duration
                            DurationControlBlock(
                                title = strings.settingsFocusDuration,
                                valueMinutes = focusMinutes,
                                unitLabel = strings.minutesShort,
                                presets = listOf(25, 45, 50, 60),
                                valueRange = 10f..90f,
                                steps = 15,
                                accentColor = MaterialTheme.colorScheme.primary,
                                sliderTestTag = "focus_duration_slider",
                                onValueChange = { focusMinutes = it }
                            )

                            SettingsGlassDivider()

                            // Short Break Duration
                            DurationControlBlock(
                                title = strings.settingsShortBreak,
                                valueMinutes = shortBreakMinutes,
                                unitLabel = strings.minutesShort,
                                presets = listOf(5, 10, 15),
                                valueRange = 2f..25f,
                                steps = 22,
                                accentColor = BreakSage,
                                sliderTestTag = "short_break_slider",
                                onValueChange = { shortBreakMinutes = it }
                            )

                            SettingsGlassDivider()

                            // Long Break Duration
                            DurationControlBlock(
                                title = strings.settingsLongBreak,
                                valueMinutes = longBreakMinutes,
                                unitLabel = strings.minutesShort,
                                presets = listOf(15, 20, 30),
                                valueRange = 5f..45f,
                                steps = 7,
                                accentColor = LongBreakBlue,
                                sliderTestTag = null,
                                onValueChange = { longBreakMinutes = it }
                            )
                        }
                    }

                    // Cycle Interval & Daily Goal Glass Card
                    SettingsGlassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Sessions Before Long Break Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 10.dp)
                                ) {
                                    Text(
                                        text = strings.settingsLongBreakInterval,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$sessionsBeforeLongBreak ${strings.targetTypeSessions}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    GlassStepperButton(
                                        symbol = "−",
                                        onClick = { if (sessionsBeforeLongBreak > 2) sessionsBeforeLongBreak-- }
                                    )
                                    Text(
                                        text = "$sessionsBeforeLongBreak",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    GlassStepperButton(
                                        symbol = "+",
                                        onClick = { if (sessionsBeforeLongBreak < 8) sessionsBeforeLongBreak++ }
                                    )
                                }
                            }

                            SettingsGlassDivider()

                            // Daily Goal Pomodoro Count
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.settingsDailyGoal,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                                            .padding(horizontal = 9.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "$dailyGoalPomodoros ${strings.targetTypeSessions}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Slider(
                                    value = dailyGoalPomodoros.toFloat(),
                                    onValueChange = { dailyGoalPomodoros = it.roundToInt() },
                                    valueRange = 1f..12f,
                                    steps = 10,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
                                    )
                                )
                            }
                        }
                    }

                    // SECTION 4: NOTIFICATIONS & FEEDBACK (GROUPED IN GLASS CARD)
                    SettingsSectionHeader(
                        icon = Icons.Default.NotificationsActive,
                        title = strings.settingsAlertsSection,
                        tint = BreakSage
                    )

                    SettingsGlassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SettingsToggleRow(
                                title = strings.settingsSoundAlert,
                                subtitle = strings.settingsSoundAlertDesc,
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it }
                            )

                            SettingsGlassDivider()

                            SettingsToggleRow(
                                title = strings.settingsVibration,
                                subtitle = strings.settingsVibrationDesc,
                                checked = vibrationEnabled,
                                onCheckedChange = { vibrationEnabled = it }
                            )

                            SettingsGlassDivider()

                            SettingsToggleRow(
                                title = strings.settingsAutoStart,
                                subtitle = strings.settingsAutoStartDesc,
                                checked = autoStartNext,
                                onCheckedChange = { autoStartNext = it }
                            )
                        }
                    }

                    // Reset to Defaults Frosted Pill Button
                    val resetDefaultsInteraction = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 4.dp, bottom = 16.dp)
                            .glassMorphism(
                                shape = RoundedCornerShape(50),
                                borderWidth = 1.dp,
                                shadowElevation = 7.dp,
                                interactionSource = resetDefaultsInteraction,
                                pressScale = 0.945f,
                                pressDepth = 2.5.dp
                            )
                            .clickable(
                                interactionSource = resetDefaultsInteraction,
                                indication = LocalIndication.current
                            ) {
                                focusMinutes = 50f
                                shortBreakMinutes = 10f
                                longBreakMinutes = 20f
                                sessionsBeforeLongBreak = 4
                                dailyGoalPomodoros = 4
                                autoStartNext = false
                                soundEnabled = true
                                vibrationEnabled = true
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.settingsResetDefaults,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsGlassTopBar(
    title: String,
    dismissLabel: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            val backInteraction = remember { MutableInteractionSource() }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = backInteraction,
                        indication = null
                    ) { onDismiss() }
                    .testTag("settings_back_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .glassMorphism(
                            shape = CircleShape,
                            elevated = true,
                            borderWidth = 1.dp,
                            shadowElevation = 7.dp,
                            interactionSource = backInteraction,
                            pressScale = 0.92f,
                            pressDepth = 2.5.dp
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = dismissLabel,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val closeInteraction = remember { MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(48.dp)
                .clip(RoundedCornerShape(50))
                .clickable(
                    interactionSource = closeInteraction,
                    indication = null
                ) { onDismiss() }
                .testTag("settings_close_button")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(36.dp)
                    .glassMorphism(
                        shape = RoundedCornerShape(50),
                        elevated = true,
                        borderWidth = 1.dp,
                        shadowElevation = 7.dp,
                        interactionSource = closeInteraction,
                        pressScale = 0.94f,
                        pressDepth = 2.5.dp
                    )
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = dismissLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SettingsGlassCard(
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .glassMorphism(
                shape = cardShape,
                borderWidth = 1.1.dp,
                shadowElevation = 9.dp
            )
    ) {
        content()
    }
}

@Composable
private fun SettingsGlassDivider() {
    val isDark = isGlassDarkTheme()
    HorizontalDivider(
        thickness = 0.8.dp,
        color = if (isDark) Color.White.copy(alpha = 0.09f) else Color.White.copy(alpha = 0.55f)
    )
}

@Composable
private fun DurationControlBlock(
    title: String,
    valueMinutes: Float,
    unitLabel: String,
    presets: List<Int>,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    accentColor: Color,
    sliderTestTag: String?,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${valueMinutes.roundToInt()} $unitLabel",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { mins ->
                val isPreset = valueMinutes.roundToInt() == mins
                PresetChip(
                    label = "${mins}${unitLabel}",
                    isSelected = isPreset,
                    activeColor = accentColor,
                    onClick = { onValueChange(mins.toFloat()) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Slider(
            value = valueMinutes,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
            ),
            modifier = if (sliderTestTag != null) Modifier.testTag(sliderTestTag) else Modifier
        )
    }
}

@Composable
private fun GlassStepperButton(
    symbol: String,
    onClick: () -> Unit
) {
    val stepperInteraction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .glassMorphism(
                shape = RoundedCornerShape(12.dp),
                elevated = true,
                borderWidth = 1.dp,
                shadowElevation = 5.dp,
                interactionSource = stepperInteraction,
                pressScale = 0.92f,
                pressDepth = 2.5.dp
            )
            .clickable(
                interactionSource = stepperInteraction,
                indication = LocalIndication.current
            ) { onClick() }
    ) {
        Text(
            text = symbol,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.16f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipInteraction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(38.dp)
            .floatingPressEffect(
                interactionSource = chipInteraction,
                pressScale = 0.94f,
                pressDepth = 2.dp
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) glassCardFillColor(
                    accentColor = activeColor,
                    accentAlpha = 0.15f,
                    elevated = true
                ) else glassCardFillColor()
            )
            .border(
                glassBorderStroke(
                    width = if (isSelected) 1.1.dp else 0.85.dp,
                    highlightBoost = if (isSelected) 1.15f else 0.9f
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = chipInteraction,
                indication = LocalIndication.current
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
        )
    }
}
