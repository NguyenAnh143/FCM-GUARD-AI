package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.model.HeartbeatLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeartbeatLogDao {
    @Insert
    suspend fun insertLog(log: HeartbeatLogEntity): Long

    @Query("SELECT * FROM heartbeat_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HeartbeatLogEntity>>

    @Query("SELECT * FROM heartbeat_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<HeartbeatLogEntity>>

    @Query("SELECT COUNT(*) FROM heartbeat_logs")
    suspend fun getCount(): Int

    @Query("DELETE FROM heartbeat_logs")
    suspend fun clearAll()
}
