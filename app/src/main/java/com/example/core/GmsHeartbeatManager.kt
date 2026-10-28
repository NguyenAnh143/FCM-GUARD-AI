package com.example.core

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.dao.HeartbeatLogDao
import com.example.data.model.HeartbeatLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class HeartbeatResult(
    val success: Boolean,
    val latencyMs: Long,
    val dnsLatencyMs: Long,
    val tcpLatencyMs: Long,
    val serverIp: String,
    val triggerReason: String,
    val message: String
)

object GmsHeartbeatManager {
    private const val TAG = "GmsHeartbeatManager"
    const val GOOGLE_MTALK_HOST = "mtalk.google.com"
    const val GOOGLE_MTALK_PORT = 5228

    suspend fun triggerHeartbeat(
        context: Context,
        reason: String,
        logDao: HeartbeatLogDao? = null
    ): HeartbeatResult = withContext(Dispatchers.IO) {
        // 1. Send MCS Heartbeat broadcast to Google Play Services with Foreground Priority
        try {
            val mcsIntent = Intent("com.google.android.intent.action.MCS_HEARTBEAT").apply {
                setPackage("com.google.android.gms")
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(mcsIntent)

            val gtalkIntent = Intent("com.google.android.intent.action.GTALK_HEARTBEAT").apply {
                setPackage("com.google.android.gms")
                addFlags(Intent.FLAG_RECEIVER_FOREGROUND or Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(gtalkIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Broadcast send failed: ${e.message}")
        }

        // 2. Test actual TCP connection & latency to mtalk.google.com:5228 with retry (max 2 retries)
        var testResult = testSocketConnection(reason)
        var retryCount = 0
        while (!testResult.success && retryCount < 2) {
            retryCount++
            Log.d(TAG, "Socket attempt failed, retrying ($retryCount/2) for trigger: $reason...")
            kotlinx.coroutines.delay(400L * retryCount)
            testResult = testSocketConnection("$reason (retry $retryCount)")
        }

        // 3. Persist to history log
        if (logDao != null) {
            try {
                logDao.insertLog(
                    HeartbeatLogEntity(
                        eventType = reason,
                        success = testResult.success,
                        latencyMs = testResult.latencyMs,
                        serverIp = testResult.serverIp,
                        triggerReason = reason,
                        details = if (retryCount > 0) "${testResult.message} [Đã thử lại $retryCount lần]" else testResult.message
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record log: ${e.message}")
            }
        }

        testResult
    }

    suspend fun testSocketConnection(triggerReason: String = "DIAGNOSTICS"): HeartbeatResult = withContext(Dispatchers.IO) {
        var dnsStart = 0L
        var dnsLatency = 0L
        var tcpLatency = 0L
        var resolvedIp = ""
        var isSuccess = false
        var message = ""

        try {
            dnsStart = System.currentTimeMillis()
            val inetAddress = InetAddress.getByName(GOOGLE_MTALK_HOST)
            dnsLatency = System.currentTimeMillis() - dnsStart
            resolvedIp = inetAddress.hostAddress ?: ""

            val tcpStart = System.currentTimeMillis()
            val socket = Socket()
            val socketAddress = InetSocketAddress(inetAddress, GOOGLE_MTALK_PORT)
            // 5000ms timeout
            socket.connect(socketAddress, 5000)
            tcpLatency = System.currentTimeMillis() - tcpStart
            socket.close()

            isSuccess = true
            message = "Socket connected successfully to $GOOGLE_MTALK_HOST:5228 ($resolvedIp)"
        } catch (e: Exception) {
            val totalElapsed = if (dnsStart > 0) System.currentTimeMillis() - dnsStart else -1L
            isSuccess = false
            message = "Connection error: ${e.localizedMessage ?: e.javaClass.simpleName}"
            return@withContext HeartbeatResult(
                success = false,
                latencyMs = totalElapsed,
                dnsLatencyMs = dnsLatency,
                tcpLatencyMs = tcpLatency,
                serverIp = resolvedIp.ifEmpty { "Unresolved" },
                triggerReason = triggerReason,
                message = message
            )
        }

        val totalLatency = dnsLatency + tcpLatency
        HeartbeatResult(
            success = isSuccess,
            latencyMs = totalLatency,
            dnsLatencyMs = dnsLatency,
            tcpLatencyMs = tcpLatency,
            serverIp = resolvedIp,
            triggerReason = triggerReason,
            message = message
        )
    }
}
