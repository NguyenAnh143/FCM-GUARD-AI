package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.core.FcmAlarmScheduler
import com.example.core.GmsHeartbeatManager
import com.example.data.db.AppDatabase
import com.example.data.pref.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action ?: return

        if (action == ACTION_ALARM_HEARTBEAT) {
            Log.d(TAG, "Screen-off Alarm received. Waking up GMS socket...")

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FCMGuard::ScreenOffKeepAliveWakeLock"
            )

            wakeLock?.acquire(8000L) // 8s timeout to safely process heartbeat

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = AppPreferences(context)
                    val db = AppDatabase.getDatabase(context)

                    if (prefs.isAutoGuardEnabled.value) {
                        val result = GmsHeartbeatManager.triggerHeartbeat(
                            context,
                            "SCREEN_OFF_ALARM",
                            db.heartbeatLogDao()
                        )

                        prefs.updateLastRepair(
                            System.currentTimeMillis(),
                            result.latencyMs,
                            if (result.success) "OK" else "ERROR"
                        )

                        // Reschedule next alarm for perpetual keep-alive
                        val interval = prefs.backupIntervalMinutes.value
                        FcmAlarmScheduler.scheduleNextAlarm(context, interval)
                        Log.d(TAG, "Alarm heartbeat finished: latency=${result.latencyMs}ms, status=${result.success}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in AlarmReceiver: ${e.message}")
                } finally {
                    try {
                        if (wakeLock?.isHeld == true) {
                            wakeLock.release()
                        }
                    } catch (ignored: Exception) {}
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_ALARM_HEARTBEAT = "com.example.receiver.ACTION_ALARM_HEARTBEAT"
    }
}
