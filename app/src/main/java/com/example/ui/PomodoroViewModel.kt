package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BadgeItem
import com.example.data.model.SessionLogEntity
import com.example.data.model.SubtaskConverter
import com.example.data.model.SubtaskItem
import com.example.data.model.TaskEntity
import com.example.data.repository.FocusRepository
import com.example.util.AmbientAudioEngine
import com.example.util.AmbientSoundType
import com.example.util.AppStrings
import com.example.util.LocalizationManager
import com.example.util.SettingsPreferences
import com.example.util.SoundAndVibrationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class TimerMode(val displayName: String) {
    FOCUS("Derin Odaklanma"),
    SHORT_BREAK("Kısa Mola"),
    LONG_BREAK("Uzun Mola")
}

data class PomodoroSettings(
    val focusMinutes: Int = 50,
    val shortBreakMinutes: Int = 10,
    val longBreakMinutes: Int = 20,
    val sessionsBeforeLongBreak: Int = 4,
    val dailyGoalPomodoros: Int = 4,
    val autoStartNext: Boolean = false,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: String = "tr",
    val themeMode: String = "system"
)

data class TimerUiState(
    val currentMode: TimerMode = TimerMode.FOCUS,
    val totalSeconds: Int = 50 * 60,
    val remainingSeconds: Int = 50 * 60,
    val isRunning: Boolean = false,
    val completedCycles: Int = 0,
    val activeTaskId: Long? = null,
    val activeTaskTitle: String? = null
)

data class DayFocusStat(
    val dayLabel: String,
    val focusMinutes: Int,
    val dayTimestamp: Long
)

