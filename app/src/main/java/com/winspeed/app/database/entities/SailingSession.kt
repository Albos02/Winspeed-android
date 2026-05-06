package com.winspeed.app.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sailing_sessions")
data class SailingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val name: String? = null
)
