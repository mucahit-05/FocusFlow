package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskEntity
import com.example.ui.PomodoroViewModel
import com.example.ui.TimerMode
import com.example.ui.components.AmbientSoundBar
import com.example.ui.components.TimerCircle
import com.example.ui.components.coloredGlassButton
import com.example.ui.components.floatingPressEffect
import com.example.ui.components.glassCardFillColor
import com.example.ui.components.glassMorphism
import com.example.ui.theme.BreakSage
import com.example.ui.theme.FocusRed
import com.example.ui.theme.LongBreakBlue

@Composable
fun TimerScreen(
    viewModel: PomodoroViewModel,
    onNavigateToTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val ambientSound by viewModel.ambientSound.collectAsStateWithLifecycle()
    val ambientVolume by viewModel.ambientVolume.collectAsStateWithLifecycle()
    val appStrings by viewModel.appStrings.collectAsStateWithLifecycle()

    var showTaskPicker by remember { mutableStateOf(false) }

    val modeColor = when (timerState.currentMode) {
        TimerMode.FOCUS -> FocusRed
        TimerMode.SHORT_BREAK -> BreakSage
        TimerMode.LONG_BREAK -> LongBreakBlue
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector Filter Chips Row (words only, centered cleanly)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                TimerMode.values().forEach { mode ->
                    val isSelected = timerState.currentMode == mode
                    val modeTitle = when (mode) {
                        TimerMode.FOCUS -> appStrings.modeFocus
                        TimerMode.SHORT_BREAK -> appStrings.modeShortBreak
                        TimerMode.LONG_BREAK -> appStrings.modeLongBreak
                    }
                    val chipInteraction = remember { MutableInteractionSource() }
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.switchMode(mode) },
                        interactionSource = chipInteraction,
                        label = {
                            Text(
                                text = modeTitle,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = glassCardFillColor(),
                            selectedContainerColor = modeColor.copy(alpha = 0.22f),
                            selectedLabelColor = modeColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.28f),
                            selectedBorderColor = Color.White.copy(alpha = 0.50f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .floatingPressEffect(
                                interactionSource = chipInteraction,
                                pressScale = 0.94f,
                                pressDepth = 2.dp
                            )
                            .testTag("mode_chip_${mode.name.lowercase()}")
                    )
                }
            }
        }

        // Cycle Progress Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val totalInCycle = settings.sessionsBeforeLongBreak
            val currentInCycle = (timerState.completedCycles % totalInCycle)

            Text(
                text = "${appStrings.cycleLabel} ${currentInCycle + 1}/$totalInCycle",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            for (i in 0 until totalInCycle) {
                val isCompleted = i < currentInCycle
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) modeColor else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }

        // Center Countdown Timer Circle
        val timerCenterStatus = if (timerState.isRunning) {
            if (timerState.currentMode == TimerMode.FOCUS) appStrings.timerStatusFocusing else appStrings.timerStatusBreak
        } else {
            appStrings.timerStatusPaused
        }

        TimerCircle(
            remainingSeconds = timerState.remainingSeconds,
            totalSeconds = timerState.totalSeconds,
            mode = timerState.currentMode,
            isRunning = timerState.isRunning,
            statusText = timerCenterStatus,
            size = 260.dp
        )

        // Active Focus Task Card
        val activeTaskShape = RoundedCornerShape(18.dp)
        val activeTaskInteraction = remember { MutableInteractionSource() }
        Card(
            shape = activeTaskShape,
            modifier = Modifier
                .fillMaxWidth()
                .glassMorphism(
                    shape = activeTaskShape,
                    elevated = true,
                    borderWidth = 1.1.dp,
                    shadowElevation = 10.dp,
                    interactionSource = activeTaskInteraction,
                    pressScale = 0.975f,
                    pressDepth = 2.dp
                )
                .clickable(
                    interactionSource = activeTaskInteraction,
                    indication = LocalIndication.current
                ) { showTaskPicker = true }
                .testTag("active_task_card"),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (timerState.activeTaskId != null) Icons.Default.TaskAlt else Icons.Default.CheckCircle,
                        contentDescription = appStrings.taskStatusDesc,
                        tint = modeColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (timerState.activeTaskId != null) appStrings.activeTaskTitle else appStrings.noTaskSelected,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = timerState.activeTaskTitle ?: appStrings.selectTaskPrompt,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                TextButton(
                    onClick = { showTaskPicker = true },
                    modifier = Modifier.testTag("change_task_button")
                ) {
                    Text(
                        text = if (timerState.activeTaskId != null) appStrings.changeTask else appStrings.selectTask,
                        color = modeColor
                    )
                }
            }
        }

        // Ambient Sound Bar
        AmbientSoundBar(
            selectedSound = ambientSound,
            volume = ambientVolume,
            strings = appStrings,
            onSoundSelected = { viewModel.setAmbientSound(it) },
            onVolumeChanged = { viewModel.setAmbientVolume(it) }
        )

        // Timer Controls: Reset, Play/Pause, Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            val resetInteraction = remember { MutableInteractionSource() }
            FilledTonalIconButton(
                onClick = { viewModel.resetTimer() },
                interactionSource = resetInteraction,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .size(54.dp)
                    .glassMorphism(
                        shape = CircleShape,
                        elevated = true,
                        borderWidth = 1.1.dp,
                        shadowElevation = 8.dp,
                        interactionSource = resetInteraction,
                        pressScale = 0.92f,
                        pressDepth = 3.dp
                    )
                    .testTag("reset_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = appStrings.timerReset,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Main Play / Pause Button (vibrant colored subtle Glassmorphism + floating press-down)
            val playInteraction = remember { MutableInteractionSource() }
            val playShape = RoundedCornerShape(30.dp)
            Button(
                onClick = {
                    if (timerState.isRunning) {
                        viewModel.toggleTimer()
                    } else {
                        viewModel.startFocusMode()
                    }
                },
                interactionSource = playInteraction,
                modifier = Modifier
                    .height(60.dp)
                    .padding(horizontal = 8.dp)
                    .coloredGlassButton(
                        containerColor = modeColor,
                        shape = playShape,
                        interactionSource = playInteraction,
                        shadowElevation = 11.dp,
                        pressScale = 0.94f,
                        pressDepth = 3.5.dp
                    )
                    .testTag("play_pause_button"),
                shape = playShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) appStrings.timerPause else appStrings.timerStart,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerState.isRunning) appStrings.timerPause else appStrings.timerStart,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Skip Next Button
            val skipInteraction = remember { MutableInteractionSource() }
            FilledTonalIconButton(
                onClick = { viewModel.skipTimer() },
                interactionSource = skipInteraction,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .size(54.dp)
                    .glassMorphism(
                        shape = CircleShape,
                        elevated = true,
                        borderWidth = 1.1.dp,
                        shadowElevation = 8.dp,
                        interactionSource = skipInteraction,
                        pressScale = 0.92f,
                        pressDepth = 3.dp
                    )
                    .testTag("skip_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = appStrings.timerSkip,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Task Picker Dialog
    if (showTaskPicker) {
        val pendingTasks = tasks.filter { !it.isCompleted }
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text(appStrings.taskPickerTitle) },
            text = {
                Column {
                    if (pendingTasks.isEmpty()) {
                        Text(
                            text = appStrings.taskPickerEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        pendingTasks.forEach { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.selectActiveTask(task)
                                        showTaskPicker = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (timerState.activeTaskId == task.id)
                                        modeColor.copy(alpha = 0.2f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                        )
                                        if (task.recurrence != "NONE") {
                                            Text(
                                                text = "${task.targetValue} ${task.targetType}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Timer,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "${task.completedPomodoros}/${task.estimatedPomodoros}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (timerState.activeTaskId != null) {
                    TextButton(
                        onClick = {
                            viewModel.selectActiveTask(null)
                            showTaskPicker = false
                        }
                    ) {
                        Text(appStrings.taskPickerRemove, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTaskPicker = false
                    onNavigateToTasks()
                }) {
                    Text(appStrings.taskPickerManage)
                }
            }
        )
    }
}
