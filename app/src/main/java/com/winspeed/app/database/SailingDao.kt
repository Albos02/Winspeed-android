package com.winspeed.app.database

import androidx.room.*
import com.winspeed.app.database.entities.SailingPointEntity
import com.winspeed.app.database.entities.SailingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SailingDao {
    // Sessions
    @Insert
    suspend fun insertSession(session: SailingSession): Long

    @Update
    suspend fun updateSession(session: SailingSession)

    @Query("UPDATE sailing_sessions SET endTime = :endTime WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endTime: Long)

    @Delete
    suspend fun deleteSession(session: SailingSession)

    @Query("SELECT * FROM sailing_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SailingSession>>

    @Query("SELECT * FROM sailing_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SailingSession?

    @Query("SELECT * FROM sailing_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): SailingSession?

    @Query("SELECT * FROM sailing_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getIncompleteSession(): SailingSession?

    // Points
    @Insert
    suspend fun insertPoint(point: SailingPointEntity)

    @Insert
    suspend fun insertPoints(points: List<SailingPointEntity>)

    @Query("SELECT * FROM sailing_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSession(sessionId: Long): Flow<List<SailingPointEntity>>

    @Query("DELETE FROM sailing_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM sailing_points WHERE sessionId = :sessionId")
    suspend fun getPointCount(sessionId: Long): Int

    @Query("SELECT MAX(speedKnots) FROM sailing_points WHERE sessionId = :sessionId")
    suspend fun getMaxSpeed(sessionId: Long): Float?

    @Query("SELECT AVG(speedKnots) FROM sailing_points WHERE sessionId = :sessionId")
    suspend fun getAvgSpeed(sessionId: Long): Float?
    @Query("SELECT * FROM sailing_sessions WHERE endTime IS NULL")
    suspend fun getIncompleteSessions(): List<SailingSession>

    @Query("SELECT timestamp FROM sailing_points WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPointTimestamp(sessionId: Long): Long?
}
