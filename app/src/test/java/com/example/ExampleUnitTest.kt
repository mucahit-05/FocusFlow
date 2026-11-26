package com.example

import com.example.data.model.SessionLogEntity
import com.example.data.model.TaskEntity
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.util.TaskRecurrenceHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLocalizationTop5Languages() {
        val languages = listOf("tr", "en", "es", "zh", "hi")
        for (code in languages) {
            val strings = LocalizationManager.getStrings(code)
            assertTrue(strings.appTitle.isNotBlank())
            assertTrue(strings.tabTimer.isNotBlank())
            assertTrue(strings.tabTasks.isNotBlank())
            assertTrue(strings.tabStats.isNotBlank())
            assertTrue(strings.goalCompletedBanner.isNotBlank())
            assertTrue(strings.ambientTitle.isNotBlank())
            assertTrue(strings.ambientMuteButton.isNotBlank())
            assertTrue(strings.timerStatusFocusing.isNotBlank())
            assertTrue(strings.timerStatusBreak.isNotBlank())
            assertTrue(strings.timerStatusPaused.isNotBlank())
            assertTrue(strings.deleteTask.isNotBlank())
            assertTrue(strings.addNewCategoryDialogTitle.isNotBlank())
            assertTrue(strings.ambientDrawerTitle.isNotBlank())
            assertTrue(strings.ambientSoundOcean.isNotBlank())
            assertTrue(strings.splashSubtitle.isNotBlank())
            assertTrue(strings.badgeLockedLabel.isNotBlank())
            assertTrue(strings.badgeProgressLabel.isNotBlank())
            assertTrue(strings.badgeEarnedHint.isNotBlank())
            assertTrue(strings.badgeLockedHint.isNotBlank())
            assertTrue(strings.badgeCloseDesc.isNotBlank())
            assertTrue(strings.settingsThemeSection.isNotBlank())
            assertTrue(strings.themeLight.isNotBlank())
            assertTrue(strings.themeBlack.isNotBlank())
            assertTrue(strings.themeSystem.isNotBlank())
            assertTrue(strings.profileLevelsTitle.isNotBlank())
            assertTrue(strings.profileLevelTapHint.isNotBlank())
            assertTrue(strings.zenExitConfirmTitle.isNotBlank())
            assertTrue(strings.zenExitConfirmMessage.isNotBlank())
            assertTrue(strings.zenExitConfirmYes.isNotBlank())
            assertTrue(strings.zenExitConfirmNo.isNotBlank())
            assertTrue(strings.zenFocusQuotes.isNotEmpty())
        }
        assertEquals("Odaklan", LocalizationManager.getStrings("tr").timerStart)
        assertEquals("Focus", LocalizationManager.getStrings("en").timerStart)
        assertEquals("Evet", LocalizationManager.getStrings("tr").zenExitConfirmYes)
        assertEquals("Hayır", LocalizationManager.getStrings("tr").zenExitConfirmNo)
    }

    @Test
    fun testWeeklyTaskProgressCalculation() {
        val strings = LocalizationManager.getStrings("tr")
        val task = TaskEntity(
            id = 100,
            title = "Matematik Çalışması",
            category = "Matematik",
            recurrence = "WEEKLY",
            targetType = "SESSIONS",
            targetValue = 5
        )

        val sessions = listOf(
            SessionLogEntity(id = 1, taskId = 100, sessionType = "FOCUS", durationMinutes = 50, completedAt = System.currentTimeMillis()),
            SessionLogEntity(id = 2, taskId = 100, sessionType = "FOCUS", durationMinutes = 50, completedAt = System.currentTimeMillis()),
            SessionLogEntity(id = 3, taskId = 100, sessionType = "FOCUS", durationMinutes = 50, completedAt = System.currentTimeMillis())
        )

        val progress = TaskRecurrenceHelper.computeTaskProgress(task, sessions, strings)
        assertEquals(3, progress.completedSessionsInPeriod)
        assertEquals(5, progress.targetValue)
        assertEquals(0.6f, progress.progressFraction, 0.01f)
        assertFalse(progress.isGoalCompleted)

        // Adding 2 more sessions to complete the weekly goal
        val completedSessions = sessions + listOf(
            SessionLogEntity(id = 4, taskId = 100, sessionType = "FOCUS", durationMinutes = 50, completedAt = System.currentTimeMillis()),
            SessionLogEntity(id = 5, taskId = 100, sessionType = "FOCUS", durationMinutes = 50, completedAt = System.currentTimeMillis())
        )

        val completedProgress = TaskRecurrenceHelper.computeTaskProgress(task, completedSessions, strings)
        assertEquals(5, completedProgress.completedSessionsInPeriod)
        assertEquals(1.0f, completedProgress.progressFraction, 0.01f)
        assertTrue(completedProgress.isGoalCompleted)
    }

    @Test
    fun testWeeklyHourlyTaskProgress() {
        val strings = LocalizationManager.getStrings("tr")
        val task = TaskEntity(
            id = 200,
            title = "Haftalık 5 Saat Matematik",
            category = "Matematik",
            recurrence = "WEEKLY",
            targetType = "DURATION_HOURS",
            targetValue = 5 // 5 hours = 300 minutes
        )

        val sessions = listOf(
            SessionLogEntity(id = 1, taskId = 200, sessionType = "FOCUS", durationMinutes = 60, completedAt = System.currentTimeMillis()),
            SessionLogEntity(id = 2, taskId = 200, sessionType = "FOCUS", durationMinutes = 120, completedAt = System.currentTimeMillis())
        )

        val progress = TaskRecurrenceHelper.computeTaskProgress(task, sessions, strings)
        assertEquals(180, progress.completedMinutesInPeriod) // 3 hours
        assertEquals(0.6f, progress.progressFraction, 0.01f)
        assertFalse(progress.isGoalCompleted)
    }

    @Test
    fun testTaskTagsExtraction() {
        val task = TaskEntity(
            title = "Proje Geliştirme",
            tags = "acil, kodlama, #android, test"
        )
        val tags = task.getTagsList()
        assertEquals(4, tags.size)
        assertEquals("acil", tags[0])
        assertEquals("kodlama", tags[1])
        assertEquals("android", tags[2]) // Strips leading '#'
        assertEquals("test", tags[3])
    }

    @Test
    fun testUserProfileCustomPhoto() {
        val defaultProfile = com.example.data.model.UserProfile(
            nickname = "Ahmet",
            avatarEmoji = "🚀",
            customPhotoPath = null
        )
        assertEquals(null, defaultProfile.customPhotoPath)
        assertEquals("🚀", defaultProfile.avatarEmoji)

        val updatedProfile = defaultProfile.copy(
            customPhotoPath = "/data/user/0/com.aistudio.focusflow/files/user_profile_avatar.jpg"
        )
        assertEquals("/data/user/0/com.aistudio.focusflow/files/user_profile_avatar.jpg", updatedProfile.customPhotoPath)
    }

    @Test
    fun testProfileLevelProgressionTiers() {
        val strings = LocalizationManager.getStrings("tr")

        val lv1 = com.example.util.ProfileLevelHelper.computeProgress(2, strings)
        assertEquals(5, lv1.allTiers.size)
        assertEquals(1, lv1.currentTier.level)
        assertEquals(2, lv1.nextTier?.level)
        assertEquals(2, lv1.remainingSessionsToNext)
        assertEquals(0.5f, lv1.progressFraction, 0.01f)

        val lv3 = com.example.util.ProfileLevelHelper.computeProgress(10, strings)
        assertEquals(3, lv3.currentTier.level)
        assertEquals(4, lv3.nextTier?.level)
        assertEquals(15, lv3.remainingSessionsToNext)
        assertEquals(0.0f, lv3.progressFraction, 0.01f)

        val lv5 = com.example.util.ProfileLevelHelper.computeProgress(62, strings)
        assertEquals(5, lv5.currentTier.level)
        assertEquals(null, lv5.nextTier)
        assertEquals(0, lv5.remainingSessionsToNext)
        assertEquals(1.0f, lv5.progressFraction, 0.01f)
    }

    @Test
    fun testAppThemeModeParsing() {
        assertEquals(com.example.ui.theme.AppThemeMode.LIGHT, com.example.ui.theme.AppThemeMode.fromCode("light"))
        assertEquals(com.example.ui.theme.AppThemeMode.BLACK, com.example.ui.theme.AppThemeMode.fromCode("black"))
        assertEquals(com.example.ui.theme.AppThemeMode.SYSTEM, com.example.ui.theme.AppThemeMode.fromCode("system"))
        assertEquals(com.example.ui.theme.AppThemeMode.SYSTEM, com.example.ui.theme.AppThemeMode.fromCode("unknown"))
    }

    @Test
    fun testFourSessionsPerDayStreakRuleAndFlameLevels() {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis
        val oneDayMs = 24L * 60L * 60L * 1000L

        // 3 focus sessions today -> 0 streak (requires 4)
        val threeToday = (1..3).map { idx ->
            SessionLogEntity(id = idx.toLong(), sessionType = "FOCUS", durationMinutes = 25, completedAt = now)
        }
        assertEquals(0, com.example.ui.computeStreakDays(threeToday, nowMillis = now))

        // 4 focus sessions today -> 1 streak
        val fourToday = threeToday + SessionLogEntity(id = 4, sessionType = "FOCUS", durationMinutes = 25, completedAt = now)
        assertEquals(1, com.example.ui.computeStreakDays(fourToday, nowMillis = now))

        // 4 yesterday + 3 today -> preserves yesterday's streak = 1
        val fourYesterdayThreeToday = (1..4).map { idx ->
            SessionLogEntity(id = (10 + idx).toLong(), sessionType = "FOCUS", durationMinutes = 25, completedAt = now - oneDayMs)
        } + threeToday
        assertEquals(1, com.example.ui.computeStreakDays(fourYesterdayThreeToday, nowMillis = now))

        // 4 yesterday + 4 today -> 2 streak
        val twoDaysStreak = (1..4).map { idx ->
            SessionLogEntity(id = (10 + idx).toLong(), sessionType = "FOCUS", durationMinutes = 25, completedAt = now - oneDayMs)
        } + fourToday
        assertEquals(2, com.example.ui.computeStreakDays(twoDaysStreak, nowMillis = now))

        // Flame level mapping tiers
        assertEquals(0, com.example.ui.components.streakToFlameLevel(0))
        assertEquals(1, com.example.ui.components.streakToFlameLevel(1))
        assertEquals(1, com.example.ui.components.streakToFlameLevel(2))
        assertEquals(2, com.example.ui.components.streakToFlameLevel(3))
        assertEquals(2, com.example.ui.components.streakToFlameLevel(6))
        assertEquals(3, com.example.ui.components.streakToFlameLevel(7))
        assertEquals(3, com.example.ui.components.streakToFlameLevel(13))
        assertEquals(4, com.example.ui.components.streakToFlameLevel(14))
    }
}
