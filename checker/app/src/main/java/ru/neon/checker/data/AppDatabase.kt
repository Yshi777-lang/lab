package ru.neon.checker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CheckRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkDao(): CheckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neon_checker"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
