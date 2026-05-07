package com.winspeed.app.database

import com.winspeed.app.database.entities.SailingPointEntity
import com.winspeed.app.database.entities.SailingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SessionsRepository(private val dao: SailingDao) {
    
    fun getAllSessions(): Flow<List<SailingSession>> = dao.getAllSessions()
    
    suspend fun getSessionById(sessionId: Long): SailingSession? = dao.getSessionById(sessionId)
    
    suspend fun getIncompleteSession(): SailingSession? = dao.getIncompleteSession()
    
    fun getPointsForSession(sessionId: Long): Flow<List<SailingPointEntity>> = dao.getPointsForSession(sessionId)
    
    suspend fun getPointsOnce(sessionId: Long): List<SailingPointEntity> {
        return dao.getPointsForSession(sessionId).first()
    }
    
    suspend fun cleanupAbandonedSessions(currentSessionId: Long?) {
        val abandoned = dao.getIncompleteSessions()
        abandoned.forEach { session ->
            if (session.id != currentSessionId) {
                val pointCount = dao.getPointCount(session.id)
                val lastTimestamp = dao.getLastPointTimestamp(session.id) ?: session.startTime
                val maxSpeed = dao.getMaxSpeed(session.id) ?: 0f
                val avgSpeed = dao.getAvgSpeed(session.id) ?: 0f
                
                // Heuristic for wind direction in abandoned sessions
                val lastPoint = dao.getPointsForSession(session.id).first().lastOrNull()
                val windDir = lastPoint?.headingDegrees
                
                dao.updateSession(session.copy(
                    endTime = lastTimestamp,
                    pointCount = pointCount,
                    maxSpeedKnots = maxSpeed,
                    avgSpeedKnots = avgSpeed,
                    lastWindDirection = windDir
                ))
            }
        }
    }
    
    suspend fun endSession(sessionId: Long) {
        dao.endSession(sessionId, System.currentTimeMillis())
    }
    
    suspend fun deleteSession(session: SailingSession) {
        dao.deletePointsForSession(session.id)
        dao.deleteSession(session)
    }
}