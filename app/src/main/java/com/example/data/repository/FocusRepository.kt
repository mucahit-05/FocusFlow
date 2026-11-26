package com.example.data.repository

import com.example.data.local.SessionDao
import com.example.data.local.TaskDao
import com.example.data.model.SessionLogEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

class FocusRepository(
    private val taskDao: TaskDao,
    private val sessionDao: SessionDao
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()

    val allSessions: Flow<List<SessionLogEntity>> = sessionDao.getAllSessions()
    val totalFocusMinutes: Flow<Int?> = sessionDao.getTotalFocusMinutes()
    val totalFocusSessionsCount: Flow<Int> = sessionDao.getTotalFocusSessionsCount()

    fun getRecentSessions(limit: Int): Flow<List<SessionLogEntity>> =
        sessionDao.getRecentSessions(limit)

    fun getSessionsSince(timestamp: Long): Flow<List<SessionLogEntity>> =
        sessionDao.getSessionsSince(timestamp)

    fun getFocusMinutesSince(timestamp: Long): Flow<Int?> =
        sessionDao.getFocusMinutesSince(timestamp)

    suspend fun getTaskById(taskId: Long): TaskEntity? =
        taskDao.getTaskById(taskId)

    suspend fun insertTask(task: TaskEntity): Long =
        taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) =
        taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) =
        taskDao.deleteTask(task)

    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) =
        taskDao.setTaskCompleted(taskId, completed)

    suspend fun cleanupLegacyWorkCategory() =
        taskDao.cleanupLegacyWorkCategory()

    suspend fun recordSession(
        sessionType: String,
        durationMinutes: Int,
        taskId: Long? = null,
        taskTitle: String? = null
    ): Long {
        val session = SessionLogEntity(
            taskId = taskId,
            taskTitle = taskTitle,
            sessionType = sessionType,
            durationMinutes = durationMinutes,
            completedAt = System.currentTimeMillis()
        )
        val id = sessionDao.insertSession(session)
        if (taskId != null && sessionType == "FOCUS") {
            taskDao.incrementCompletedPomodoro(taskId)
        }
        return id
    }
}
