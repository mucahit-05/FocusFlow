package com.example.util

import com.example.data.model.SessionLogEntity
import com.example.data.model.TaskEntity
import java.util.Calendar
import java.util.Locale

data class TaskPeriodProgress(
    val completedSessionsInPeriod: Int,
    val completedMinutesInPeriod: Int,
    val targetValue: Int,
    val targetType: String, // "SESSIONS" or "DURATION_HOURS"
    val progressFraction: Float,
    val isGoalCompleted: Boolean,
    val progressDisplayText: String,
    val remainingDisplayText: String,
    val periodResetNotice: String
)

object TaskRecurrenceHelper {

    fun getStartOfCurrentPeriod(recurrence: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return when (recurrence) {
            "DAILY" -> calendar.timeInMillis
            "WEEKLY" -> {
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                calendar.timeInMillis
            }
            "MONTHLY" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            else -> 0L
        }
    }

    fun computeTaskProgress(
        task: TaskEntity,
        sessions: List<SessionLogEntity>,
        strings: AppStrings
    ): TaskPeriodProgress {
        val isRecurring = task.recurrence != "NONE"
        val startOfPeriod = getStartOfCurrentPeriod(task.recurrence)

        val relevantSessions = sessions.filter {
            it.taskId == task.id && it.sessionType == "FOCUS" && it.completedAt >= startOfPeriod
        }

        val completedSessions = if (isRecurring) {
            relevantSessions.size
        } else {
            maxOf(task.completedPomodoros, relevantSessions.size)
        }

        val completedMinutes = relevantSessions.sumOf { it.durationMinutes }

        val targetVal = if (isRecurring) {
            if (task.targetValue > 0) task.targetValue else 5
        } else {
            if (task.estimatedPomodoros > 0) task.estimatedPomodoros else 2
        }

        val targetType = if (isRecurring) task.targetType else "SESSIONS"

        val isGoalCompleted: Boolean
        val fraction: Float
        val progressText: String
        val remainingText: String

        if (targetType == "DURATION_HOURS") {
            val targetMinutes = targetVal * 60
            fraction = if (targetMinutes > 0) (completedMinutes.toFloat() / targetMinutes.toFloat()).coerceIn(0f, 1f) else 0f
            isGoalCompleted = completedMinutes >= targetMinutes

            val compHoursFloat = completedMinutes / 60.0
            val formattedComp = if (compHoursFloat % 1.0 == 0.0) {
                "${compHoursFloat.toInt()}"
            } else {
                String.format(Locale.US, "%.1f", compHoursFloat)
            }
            progressText = "$formattedComp / $targetVal ${strings.targetTypeHours}"

            val remainingMins = (targetMinutes - completedMinutes).coerceAtLeast(0)
            val remHours = remainingMins / 60
            val remMins = remainingMins % 60
            remainingText = if (remHours > 0) {
                "$remHours sa $remMins dk ${strings.remainingText}"
            } else {
                "$remMins dk ${strings.remainingText}"
            }
        } else {
            fraction = if (targetVal > 0) (completedSessions.toFloat() / targetVal.toFloat()).coerceIn(0f, 1f) else 0f
            isGoalCompleted = if (isRecurring) completedSessions >= targetVal else task.isCompleted

            progressText = "$completedSessions / $targetVal ${strings.targetTypeSessions}"
            val remCount = (targetVal - completedSessions).coerceAtLeast(0)
            remainingText = "$remCount ${strings.targetTypeSessions} ${strings.remainingText}"
        }

        val resetNotice = when (task.recurrence) {
            "WEEKLY" -> strings.goalResetsMonday
            "MONTHLY" -> strings.goalResetsFirstOfMonth
            "DAILY" -> strings.goalResetsTomorrow
            else -> ""
        }

        return TaskPeriodProgress(
            completedSessionsInPeriod = completedSessions,
            completedMinutesInPeriod = completedMinutes,
            targetValue = targetVal,
            targetType = targetType,
            progressFraction = fraction,
            isGoalCompleted = isGoalCompleted,
            progressDisplayText = progressText,
            remainingDisplayText = remainingText,
            periodResetNotice = resetNotice
        )
    }
}
