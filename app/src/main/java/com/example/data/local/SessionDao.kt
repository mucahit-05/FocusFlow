package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SessionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM session_logs ORDER BY completedAt DESC")
    fun getAllSessions(): Flow<List<SessionLogEntity>>

    @Query("SELECT * FROM session_logs ORDER BY completedAt DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<SessionLogEntity>>

    @Query("SELECT * FROM session_logs WHERE completedAt >= :sinceTimestamp ORDER BY completedAt DESC")
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<SessionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionLogEntity): Long

    @Query("SELECT SUM(durationMinutes) FROM session_logs WHERE sessionType = 'FOCUS'")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM session_logs WHERE sessionType = 'FOCUS' AND completedAt >= :sinceTimestamp")
    fun getFocusMinutesSince(sinceTimestamp: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM session_logs WHERE sessionType = 'FOCUS'")
    fun getTotalFocusSessionsCount(): Flow<Int>
}
