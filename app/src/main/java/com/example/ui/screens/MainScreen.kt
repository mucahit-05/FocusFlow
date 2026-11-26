package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PomodoroViewModel
import com.example.ui.components.GlassAmbientBackdrop
import com.example.ui.components.glassBorderStroke
import com.example.ui.components.glassCardFillColor
import com.example.ui.components.glassMorphism

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Timer, 1: Tasks, 2: Stats
    var showSettingsScreen by remember { mutableStateOf(false) }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val appStrings by viewModel.appStrings.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val isZenMode by viewModel.isZenMode.collectAsStateWithLifecycle()
    val currentMode = timerState.currentMode

    // Handle back button to return to Timer tab if currently on other tabs (when Settings and Focus Mode are closed)
    BackHandler(enabled = !showSettingsScreen && !isZenMode && selectedTab != 0) {
        selectedTab = 0
    }

    val scaffoldScale by animateFloatAsState(
        targetValue = if (isZenMode) 0.92f else 1f,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
        label = "scaffold_focus_scale"
    )
    val scaffoldAlpha by animateFloatAsState(
        targetValue = if (isZenMode) 0f else 1f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "scaffold_focus_alpha"
    )

    GlassAmbientBackdrop(mode = currentMode, modifier = modifier.fillMaxSize()) {

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scaffoldScale
                scaleY = scaffoldScale
                alpha = scaffoldAlpha
            },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FocusFlow",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Surface(
                            shape = CircleShape,
                            color = glassCardFillColor(
                                accentColor = MaterialTheme.colorScheme.primary,
                                accentAlpha = 0.07f,
                                elevated = true
                            ),
                            border = glassBorderStroke(width = 0.9.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = when (currentMode) {
                                        com.example.ui.TimerMode.FOCUS -> appStrings.modeFocus
                                        com.example.ui.TimerMode.SHORT_BREAK -> appStrings.modeShortBreak
                                        com.example.ui.TimerMode.LONG_BREAK -> appStrings.modeLongBreak
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                actions = {
                    val settingsBtnInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { showSettingsScreen = true },
                        interactionSource = settingsBtnInteraction,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .glassMorphism(
                                    shape = CircleShape,
                                    elevated = true,
                                    borderWidth = 1.dp,
                                    shadowElevation = 6.dp,
                                    interactionSource = settingsBtnInteraction,
                                    pressScale = 0.90f,
                                    pressDepth = 2.dp
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = appStrings.settingsTitle,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                modifier = Modifier
                    .glassMorphism(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp
                        ),
                        elevated = true,
                        borderWidth = 1.1.dp,
                        shadowElevation = 10.dp
                    )
                    .testTag("bottom_navigation_bar")
            ) {
                val navBarColors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )

                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Timer, contentDescription = appStrings.tabTimer) },
                    label = { Text(appStrings.tabTimer) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_timer")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Checklist, contentDescription = appStrings.tabTasks) },
                    label = { Text(appStrings.tabTasks) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_tasks")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = appStrings.tabStats) },
                    label = { Text(appStrings.tabStats) },
                    colors = navBarColors,
                    modifier = Modifier.testTag("nav_tab_stats")
                )
            }
        }

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> width / 4 } +
                                fadeIn(animationSpec = tween(250))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                        fadeOut(animationSpec = tween(200)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                fadeIn(animationSpec = tween(250))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { width -> width / 4 } +
                                        fadeOut(animationSpec = tween(200)))
                    }
                },
                label = "TabAnimatedContent"
            ) { tab ->
                when (tab) {
                    0 -> TimerScreen(
                        viewModel = viewModel,
                        onNavigateToTasks = { selectedTab = 1 }
                    )
                    1 -> TasksScreen(
                        viewModel = viewModel,
                        onNavigateToTimer = { selectedTab = 0 }
                    )
                    2 -> StatsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isZenMode,
        enter = fadeIn(tween(460, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.88f,
                animationSpec = spring(dampingRatio = 0.82f, stiffness = 280f)
            ),
        exit = fadeOut(tween(340, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = 0.90f,
                animationSpec = tween(360, easing = FastOutSlowInEasing)
            )
    ) {
        ZenModeScreen(
            timerState = timerState,
            strings = appStrings,
            onExitZen = { viewModel.exitFocusMode() }
        )
    }

    AnimatedVisibility(
        visible = showSettingsScreen,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(280, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(220)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(240, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(180))
    ) {
        SettingsScreen(
            currentSettings = settings,
            onDismiss = { showSettingsScreen = false },
            onLanguageSelected = { langCode ->
                viewModel.setLanguage(langCode)
            },
            onThemeSelected = { themeCode ->
                viewModel.setThemeMode(themeCode)
            },
            onSave = { updated ->
                viewModel.updateSettings(updated)
                showSettingsScreen = false
            }
        )
    }
    }
}
