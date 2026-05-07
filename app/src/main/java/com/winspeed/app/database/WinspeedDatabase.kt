package com.winspeed.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.winspeed.app.database.entities.SailingPointEntity
import com.winspeed.app.database.entities.SailingSession

@Database(entities = [SailingSession::class, SailingPointEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WinspeedDatabase : RoomDatabase() {

    abstract fun sailingDao(): SailingDao

    companion object {
        @Volatile
        private var INSTANCE: WinspeedDatabase? = null

        fun getDatabase(context: Context): WinspeedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WinspeedDatabase::class.java,
                    "winspeed_database"
                )
                .fallbackToDestructiveMigration() // Useful during development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
