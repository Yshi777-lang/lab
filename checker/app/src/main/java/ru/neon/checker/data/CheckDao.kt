package ru.neon.checker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckDao {
    @Insert
    suspend fun insert(record: CheckRecord)

    @Query("SELECT * FROM checks ORDER BY timestamp DESC LIMIT 100")
    fun getAll(): Flow<List<CheckRecord>>

    @Query("DELETE FROM checks")
    suspend fun clear()
}
