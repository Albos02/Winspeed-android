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

    @Delete
    suspend fun deleteSession(session: SailingSession)

    @Query("SELECT * FROM sailing_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SailingSession>>

    @Query("SELECT * FROM sailing_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): SailingSession?

    // Points
    @Insert
    suspend fun insertPoint(point: SailingPointEntity)

    @Insert
    suspend fun insertPoints(points: List<SailingPointEntity>)

    @Query("SELECT * FROM sailing_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSession(sessionId: Long): Flow<List<SailingPointEntity>>

    @Query("DELETE FROM sailing_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: Long)
}
