package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.GameRecord
import com.example.model.SavedTicketEntity

@Database(
    entities = [GameRecord::class, SavedTicketEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TambolaDatabase : RoomDatabase() {
    abstract fun tambolaDao(): TambolaDao

    companion object {
        @Volatile
        private var INSTANCE: TambolaDatabase? = null

        fun getDatabase(context: Context): TambolaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TambolaDatabase::class.java,
                    "tambola_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
