package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.PomodoroSettings

class SettingsPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("focusflow_user_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_FOCUS_MINUTES = "pref_focus_minutes"
        private const val KEY_SHORT_BREAK_MINUTES = "pref_short_break_minutes"
        private const val KEY_LONG_BREAK_MINUTES = "pref_long_break_minutes"
        private const val KEY_SESSIONS_BEFORE_LONG_BREAK = "pref_sessions_before_long_break"
        private const val KEY_DAILY_GOAL = "pref_daily_goal"
        private const val KEY_AUTO_START_NEXT = "pref_auto_start_next"
        private const val KEY_SOUND_ENABLED = "pref_sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "pref_vibration_enabled"
        private const val KEY_CUSTOM_CATEGORIES = "pref_custom_categories"
        private const val KEY_USER_NICKNAME = "pref_user_nickname"
        private const val KEY_USER_AVATAR = "pref_user_avatar"
        private const val KEY_USER_AVATAR_BG = "pref_user_avatar_bg"
        private const val KEY_USER_PHOTO_PATH = "pref_user_photo_path"
        private const val KEY_THEME_MODE = "pref_theme_mode"
    }

    fun loadSettings(): PomodoroSettings {
        return PomodoroSettings(
            language = prefs.getString(KEY_LANGUAGE, "tr") ?: "tr",
            themeMode = prefs.getString(KEY_THEME_MODE, "system") ?: "system",
            focusMinutes = prefs.getInt(KEY_FOCUS_MINUTES, 50),
            shortBreakMinutes = prefs.getInt(KEY_SHORT_BREAK_MINUTES, 10),
            longBreakMinutes = prefs.getInt(KEY_LONG_BREAK_MINUTES, 20),
            sessionsBeforeLongBreak = prefs.getInt(KEY_SESSIONS_BEFORE_LONG_BREAK, 4),
            dailyGoalPomodoros = prefs.getInt(KEY_DAILY_GOAL, 4),
            autoStartNext = prefs.getBoolean(KEY_AUTO_START_NEXT, false),
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        )
    }

    fun saveSettings(settings: PomodoroSettings) {
        prefs.edit()
            .putString(KEY_LANGUAGE, settings.language)
            .putString(KEY_THEME_MODE, settings.themeMode)
            .putInt(KEY_FOCUS_MINUTES, settings.focusMinutes)
            .putInt(KEY_SHORT_BREAK_MINUTES, settings.shortBreakMinutes)
            .putInt(KEY_LONG_BREAK_MINUTES, settings.longBreakMinutes)
            .putInt(KEY_SESSIONS_BEFORE_LONG_BREAK, settings.sessionsBeforeLongBreak)
            .putInt(KEY_DAILY_GOAL, settings.dailyGoalPomodoros)
            .putBoolean(KEY_AUTO_START_NEXT, settings.autoStartNext)
            .putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, settings.vibrationEnabled)
            .apply()
    }

    fun loadCustomCategories(): Set<String> {
        return prefs.getStringSet(KEY_CUSTOM_CATEGORIES, emptySet()) ?: emptySet()
    }

    fun saveCustomCategories(categories: Set<String>) {
        prefs.edit()
            .putStringSet(KEY_CUSTOM_CATEGORIES, categories)
            .apply()
    }

    fun loadUserProfile(defaultNickname: String = "Odak Ustası"): com.example.data.model.UserProfile {
        return com.example.data.model.UserProfile(
            nickname = prefs.getString(KEY_USER_NICKNAME, defaultNickname) ?: defaultNickname,
            avatarEmoji = prefs.getString(KEY_USER_AVATAR, "🧘") ?: "🧘",
            avatarColorIndex = prefs.getInt(KEY_USER_AVATAR_BG, 0),
            customPhotoPath = prefs.getString(KEY_USER_PHOTO_PATH, null)
        )
    }

    fun saveUserProfile(profile: com.example.data.model.UserProfile) {
        prefs.edit()
            .putString(KEY_USER_NICKNAME, profile.nickname)
            .putString(KEY_USER_AVATAR, profile.avatarEmoji)
            .putInt(KEY_USER_AVATAR_BG, profile.avatarColorIndex)
            .putString(KEY_USER_PHOTO_PATH, profile.customPhotoPath)
            .apply()
    }
}

