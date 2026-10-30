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
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isScreenOn = powerManager?.isInteractive ?: false

            if (isScreenOn) {
                Log.d(TAG, "Screen is ON (isInteractive=true). Cancelling alarm to save battery.")
                FcmAlarmScheduler.cancelAlarm(context)
                return
            }

            Log.d(TAG, "Screen is OFF. Waking up briefly for GMS whitelist check & socket keep-alive...")

            val wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FCMGuard::ScreenOffKeepAliveWakeLock"
            )

            wakeLock?.acquire(8000L) // 8s timeout to safely process

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = AppPreferences(context)
                    val db = AppDatabase.getDatabase(context)

                    // 1. Ensure GMS is whitelisted in MILLET_NO_RESTRICT_APP (cheap & critical)
                    val whitelistRes = com.example.core.GmsWhitelistManager.ensureGmsWhitelisted(context)
                    val currentStatus = com.example.core.GmsWhitelistManager.getStatus(context)
                    prefs.updateWhitelistState(currentStatus.isGmsWhitelisted, currentStatus.rawValue, whitelistRes)

                    // 2. Network socket heartbeat check if auto guard enabled
                    if (prefs.isAutoGuardEnabled.value) {
                        val result = GmsHeartbeatManager.triggerHeartbeat(
                            context,
                            "SCREEN_OFF_ALARM",
                            db.heartbeatLogDao()
                        )

                        prefs.updateLastRepair(
                            System.currentTimeMillis(),
                            result.latencyMs,
                            if (result.success) "OK" else "ERROR",
                            "SCREEN_OFF_ALARM"
                        )
                    }

                    // 3. ONLY reschedule next alarm if screen is STILL off
                    val stillScreenOff = !(powerManager?.isInteractive ?: false)
                    if (stillScreenOff && prefs.isAutoGuardEnabled.value) {
                        val interval = prefs.screenOffIntervalMinutes.value
                        FcmAlarmScheduler.scheduleNextAlarm(context, interval)
                        Log.d(TAG, "Rescheduled next screen-off alarm in $interval mins")
                    } else {
                        FcmAlarmScheduler.cancelAlarm(context)
                        Log.d(TAG, "Screen woke up during execution. Cancelled alarm.")
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
