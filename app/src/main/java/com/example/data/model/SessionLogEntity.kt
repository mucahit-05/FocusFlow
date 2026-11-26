package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val sessionType: String, // "FOCUS", "SHORT_BREAK", "LONG_BREAK"
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis()
)
