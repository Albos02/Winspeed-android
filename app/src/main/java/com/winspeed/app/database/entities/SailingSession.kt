package com.winspeed.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sailing_sessions")
data class SailingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val name: String? = null,
    val maxSpeedKnots: Float = 0f,
    val avgSpeedKnots: Float = 0f,
    val pointCount: Int = 0,
    val lastWindDirection: Float? = null
)
