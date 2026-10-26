package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.GmsHeartbeatManager
import com.example.data.db.AppDatabase
import com.example.data.pref.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FcmGuardService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var appPreferences: AppPreferences
    private lateinit var appDatabase: AppDatabase

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var screenReceiver: BroadcastReceiver? = null
    private var periodicTimerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(applicationContext)
        appDatabase = AppDatabase.getDatabase(applicationContext)
        createNotificationChannel()

        registerNetworkListener()
        registerScreenReceiver()
        startPeriodicTimer()

        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_REFRESH_NOW -> {
                triggerManualRepair()
            }
            ACTION_UPDATE_CONFIG -> {
                updateNotification()
                restartPeriodicTimer()
            }
            ACTION_START -> {
                startAsForeground()
            }
        }

        return START_STICKY
    }

    private fun startAsForeground() {
        val notification = buildNotification("Bảo vệ thông báo đẩy đang hoạt động")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
                startForeground(NOTIFICATION_ID, notification, fgsType)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val repairIntent = Intent(this, FcmGuardService::class.java).apply {
            action = ACTION_REFRESH_NOW
        }
        val pendingRepair = PendingIntent.getService(
            this,
            1,
            repairIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val interval = appPreferences.backupIntervalMinutes.value

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FCM Guard — HyperOS Guardian")
            .setContentText("$contentText (Dự phòng: ${interval}m)")
            .setSmallIcon(R.drawable.ic_notification_guard)
            .setContentIntent(pendingOpenApp)
            .addAction(0, "Sửa lỗi ngay", pendingRepair)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(message: String = "Bảo vệ thông báo đẩy đang hoạt động") {
        if (!appPreferences.isPersistentNotificationEnabled.value) {
            return
        }
        val notification = buildNotification(message)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun registerNetworkListener() {
        try {
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                private var wasDisconnected = false

                override fun onAvailable(network: Network) {
                    if (wasDisconnected) {
                        wasDisconnected = false
                        serviceScope.launch {
                            delay(1500) // Brief grace period for network route settling
                            val result = GmsHeartbeatManager.triggerHeartbeat(
                                applicationContext,
                                "NETWORK_RESTORED",
                                appDatabase.heartbeatLogDao()
                            )
                            appPreferences.updateLastRepair(
                                System.currentTimeMillis(),
                                result.latencyMs,
                                if (result.success) "OK" else "ERROR"
                            )
                            updateNotification("Mạng phục hồi — Đã làm mới socket MCS")
                        }
                    }
                }

                override fun onLost(network: Network) {
                    wasDisconnected = true
                    serviceScope.launch {
                        appPreferences.updateLastRepair(
                            System.currentTimeMillis(),
                            -1L,
                            "DISCONNECTED"
                        )
                    }
                }
            }

            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Network callback registration failed: ${e.message}")
        }
    }

    private fun registerScreenReceiver() {
        try {
            screenReceiver = object : BroadcastReceiver() {
                private var lastScreenOnTrigger = 0L

                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_SCREEN_ON) {
                        val now = System.currentTimeMillis()
                        // Throttle screen on heartbeats to avoid spamming (minimum 3 minutes apart)
                        if (now - lastScreenOnTrigger > 3 * 60 * 1000L) {
                            lastScreenOnTrigger = now
                            serviceScope.launch {
                                val result = GmsHeartbeatManager.triggerHeartbeat(
                                    applicationContext,
                                    "SCREEN_ON",
                                    appDatabase.heartbeatLogDao()
                                )
                                appPreferences.updateLastRepair(
                                    System.currentTimeMillis(),
                                    result.latencyMs,
                                    if (result.success) "OK" else "ERROR"
                                )
                            }
                        }
                    }
                }
            }
            val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
            registerReceiver(screenReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Screen receiver registration failed: ${e.message}")
        }
    }

    private fun startPeriodicTimer() {
        periodicTimerJob?.cancel()
        periodicTimerJob = serviceScope.launch {
            while (isActive) {
                val minutes = appPreferences.backupIntervalMinutes.value
                val delayMs = minutes * 60 * 1000L
                delay(delayMs)

                if (appPreferences.isAutoGuardEnabled.value) {
                    val result = GmsHeartbeatManager.triggerHeartbeat(
                        applicationContext,
                        "PERIODIC_TIMER_${minutes}M",
                        appDatabase.heartbeatLogDao()
                    )
                    appPreferences.updateLastRepair(
                        System.currentTimeMillis(),
                        result.latencyMs,
                        if (result.success) "OK" else "ERROR"
                    )
                }
            }
        }
    }

    private fun restartPeriodicTimer() {
        startPeriodicTimer()
    }

    private fun triggerManualRepair() {
        serviceScope.launch {
            val result = GmsHeartbeatManager.triggerHeartbeat(
                applicationContext,
                "REPAIR_NOW",
                appDatabase.heartbeatLogDao()
            )
            appPreferences.updateLastRepair(
                System.currentTimeMillis(),
                result.latencyMs,
                if (result.success) "OK" else "ERROR"
            )
            updateNotification(
                if (result.success) "Đã gửi MCS Heartbeat (${result.latencyMs}ms)"
                else "Lỗi kết nối socket push"
            )
        }
    }

    private fun stopForegroundService() {
        isRunning = false
        periodicTimerJob?.cancel()
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (ignored: Exception) {}
        try {
            screenReceiver?.let { unregisterReceiver(it) }
        } catch (ignored: Exception) {}

        serviceJob.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopForegroundService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "FcmGuardService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "fcm_guard_channel"

        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_REFRESH_NOW = "com.example.service.ACTION_REFRESH_NOW"
        const val ACTION_UPDATE_CONFIG = "com.example.service.ACTION_UPDATE_CONFIG"

        var isRunning: Boolean = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, FcmGuardService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FcmGuardService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun refreshNow(context: Context) {
            val intent = Intent(context, FcmGuardService::class.java).apply {
                action = ACTION_REFRESH_NOW
            }
            context.startService(intent)
        }

        fun updateConfig(context: Context) {
            val intent = Intent(context, FcmGuardService::class.java).apply {
                action = ACTION_UPDATE_CONFIG
            }
            context.startService(intent)
        }
    }
}
