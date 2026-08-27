package ru.neon.checker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checks")
data class CheckRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val listType: String,
    val status: Boolean,
    val latencyMs: Int,
    val exitIp: String?,
    val timestamp: Long = System.currentTimeMillis()
)