class PomodoroViewModel(
    private val repository: FocusRepository,
    private val alertHelper: SoundAndVibrationHelper,
    private val audioEngine: AmbientAudioEngine,
    private val preferences: SettingsPreferences
) : ViewModel() {

    private val _settings = MutableStateFlow(preferences.loadSettings())
    val settings: StateFlow<PomodoroSettings> = _settings.asStateFlow()

    private val _appStrings = MutableStateFlow(LocalizationManager.getStrings(_settings.value.language))
    val appStrings: StateFlow<AppStrings> = _appStrings.asStateFlow()

    private val _customCategories = MutableStateFlow(preferences.loadCustomCategories().toList().sorted())
    val customCategories: StateFlow<List<String>> = _customCategories.asStateFlow()

    private val _userProfile = MutableStateFlow(
        preferences.loadUserProfile(defaultNickname = _appStrings.value.profileDefaultNickname)
    )
    val userProfile: StateFlow<com.example.data.model.UserProfile> = _userProfile.asStateFlow()

    fun updateUserProfile(profile: com.example.data.model.UserProfile) {
        _userProfile.value = profile
        preferences.saveUserProfile(profile)
    }

    fun setCustomProfilePhoto(uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val photoFile = java.io.File(context.filesDir, "user_profile_avatar.jpg")
                val outputStream = java.io.FileOutputStream(photoFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val updated = _userProfile.value.copy(customPhotoPath = photoFile.absolutePath)
                _userProfile.value = updated
                preferences.saveUserProfile(updated)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeCustomProfilePhoto() {
        val currentPath = _userProfile.value.customPhotoPath
        if (currentPath != null) {
            try {
                java.io.File(currentPath).delete()
            } catch (_: Exception) {}
        }
        val updated = _userProfile.value.copy(customPhotoPath = null)
        _userProfile.value = updated
        preferences.saveUserProfile(updated)
    }

    init {
        viewModelScope.launch {
            repository.cleanupLegacyWorkCategory()
        }
    }


    private val _timerState = MutableStateFlow(
        TimerUiState(
            currentMode = TimerMode.FOCUS,
            totalSeconds = _settings.value.focusMinutes * 60,
            remainingSeconds = _settings.value.focusMinutes * 60
        )
    )
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private val _ambientSound = MutableStateFlow(AmbientSoundType.NONE)
    val ambientSound: StateFlow<AmbientSoundType> = _ambientSound.asStateFlow()

    private val _ambientVolume = MutableStateFlow(0.5f)
    val ambientVolume: StateFlow<Float> = _ambientVolume.asStateFlow()

    private val _isZenMode = MutableStateFlow(false)
    val isZenMode: StateFlow<Boolean> = _isZenMode.asStateFlow()

    private val _celebrationMessage = MutableStateFlow<String?>(null)
    val celebrationMessage: StateFlow<String?> = _celebrationMessage.asStateFlow()

    private val motivationalQuotes = listOf(
        "Harika iş! Bir adımı daha başarıyla tamamladın.",
        "Odaklanmanın gücü! Başarı küçük adımların toplamıdır.",
        "Tebrikler! Büyük hedefine bir adım daha yaklaştın.",
        "Derin çalışma ritmin harika ilerliyor, durma!",
        "Büyük başarılar, tamamlanan küçük görevlerle başlar.",
        "Ertelemeyi yendin, harika bir disiplin!"
    )

    fun dismissCelebration() {
        _celebrationMessage.value = null
    }

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSessions: StateFlow<List<SessionLogEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dynamic Badges & Achievements state
    val badges: StateFlow<List<BadgeItem>> = combine(allSessions, allTasks, _appStrings) { sessions, tasks, strings ->
        computeBadges(sessions, tasks, strings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var timerJob: Job? = null

    fun toggleTimer() {
        alertHelper.triggerClickHaptic()
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isRunning = true)

        if (_ambientSound.value != AmbientSoundType.NONE) {
            audioEngine.play()
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                val newRemaining = _timerState.value.remainingSeconds - 1
                _timerState.value = _timerState.value.copy(remainingSeconds = newRemaining)
            }

            if (_timerState.value.remainingSeconds <= 0 && _timerState.value.isRunning) {
                onTimerCompleted()
            }
        }
    }

    fun pauseTimer() {
        _timerState.value = _timerState.value.copy(isRunning = false)
        timerJob?.cancel()
        audioEngine.pause()
    }

    fun resetTimer() {
        alertHelper.triggerClickHaptic()
        pauseTimer()
        val totalSecs = getModeDurationSeconds(_timerState.value.currentMode)
        _timerState.value = _timerState.value.copy(
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs
        )
    }

    fun skipTimer() {
        alertHelper.triggerClickHaptic()
        pauseTimer()
        advanceToNextMode(saveRecord = false)
    }

    fun switchMode(mode: TimerMode) {
        alertHelper.triggerClickHaptic()
        pauseTimer()
        val totalSecs = getModeDurationSeconds(mode)
        _timerState.value = _timerState.value.copy(
            currentMode = mode,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs
        )
    }

    fun selectActiveTask(task: TaskEntity?) {
        alertHelper.triggerClickHaptic()
        _timerState.value = _timerState.value.copy(
            activeTaskId = task?.id,
            activeTaskTitle = task?.title
        )
    }

    fun setAmbientSound(soundType: AmbientSoundType) {
        alertHelper.triggerClickHaptic()
        val newSound = if (_ambientSound.value == soundType || soundType == AmbientSoundType.NONE) {
            AmbientSoundType.NONE
        } else {
            soundType
        }
        _ambientSound.value = newSound
        if (newSound == AmbientSoundType.NONE) {
            audioEngine.stopAndRelease()
        } else {
            audioEngine.setSound(newSound)
        }
    }

    fun setAmbientVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _ambientVolume.value = clamped
        audioEngine.setVolume(clamped)
        if (clamped <= 0.01f) {
            _ambientSound.value = AmbientSoundType.NONE
            audioEngine.stopAndRelease()
        } else if (_timerState.value.isRunning && _ambientSound.value != AmbientSoundType.NONE) {
            audioEngine.play()
        }
    }

    fun toggleZenMode() {
        alertHelper.triggerClickHaptic()
        _isZenMode.value = !_isZenMode.value
    }

    fun startFocusMode() {
        alertHelper.triggerClickHaptic()
        if (!_timerState.value.isRunning) {
            startTimer()
        }
        _isZenMode.value = true
    }

    fun exitFocusMode() {
        alertHelper.triggerClickHaptic()
        pauseTimer()
        _isZenMode.value = false
    }

    private fun getModeDurationSeconds(mode: TimerMode): Int {
        val s = _settings.value
        val mins = when (mode) {
            TimerMode.FOCUS -> s.focusMinutes
            TimerMode.SHORT_BREAK -> s.shortBreakMinutes
            TimerMode.LONG_BREAK -> s.longBreakMinutes
        }
        return mins * 60
    }

    private fun onTimerCompleted() {
        pauseTimer()
        alertHelper.playSessionCompleteAlert(
            soundEnabled = _settings.value.soundEnabled,
            vibrationEnabled = _settings.value.vibrationEnabled
        )
        advanceToNextMode(saveRecord = true)
    }

    private fun advanceToNextMode(saveRecord: Boolean) {
        val current = _timerState.value
        val currentSettings = _settings.value

        if (saveRecord) {
            val sessionMinutes = when (current.currentMode) {
                TimerMode.FOCUS -> currentSettings.focusMinutes
                TimerMode.SHORT_BREAK -> currentSettings.shortBreakMinutes
                TimerMode.LONG_BREAK -> currentSettings.longBreakMinutes
            }

            viewModelScope.launch {
                repository.recordSession(
                    sessionType = current.currentMode.name,
                    durationMinutes = sessionMinutes,
                    taskId = if (current.currentMode == TimerMode.FOCUS) current.activeTaskId else null,
                    taskTitle = if (current.currentMode == TimerMode.FOCUS) current.activeTaskTitle else null
                )
            }
        }

        val nextMode: TimerMode
        var newCompletedCycles = current.completedCycles

        if (current.currentMode == TimerMode.FOCUS) {
            newCompletedCycles += 1
            nextMode = if (newCompletedCycles % currentSettings.sessionsBeforeLongBreak == 0) {
                TimerMode.LONG_BREAK
            } else {
                TimerMode.SHORT_BREAK
            }
        } else {
            nextMode = TimerMode.FOCUS
        }

        val totalSecs = getModeDurationSeconds(nextMode)
        _timerState.value = current.copy(
            currentMode = nextMode,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            completedCycles = newCompletedCycles,
            isRunning = false
        )

        if (currentSettings.autoStartNext) {
            startTimer()
        }
    }

    fun addTask(
        title: String,
        description: String = "",
        category: String = "",
        recurrence: String = "NONE",
        targetType: String = "SESSIONS",
        targetValue: Int = 5,
        scheduledTime: String? = null,
        priority: String = "NORMAL",
        estimatedPomodoros: Int = 2,
        initialSubtasks: List<String> = emptyList(),
        tags: String = ""
    ) {
        if (title.isBlank()) return
        alertHelper.triggerClickHaptic()
        if (category.isNotBlank()) {
            addCustomCategory(category)
        }
        viewModelScope.launch {
            val subtasks = initialSubtasks.filter { it.isNotBlank() }.map { SubtaskItem(title = it.trim()) }
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                recurrence = recurrence,
                targetType = targetType,
                targetValue = targetValue,
                scheduledTime = scheduledTime,
                subtasksJson = SubtaskConverter.toJson(subtasks),
                priority = priority,
                estimatedPomodoros = estimatedPomodoros,
                tags = tags.trim()
            )
            val newId = repository.insertTask(task)
            if (_timerState.value.activeTaskId == null) {
                _timerState.value = _timerState.value.copy(
                    activeTaskId = newId,
                    activeTaskTitle = task.title
                )
            }
        }
    }

    fun logSessionForTask(task: TaskEntity, durationMinutes: Int = 25) {
        alertHelper.triggerClickHaptic()
        viewModelScope.launch {
            repository.recordSession(
                sessionType = "FOCUS",
                durationMinutes = durationMinutes,
                taskId = task.id,
                taskTitle = task.title
            )
        }
    }

    fun resetRecurringTask(task: TaskEntity) {
        alertHelper.triggerClickHaptic()
        val resetSubtasks = task.getSubtasks().map { it.copy(isCompleted = false) }
        val updatedTask = task.copy(
            isCompleted = false,
            completedPomodoros = 0,
            subtasksJson = SubtaskConverter.toJson(resetSubtasks)
        )
        viewModelScope.launch {
            repository.updateTask(updatedTask)
        }
    }

    fun addSubtaskToTask(task: TaskEntity, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        alertHelper.triggerClickHaptic()
        val currentSubtasks = task.getSubtasks().toMutableList()
        currentSubtasks.add(SubtaskItem(title = subtaskTitle.trim()))
        val updatedTask = task.copy(subtasksJson = SubtaskConverter.toJson(currentSubtasks))
        viewModelScope.launch {
            repository.updateTask(updatedTask)
        }
    }

    fun toggleSubtask(task: TaskEntity, subtaskId: String) {
        alertHelper.triggerClickHaptic()
        val currentSubtasks = task.getSubtasks().map {
            if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        val updatedTask = task.copy(subtasksJson = SubtaskConverter.toJson(currentSubtasks))
        viewModelScope.launch {
            repository.updateTask(updatedTask)
        }
    }

    fun deleteSubtask(task: TaskEntity, subtaskId: String) {
        alertHelper.triggerClickHaptic()
        val currentSubtasks = task.getSubtasks().filter { it.id != subtaskId }
        val updatedTask = task.copy(subtasksJson = SubtaskConverter.toJson(currentSubtasks))
        viewModelScope.launch {
            repository.updateTask(updatedTask)
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        alertHelper.triggerClickHaptic()
        val willBeCompleted = !task.isCompleted
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, willBeCompleted)
            if (_timerState.value.activeTaskId == task.id && willBeCompleted) {
                _timerState.value = _timerState.value.copy(
                    activeTaskId = null,
                    activeTaskTitle = null
                )
            }
            if (willBeCompleted) {
                val quote = _appStrings.value.motivationalQuotes.random()
                _celebrationMessage.value = quote
                delay(3500L)
                if (_celebrationMessage.value == quote) {
                    _celebrationMessage.value = null
                }
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        alertHelper.triggerClickHaptic()
        viewModelScope.launch {
            repository.deleteTask(task)
            if (_timerState.value.activeTaskId == task.id) {
                _timerState.value = _timerState.value.copy(
                    activeTaskId = null,
                    activeTaskTitle = null
                )
            }
        }
    }

    fun updateSettings(newSettings: PomodoroSettings) {
        _settings.value = newSettings
        _appStrings.value = LocalizationManager.getStrings(newSettings.language)
        preferences.saveSettings(newSettings)
        if (!_timerState.value.isRunning) {
            val totalSecs = getModeDurationSeconds(_timerState.value.currentMode)
            _timerState.value = _timerState.value.copy(
                totalSeconds = totalSecs,
                remainingSeconds = totalSecs
            )
        }
    }

    fun setLanguage(languageCode: String) {
        val updated = _settings.value.copy(language = languageCode)
        updateSettings(updated)
    }

    fun setThemeMode(themeModeCode: String) {
        val updated = _settings.value.copy(themeMode = themeModeCode)
        updateSettings(updated)
    }

    fun addCustomCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            val current = _customCategories.value.toMutableSet()
            current.add(trimmed)
            val sorted = current.toList().sorted()
            _customCategories.value = sorted
            preferences.saveCustomCategories(current)
        }
    }

    fun deleteCustomCategory(name: String) {
        val current = _customCategories.value.toMutableSet()
        current.remove(name)
        val sorted = current.toList().sorted()
        _customCategories.value = sorted
        preferences.saveCustomCategories(current)
    }

    fun getStreakDays(sessions: List<SessionLogEntity>): Int =
        computeStreakDays(sessions)

    fun getWeeklyStats(sessions: List<SessionLogEntity>): List<DayFocusStat> {
        val calendar = Calendar.getInstance()
        val strings = _appStrings.value
        val dayNames = strings.chartDaysOfWeek
        val result = mutableListOf<DayFocusStat>()

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000

            val dayFocusMins = sessions
                .filter { it.sessionType == "FOCUS" && it.completedAt in startOfDay until endOfDay }
                .sumOf { it.durationMinutes }

            val dayOfWeek = (dayCal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, dayNames.lastIndex)
            val label = if (i == 0) strings.chartTodayLabel else dayNames[dayOfWeek]
            result.add(DayFocusStat(dayLabel = label, focusMinutes = dayFocusMins, dayTimestamp = startOfDay))
        }

        return result
    }

    private fun computeBadges(
        sessions: List<SessionLogEntity>,
        tasks: List<TaskEntity>,
        strings: com.example.util.AppStrings = _appStrings.value
    ): List<BadgeItem> {
        val focusSessions = sessions.filter { it.sessionType == "FOCUS" }
        val totalSessions = focusSessions.size
        val totalMinutes = focusSessions.sumOf { it.durationMinutes }
        val completedTasks = tasks.count { it.isCompleted }
        val streak = getStreakDays(sessions)

        return listOf(
            BadgeItem(
                id = "FIRST_STEP",
                title = strings.badgeFirstStepTitle,
                description = strings.badgeFirstStepDesc,
                emoji = "",
                isUnlocked = totalSessions >= 1,
                currentProgress = totalSessions.coerceAtMost(1),
                maxProgress = 1
            ),
            BadgeItem(
                id = "FOCUS_MASTER",
                title = strings.badgeFocusMasterTitle,
                description = strings.badgeFocusMasterDesc,
                emoji = "",
                isUnlocked = totalSessions >= 4,
                currentProgress = totalSessions.coerceAtMost(4),
                maxProgress = 4
            ),
            BadgeItem(
                id = "MARATHON",
                title = strings.badgeMarathonTitle,
                description = strings.badgeMarathonDesc,
                emoji = "",
                isUnlocked = totalMinutes >= 100,
                currentProgress = totalMinutes.coerceAtMost(100),
                maxProgress = 100
            ),
            BadgeItem(
                id = "TASK_HUNTER",
                title = strings.badgeTaskHunterTitle,
                description = strings.badgeTaskHunterDesc,
                emoji = "",
                isUnlocked = completedTasks >= 3,
                currentProgress = completedTasks.coerceAtMost(3),
                maxProgress = 3
            ),
            BadgeItem(
                id = "STREAK_WARRIOR",
                title = strings.badgeStreakWarriorTitle,
                description = strings.badgeStreakWarriorDesc,
                emoji = "",
                isUnlocked = streak >= 2,
                currentProgress = streak.coerceAtMost(2),
                maxProgress = 2
            ),
            BadgeItem(
                id = "ZEN_MIND",
                title = strings.badgeZenMindTitle,
                description = strings.badgeZenMindDesc,
                emoji = "",
                isUnlocked = totalSessions >= 10,
                currentProgress = totalSessions.coerceAtMost(10),
                maxProgress = 10
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopAndRelease()
    }
}

fun computeStreakDays(
    sessions: List<SessionLogEntity>,
    nowMillis: Long = System.currentTimeMillis()
): Int {
    val focusSessions = sessions.filter { it.sessionType == "FOCUS" }
    if (focusSessions.isEmpty()) return 0

    val requiredSessionsPerDay = 4
    val calendar = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    var streak = 0

    // Check today (requires 4 completed focus sessions to count as a streak day)
    val checkStart = calendar.timeInMillis
    val checkEnd = checkStart + 24 * 60 * 60 * 1000

    val todaySessionCount = focusSessions.count { it.completedAt in checkStart until checkEnd }
    if (todaySessionCount >= requiredSessionsPerDay) {
        streak++
    }

    // Check backward consecutive days that each reached 4 completed focus sessions
    var daysBack = 1
    while (true) {
        val pastCal = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            add(Calendar.DAY_OF_YEAR, -daysBack)
        }
        val start = pastCal.timeInMillis
        val end = start + 24 * 60 * 60 * 1000

        val dayCount = focusSessions.count { it.completedAt in start until end }
        if (dayCount >= requiredSessionsPerDay) {
            streak++
            daysBack++
        } else {
            break
        }
    }
    return streak
}

class PomodoroViewModelFactory(
    private val repository: FocusRepository,
    private val alertHelper: SoundAndVibrationHelper,
    private val audioEngine: AmbientAudioEngine,
    private val preferences: SettingsPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            return PomodoroViewModel(repository, alertHelper, audioEngine, preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
