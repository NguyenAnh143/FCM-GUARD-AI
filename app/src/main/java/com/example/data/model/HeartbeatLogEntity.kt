package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "heartbeat_logs")
data class HeartbeatLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // REPAIR_NOW, NETWORK_RESTORED, SCREEN_ON, PERIODIC_TIMER, SOCKET_TEST
    val success: Boolean,
    val latencyMs: Long,
    val serverIp: String,
    val triggerReason: String,
    val details: String
)
