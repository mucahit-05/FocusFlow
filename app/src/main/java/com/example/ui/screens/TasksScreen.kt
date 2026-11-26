package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.util.bounceClick
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SessionLogEntity
import com.example.data.model.TaskEntity
import com.example.ui.PomodoroViewModel
import com.example.ui.components.coloredGlassButton
import com.example.ui.components.floatingPressEffect
import com.example.ui.components.glassCardFillColor
import com.example.ui.components.glassMorphism
import com.example.ui.components.tintedGlassButton
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BreakSage
import com.example.ui.theme.FocusRed
import com.example.ui.theme.LongBreakBlue
import com.example.util.AppStrings
import com.example.util.TaskRecurrenceHelper
import kotlin.math.abs

fun getDynamicCategoryColor(category: String): Color {
    val palette = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFE54D38), // Coral
        Color(0xFF8B5CF6), // Purple
        Color(0xFF06B6D4), // Cyan
        Color(0xFFEC4899), // Pink
        Color(0xFF14B8A6), // Teal
        Color(0xFFF97316)  // Orange
    )
    val index = abs(category.lowercase().hashCode()) % palette.size
    return palette[index]
}

@Composable
fun TasksScreen(
    viewModel: PomodoroViewModel,
    onNavigateToTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val celebrationMessage by viewModel.celebrationMessage.collectAsStateWithLifecycle()
    val appStrings by viewModel.appStrings.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "RECURRING", "COMPLETED"
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()

    // Distinct custom categories dynamically collected from user custom categories and tasks
    val userCategories = remember(tasks, customCategories) {
        (customCategories + tasks.map { it.category.trim() }.filter { it.isNotBlank() }).distinct().sorted()
    }

    // Dynamic tags collected from tasks
    val allTags = remember(tasks) {
        tasks.flatMap { it.getTagsList() }.distinct().sorted()
    }

    val filteredTasks = tasks.filter { task ->
        val progress = TaskRecurrenceHelper.computeTaskProgress(task, allSessions, appStrings)
        val matchesStatus = when (statusFilter) {
            "PENDING" -> !progress.isGoalCompleted
            "COMPLETED" -> progress.isGoalCompleted
            "RECURRING" -> task.recurrence != "NONE"
            else -> true
        }
        val matchesCategory = when {
            selectedCategoryFilter == null -> true
            selectedCategoryFilter == "__NONE__" -> task.category.isBlank()
            else -> task.category.equals(selectedCategoryFilter, ignoreCase = true)
        }
        val matchesTag = when {
            selectedTagFilter == null -> true
            else -> task.getTagsList().any { it.equals(selectedTagFilter, ignoreCase = true) }
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            task.title.lowercase().contains(q) ||
            task.description.lowercase().contains(q) ||
            task.category.lowercase().contains(q) ||
            task.getTagsList().any { it.lowercase().contains(q) }
        }
        matchesStatus && matchesCategory && matchesTag && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(appStrings.tasksSearchPlaceholder, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = glassCardFillColor(elevated = true),
                    unfocusedContainerColor = glassCardFillColor(),
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.38f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("task_search_bar")
            )

            // Tag Filter Chips
            if (allTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedTagFilter == null,
                        onClick = { selectedTagFilter = null },
                        label = { Text(appStrings.filterAll, fontSize = 11.sp) }
                    )
                    allTags.forEach { tag ->
                        val isSelected = selectedTagFilter.equals(tag, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTagFilter = if (isSelected) null else tag
                            },
                            label = { Text("#$tag", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Status & Recurrence Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == "ALL",
                    onClick = { statusFilter = "ALL" },
                    label = { Text("${appStrings.filterAll} (${tasks.size})") }
                )
                FilterChip(
                    selected = statusFilter == "PENDING",
                    onClick = { statusFilter = "PENDING" },
                    label = {
                        val activeCount = tasks.count {
                            !TaskRecurrenceHelper.computeTaskProgress(it, allSessions, appStrings).isGoalCompleted
                        }
                        Text("${appStrings.filterActive} ($activeCount)")
                    }
                )
                FilterChip(
                    selected = statusFilter == "RECURRING",
                    onClick = { statusFilter = "RECURRING" },
                    label = { Text("${appStrings.filterRecurring} (${tasks.count { it.recurrence != "NONE" }})") }
                )
                FilterChip(
                    selected = statusFilter == "COMPLETED",
                    onClick = { statusFilter = "COMPLETED" },
                    label = {
                        val compCount = tasks.count {
                            TaskRecurrenceHelper.computeTaskProgress(it, allSessions, appStrings).isGoalCompleted
                        }
                        Text("${appStrings.filterCompleted} ($compCount)")
                    }
                )
            }

            // User-Defined Custom Categories Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text(appStrings.allCategories) }
                )
                if (tasks.any { it.category.isBlank() }) {
                    FilterChip(
                        selected = selectedCategoryFilter == "__NONE__",
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == "__NONE__") null else "__NONE__"
                        },
                        label = { Text(appStrings.noCategoryLabel, fontSize = 12.sp) }
                    )
                }
                userCategories.forEach { cat ->
                    val isSelected = selectedCategoryFilter.equals(cat, ignoreCase = true)
                    val catColor = getDynamicCategoryColor(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCategoryFilter = if (isSelected) null else cat
                        },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor.copy(alpha = 0.2f),
                            selectedLabelColor = catColor
                        )
                    )
                }

                // Add Category Chip directly in filter row
                FilterChip(
                    selected = false,
                    onClick = { showAddCategoryDialog = true },
                    label = { Text(appStrings.addCategoryPrompt, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("add_custom_category_chip")
                )
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = appStrings.emptyTasksTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = appStrings.emptyTasksDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        EnhancedTaskCard(
                            task = task,
                            allSessions = allSessions,
                            strings = appStrings,
                            isActiveForTimer = timerState.activeTaskId == task.id,
                            onToggleComplete = { viewModel.toggleTaskComplete(task) },
                            onSelectForTimer = {
                                viewModel.selectActiveTask(task)
                                onNavigateToTimer()
                            },
                            onQuickLogSession = {
                                viewModel.logSessionForTask(task, durationMinutes = 25)
                            },
                            onDelete = { viewModel.deleteTask(task) },
                            onToggleSubtask = { subtaskId ->
                                viewModel.toggleSubtask(task, subtaskId)
                            },
                            onAddSubtask = { subtaskTitle ->
                                viewModel.addSubtaskToTask(task, subtaskTitle)
                            },
                            onDeleteSubtask = { subtaskId ->
                                viewModel.deleteSubtask(task, subtaskId)
                            },
                            onResetRecurring = {
                                viewModel.resetRecurringTask(task)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Celebration Banner
        AnimatedVisibility(
            visible = celebrationMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
        ) {
            celebrationMessage?.let { msg ->
                val bannerShape = RoundedCornerShape(18.dp)
                Card(
                    shape = bannerShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassMorphism(
                            shape = bannerShape,
                            accentColor = BreakSage,
                            accentAlpha = 0.08f,
                            elevated = true,
                            borderWidth = 1.1.dp,
                            shadowElevation = 10.dp
                        )
                        .testTag("task_celebration_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BreakSage.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Celebration,
                                contentDescription = null,
                                tint = BreakSage,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appStrings.goalCompletedBanner,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = BreakSage
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissCelebration() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = appStrings.settingsDismiss,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Add Task FAB
        val fabInteraction = remember { MutableInteractionSource() }
        val fabShape = RoundedCornerShape(18.dp)
        FloatingActionButton(
            onClick = { showAddDialog = true },
            interactionSource = fabInteraction,
            shape = fabShape,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .coloredGlassButton(
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = fabShape,
                    interactionSource = fabInteraction,
                    shadowElevation = 11.dp,
                    pressScale = 0.92f,
                    pressDepth = 3.5.dp
                )
                .testTag("add_task_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = appStrings.addTaskFab)
        }
    }

    if (showAddDialog) {
        EnhancedAddTaskDialog(
            strings = appStrings,
            existingCategories = userCategories,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, category, recurrence, targetType, targetValue, scheduledTime, priority, est, subtasks, tags ->
                viewModel.addTask(
                    title = title,
                    description = desc,
                    category = category,
                    recurrence = recurrence,
                    targetType = targetType,
                    targetValue = targetValue,
                    scheduledTime = scheduledTime,
                    priority = priority,
                    estimatedPomodoros = est,
                    initialSubtasks = subtasks,
                    tags = tags
                )
                showAddDialog = false
            }
        )
    }

    if (showAddCategoryDialog) {
        var newCatInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = {
                Text(
                    text = appStrings.addNewCategoryDialogTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                OutlinedTextField(
                    value = newCatInput,
                    onValueChange = { newCatInput = it },
                    label = { Text(appStrings.categoryLabel) },
                    placeholder = { Text(appStrings.customCategoryPlaceholder, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_category_dialog_input")
                )
            },
            confirmButton = {
                val catConfirmInteraction = remember { MutableInteractionSource() }
                val catBtnShape = RoundedCornerShape(12.dp)
                Button(
                    onClick = {
                        val trimmed = newCatInput.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.addCustomCategory(trimmed)
                            selectedCategoryFilter = trimmed
                            showAddCategoryDialog = false
                        }
                    },
                    interactionSource = catConfirmInteraction,
                    shape = catBtnShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier.coloredGlassButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        shape = catBtnShape,
                        interactionSource = catConfirmInteraction,
                        shadowElevation = 7.dp
                    )
                ) {
                    Text(appStrings.addCustomCategoryButton, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(appStrings.cancelButton)
                }
            }
        )
    }
}

@Composable
fun EnhancedTaskCard(
    task: TaskEntity,
    allSessions: List<SessionLogEntity>,
    strings: AppStrings,
    isActiveForTimer: Boolean,
    onToggleComplete: () -> Unit,
    onSelectForTimer: () -> Unit,
    onQuickLogSession: () -> Unit,
    onDelete: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDeleteSubtask: (String) -> Unit,
    onResetRecurring: () -> Unit
) {
    var expandedSubtasks by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var isAddingSubtask by remember { mutableStateOf(false) }

    val subtasks = task.getSubtasks()
    val completedSubtasks = subtasks.count { it.isCompleted }

    val isRecurring = task.recurrence != "NONE"
    val progress = remember(task, allSessions) {
        TaskRecurrenceHelper.computeTaskProgress(task, allSessions, strings)
    }

    val priorityColor = when (task.priority) {
        "HIGH" -> FocusRed
        "LOW" -> BreakSage
        else -> AmberAccent
    }
    val priorityLabel = when (task.priority) {
        "HIGH" -> strings.priorityHigh
        "LOW" -> strings.priorityLow
        else -> strings.priorityNormal
    }

    val catColor = getDynamicCategoryColor(task.category)

    val recurrenceBadge = when (task.recurrence) {
        "DAILY" -> strings.recDaily
        "WEEKLY" -> strings.recWeekly
        "MONTHLY" -> strings.recMonthly
        else -> null
    }

    val taskCardShape = RoundedCornerShape(18.dp)
    Card(
        shape = taskCardShape,
        modifier = Modifier
            .fillMaxWidth()
            .glassMorphism(
                shape = taskCardShape,
                accentColor = if (isActiveForTimer) FocusRed else null,
                accentAlpha = if (isActiveForTimer) 0.05f else 0f,
                elevated = isActiveForTimer,
                borderWidth = 1.1.dp,
                shadowElevation = 9.dp
            )
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category, Recurrence, Priority Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Custom User-Defined Category Badge (only displayed if user added a category)
                    if (task.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(catColor.copy(alpha = 0.16f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = catColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Recurrence Badge
                    if (recurrenceBadge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LongBreakBlue.copy(alpha = 0.18f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = recurrenceBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = LongBreakBlue,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priorityLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor,
                            fontSize = 10.sp
                        )
                    }

                    // Scheduled Time
                    if (!task.scheduledTime.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = task.scheduledTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Delete button in top corner
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("delete_task_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.deleteTask,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox for one-time task
                if (!isRecurring) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = BreakSage,
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.testTag("task_checkbox_${task.id}")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (!isRecurring && task.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (!isRecurring && task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val taskTags = task.getTagsList()
                    if (taskTags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            taskTags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // RECURRING GOAL PROGRESS BAR & STATUS
            if (isRecurring) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (progress.isGoalCompleted) BreakSage.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (progress.isGoalCompleted) Icons.Rounded.EmojiEvents else Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                tint = if (progress.isGoalCompleted) BreakSage else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (progress.isGoalCompleted) strings.goalCompletedBanner else progress.progressDisplayText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (progress.isGoalCompleted) BreakSage else MaterialTheme.colorScheme.onSurface
                            )
                            if (!progress.isGoalCompleted) {
                                Text(
                                    text = " • ${progress.remainingDisplayText}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "%${(progress.progressFraction * 100).toInt()}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (progress.isGoalCompleted) BreakSage else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress.isGoalCompleted) BreakSage else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    if (progress.periodResetNotice.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = progress.periodResetNotice,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                // One-time task progress row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${task.completedPomodoros}/${task.estimatedPomodoros} ${strings.targetTypeSessions}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ACTION BAR: Quick +1 Session, Focus in Timer, Subtasks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left action: Quick log session
                val quickInteraction = remember { MutableInteractionSource() }
                val quickShape = RoundedCornerShape(10.dp)
                Button(
                    onClick = onQuickLogSession,
                    interactionSource = quickInteraction,
                    shape = quickShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = BreakSage
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .tintedGlassButton(
                            tintColor = BreakSage,
                            shape = quickShape,
                            interactionSource = quickInteraction,
                            shadowElevation = 5.dp,
                            pressScale = 0.94f,
                            pressDepth = 2.dp
                        )
                        .testTag("quick_add_session_${task.id}"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = BreakSage
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.quickAddSession, fontSize = 11.sp, color = BreakSage, fontWeight = FontWeight.Bold)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isRecurring && progress.isGoalCompleted) {
                        val resetRecInteraction = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = onResetRecurring,
                            interactionSource = resetRecInteraction,
                            modifier = Modifier
                                .size(34.dp)
                                .tintedGlassButton(
                                    tintColor = LongBreakBlue,
                                    shape = CircleShape,
                                    interactionSource = resetRecInteraction,
                                    shadowElevation = 4.dp,
                                    pressScale = 0.92f,
                                    pressDepth = 2.dp
                                )
                                .testTag("reset_recurring_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = strings.resetCycle,
                                tint = LongBreakBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Focus Timer Button
                    val focusBtnInteraction = remember { MutableInteractionSource() }
                    val focusBtnShape = RoundedCornerShape(10.dp)
                    val focusBtnColor = MaterialTheme.colorScheme.primary
                    Button(
                        onClick = onSelectForTimer,
                        interactionSource = focusBtnInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = if (isActiveForTimer) Color.White else focusBtnColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        shape = focusBtnShape,
                        modifier = Modifier
                            .height(34.dp)
                            .let { base ->
                                if (isActiveForTimer) {
                                    base.coloredGlassButton(
                                        containerColor = focusBtnColor,
                                        shape = focusBtnShape,
                                        interactionSource = focusBtnInteraction,
                                        shadowElevation = 7.dp,
                                        pressScale = 0.94f,
                                        pressDepth = 2.5.dp
                                    )
                                } else {
                                    base.tintedGlassButton(
                                        tintColor = focusBtnColor,
                                        shape = focusBtnShape,
                                        interactionSource = focusBtnInteraction,
                                        shadowElevation = 5.dp,
                                        pressScale = 0.94f,
                                        pressDepth = 2.dp
                                    )
                                }
                            }
                            .testTag("focus_task_button_${task.id}"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = if (isActiveForTimer) Icons.Default.PlayCircleFilled else Icons.Default.PlayCircleOutline,
                            contentDescription = strings.focusOnTask,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isActiveForTimer) strings.taskActiveBadge else strings.focusOnTask,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Subtasks accordion toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedSubtasks = !expandedSubtasks }
                    .padding(top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (subtasks.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (completedSubtasks == subtasks.size) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$completedSubtasks/${subtasks.size} ${strings.subtasksHeader}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (completedSubtasks == subtasks.size) BreakSage else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = strings.addSubtaskPrompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Icon(
                    imageVector = if (expandedSubtasks) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded Subtasks Content
            AnimatedVisibility(visible = expandedSubtasks) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    subtasks.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sub.isCompleted,
                                onCheckedChange = { onToggleSubtask(sub.id) },
                                modifier = Modifier.size(24.dp),
                                colors = CheckboxDefaults.colors(checkedColor = BreakSage)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else null
                                ),
                                color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteSubtask(sub.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.deleteSubtask,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (isAddingSubtask) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskText,
                                onValueChange = { newSubtaskText = it },
                                placeholder = { Text(strings.addSubtaskInputPlaceholder, fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = {
                                    if (newSubtaskText.isNotBlank()) {
                                        onAddSubtask(newSubtaskText)
                                        newSubtaskText = ""
                                        isAddingSubtask = false
                                    }
                                }
                            ) {
                                Text(strings.addSubtaskAction, color = BreakSage)
                            }
                        }
                    } else {
                        TextButton(
                            onClick = { isAddingSubtask = true },
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.addSubtaskPrompt, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnhancedAddTaskDialog(
    strings: AppStrings,
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        category: String,
        recurrence: String,
        targetType: String,
        targetValue: Int,
        scheduledTime: String?,
        priority: String,
        estimatedPomodoros: Int,
        initialSubtasks: List<String>,
        tags: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("WEEKLY") }
    var targetType by remember { mutableStateOf("SESSIONS") }
    var targetValue by remember { mutableIntStateOf(5) }
    var scheduledTime by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }
    var estimatedPomodoros by remember { mutableIntStateOf(2) }

    val initialSubtasks = remember { mutableStateListOf<String>() }
    var currentSubtaskInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.newTaskDialogTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.taskTitleLabel) },
                    placeholder = { Text(strings.taskTitlePlaceholder, fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                // User-Defined Custom Category Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.categoryLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        placeholder = { Text(strings.customCategoryPlaceholder, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_category_input")
                    )

                    // Quick-select chips from existing user categories
                    if (existingCategories.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            existingCategories.forEach { cat ->
                                val isSelected = customCategory.equals(cat, ignoreCase = true)
                                val catColor = getDynamicCategoryColor(cat)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) catColor else catColor.copy(alpha = 0.15f))
                                        .clickable { customCategory = cat }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isSelected) Color.White else catColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Tags Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.tasksTagsHeader,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        placeholder = { Text(strings.tasksTagInputPlaceholder, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_tags_input")
                    )
                }

                // RECURRENCE & CYCLE SELECTOR
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.recurrenceSectionTitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val recurrenceOptions = listOf(
                        "WEEKLY" to strings.recWeekly,
                        "MONTHLY" to strings.recMonthly,
                        "DAILY" to strings.recDaily,
                        "NONE" to strings.recNone
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        recurrenceOptions.forEach { (rKey, rLabel) ->
                            val isSel = recurrence == rKey
                            FilterChip(
                                selected = isSel,
                                onClick = { recurrence = rKey },
                                label = { Text(rLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LongBreakBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = LongBreakBlue
                                )
                            )
                        }
                    }
                }

                // TARGET TYPE & TARGET VALUE (FOR RECURRING GOALS)
                if (recurrence != "NONE") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = strings.targetTypeSectionTitle,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = LongBreakBlue
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = targetType == "SESSIONS",
                                    onClick = {
                                        targetType = "SESSIONS"
                                        if (targetValue > 30) targetValue = 5
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Timer,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    label = { Text(strings.targetTypeSessions, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = targetType == "DURATION_HOURS",
                                    onClick = {
                                        targetType = "DURATION_HOURS"
                                        if (targetValue > 20) targetValue = 5
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    label = { Text(strings.targetTypeHours, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Stepper & Quick Presets for Target Value
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$targetValue ${if (targetType == "DURATION_HOURS") strings.targetTypeHours else strings.targetTypeSessions}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FocusRed
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { if (targetValue > 1) targetValue-- },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(34.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("-", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "$targetValue",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    OutlinedButton(
                                        onClick = { if (targetValue < 50) targetValue++ },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(34.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("+", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Target Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = if (targetType == "DURATION_HOURS") listOf(2, 3, 5, 10) else listOf(3, 5, 8, 12)
                                presets.forEach { p ->
                                    val isP = targetValue == p
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                            .clickable { targetValue = p }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$p",
                                            fontSize = 11.sp,
                                            fontWeight = if (isP) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isP) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Estimated Pomodoro for one-time task
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${strings.estimatedPomodoroLabel}: $estimatedPomodoros",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { if (estimatedPomodoros > 1) estimatedPomodoros-- },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("-1")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "$estimatedPomodoros",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            OutlinedButton(
                                onClick = { if (estimatedPomodoros < 12) estimatedPomodoros++ },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+1")
                            }
                        }
                    }
                }

                // Priority Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.prioritySectionTitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val priorities = listOf(
                            "LOW" to strings.priorityLow,
                            "NORMAL" to strings.priorityNormal,
                            "HIGH" to strings.priorityHigh
                        )
                        priorities.forEach { (pKey, pLabel) ->
                            FilterChip(
                                selected = priority == pKey,
                                onClick = { priority = pKey },
                                label = { Text(pLabel) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Subtasks
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.subtasksHeader,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    initialSubtasks.forEachIndexed { index, sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("• $sub", modifier = Modifier.weight(1f), fontSize = 12.sp)
                            IconButton(
                                onClick = { initialSubtasks.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentSubtaskInput,
                            onValueChange = { currentSubtaskInput = it },
                            placeholder = { Text(strings.addSubtaskInputPlaceholder, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                if (currentSubtaskInput.isNotBlank()) {
                                    initialSubtasks.add(currentSubtaskInput.trim())
                                    currentSubtaskInput = ""
                                }
                            }
                        ) {
                            Text(strings.addSubtaskAction)
                        }
                    }
                }
            }
        },
        confirmButton = {
            val confirmInteraction = remember { MutableInteractionSource() }
            val confirmShape = RoundedCornerShape(12.dp)
            val isEnabled = title.isNotBlank()
            Button(
                onClick = {
                    if (isEnabled) {
                        val finalCategory = customCategory.trim()
                        onConfirm(
                            title,
                            description,
                            finalCategory,
                            recurrence,
                            targetType,
                            targetValue,
                            if (scheduledTime.isNotBlank()) scheduledTime.trim() else null,
                            priority,
                            estimatedPomodoros,
                            initialSubtasks,
                            tagsInput.trim()
                        )
                    }
                },
                enabled = isEnabled,
                interactionSource = confirmInteraction,
                shape = confirmShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.55f)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier
                    .coloredGlassButton(
                        containerColor = if (isEnabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        shape = confirmShape,
                        interactionSource = confirmInteraction,
                        shadowElevation = if (isEnabled) 8.dp else 0.dp
                    )
                    .testTag("confirm_add_task_button")
            ) {
                Text(strings.createButton, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelButton)
            }
        }
    )
}
