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
import android.database.ContentObserver
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.core.FcmAlarmScheduler
import com.example.core.GmsHeartbeatManager
import com.example.core.GmsWhitelistManager
import com.example.core.WhitelistResult
import com.example.data.db.AppDatabase
import com.example.data.pref.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FcmGuardService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var appPreferences: AppPreferences
    private lateinit var appDatabase: AppDatabase

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var screenReceiver: BroadcastReceiver? = null

    // ContentObserver for HyperOS MILLET_NO_RESTRICT_APP
    private var whitelistObserver: ContentObserver? = null
    private var whitelistDebounceJob: Job? = null

    // Dual periodic timers
    private var screenOnHeartbeatJob: Job? = null
    private var screenOffHeartbeatJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(applicationContext)
        appDatabase = AppDatabase.getDatabase(applicationContext)
        createNotificationChannel()

        registerNetworkListener()
        registerScreenStateReceiver()
        registerWhitelistObserver()

        // Check & ensure GMS is in MILLET_NO_RESTRICT_APP on service creation
        serviceScope.launch(Dispatchers.IO) {
            val result = GmsWhitelistManager.ensureGmsWhitelisted(applicationContext)
            val status = GmsWhitelistManager.getStatus(applicationContext)
            appPreferences.updateWhitelistState(status.isGmsWhitelisted, status.rawValue, result)
        }

        // Start initial heartbeat timers based on current screen state
        val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        val isScreenCurrentlyOn = powerManager?.isInteractive ?: true
        appPreferences.updateScreenState(isScreenOn = isScreenCurrentlyOn, isDeviceSleeping = !isScreenCurrentlyOn)

        if (isScreenCurrentlyOn) {
            startScreenOnTimer()
        } else {
            startScreenOffTimer()
        }

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
                restartHeartbeatTimers()
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

        val offInterval = appPreferences.screenOffIntervalMinutes.value
        val onInterval = appPreferences.screenOnIntervalMinutes.value

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FCM Guard — HyperOS Guardian")
            .setContentText("$contentText (Tắt màn hình: ${offInterval}m • Sáng: ${onInterval}m)")
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
                                if (result.success) "OK" else "ERROR",
                                "NETWORK_RESTORED"
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
                            "DISCONNECTED",
                            "NETWORK_LOST"
                        )
                    }
                }
            }

            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Network callback registration failed: ${e.message}")
        }
    }

    /**
     * BroadcastReceiver for:
     * - Intent.ACTION_SCREEN_ON
     * - Intent.ACTION_SCREEN_OFF
     * - Intent.ACTION_USER_PRESENT
     */
    private fun registerScreenStateReceiver() {
        try {
            screenReceiver = object : BroadcastReceiver() {
                private var lastScreenOnTrigger = 0L

                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_SCREEN_ON -> {
                            val now = System.currentTimeMillis()
                            appPreferences.updateScreenState(isScreenOn = true, isDeviceSleeping = false)
                            Log.d(TAG, "ACTION_SCREEN_ON detected: isScreenOn=true -> Cancelling alarm to save battery")

                            // Cancel any pending alarm immediately to prevent wakeups while screen is on
                            FcmAlarmScheduler.cancelAlarm(applicationContext)
                            startScreenOnTimer()

                            // Check whitelist in background
                            serviceScope.launch(Dispatchers.IO) {
                                val result = GmsWhitelistManager.ensureGmsWhitelisted(applicationContext)
                                val status = GmsWhitelistManager.getStatus(applicationContext)
                                appPreferences.updateWhitelistState(status.isGmsWhitelisted, status.rawValue, result)
                            }

                            // Throttle screen-on network ping (minimum 2 minutes apart to prevent spamming)
                            if (now - lastScreenOnTrigger > 2 * 60 * 1000L) {
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
                                        if (result.success) "OK" else "ERROR",
                                        "SCREEN_ON"
                                    )
                                }
                            }
                        }

                        Intent.ACTION_SCREEN_OFF -> {
                            appPreferences.updateScreenState(isScreenOn = false, isDeviceSleeping = true)
                            Log.d(TAG, "ACTION_SCREEN_OFF detected: device entering sleep mode")

                            // Switch to screen-off timer & prime exact alarm keep-alive
                            startScreenOffTimer()
                        }

                        Intent.ACTION_USER_PRESENT -> {
                            appPreferences.updateScreenState(isScreenOn = true, isDeviceSleeping = false)
                            Log.d(TAG, "ACTION_USER_PRESENT detected: User unlocked phone! Cancelling alarm and ensuring GMS whitelist")

                            // Cancel pending screen-off alarm
                            FcmAlarmScheduler.cancelAlarm(applicationContext)
                            startScreenOnTimer()

                            // Ensure GMS whitelist immediately when unlocked
                            serviceScope.launch(Dispatchers.IO) {
                                val result = GmsWhitelistManager.ensureGmsWhitelisted(applicationContext)
                                val status = GmsWhitelistManager.getStatus(applicationContext)
                                appPreferences.updateWhitelistState(status.isGmsWhitelisted, status.rawValue, result)
                            }

                            // Trigger network socket heartbeat
                            serviceScope.launch {
                                val result = GmsHeartbeatManager.triggerHeartbeat(
                                    applicationContext,
                                    "USER_PRESENT",
                                    appDatabase.heartbeatLogDao()
                                )
                                appPreferences.updateLastRepair(
                                    System.currentTimeMillis(),
                                    result.latencyMs,
                                    if (result.success) "OK" else "ERROR",
                                    "USER_PRESENT"
                                )
                            }
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Screen state receiver registration failed: ${e.message}")
        }
    }

    private fun registerWhitelistObserver() {
        try {
            val uri = Settings.System.getUriFor(GmsWhitelistManager.KEY_MILLET_NO_RESTRICT_APP)
            if (uri != null) {
                whitelistObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)
                        Log.d(TAG, "ContentObserver: MILLET_NO_RESTRICT_APP modified in system")
                        whitelistDebounceJob?.cancel()
                        whitelistDebounceJob = serviceScope.launch(Dispatchers.IO) {
                            delay(1000L) // 1s debounce to avoid recursive loops
                            val result = GmsWhitelistManager.ensureGmsWhitelisted(applicationContext)
                            val status = GmsWhitelistManager.getStatus(applicationContext)
                            appPreferences.updateWhitelistState(status.isGmsWhitelisted, status.rawValue, result)
                            Log.d(TAG, "Debounced GmsWhitelistManager result: $result, isWhitelisted=${status.isGmsWhitelisted}")
                        }
                    }
                }
                contentResolver.registerContentObserver(uri, false, whitelistObserver!!)
                Log.d(TAG, "Registered ContentObserver for $uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register ContentObserver for MILLET_NO_RESTRICT_APP: ${e.message}")
        }
    }

    /**
     * Periodic Timer when screen is ON
     * Uses longer interval (e.g. 30 mins) to save resources
     */
    private fun startScreenOnTimer() {
        screenOffHeartbeatJob?.cancel()
        screenOnHeartbeatJob?.cancel()

        screenOnHeartbeatJob = serviceScope.launch {
            while (isActive) {
                val intervalMins = appPreferences.screenOnIntervalMinutes.value
                val delayMs = intervalMins * 60 * 1000L
                delay(delayMs)

                if (appPreferences.isAutoGuardEnabled.value && appPreferences.isScreenOn.value) {
                    val result = GmsHeartbeatManager.triggerHeartbeat(
                        applicationContext,
                        "PERIODIC_SCREEN_ON_${intervalMins}M",
                        appDatabase.heartbeatLogDao()
                    )
                    appPreferences.updateLastRepair(
                        System.currentTimeMillis(),
                        result.latencyMs,
                        if (result.success) "OK" else "ERROR",
                        "PERIODIC_SCREEN_ON_${intervalMins}M"
                    )
                }
            }
        }
    }

    /**
     * Periodic Timer when screen is OFF (Device in standby / Deep Doze)
     * Uses shorter interval (e.g. 10 or 15 mins) and primes AlarmManager with WakeLock
     */
    private fun startScreenOffTimer() {
        screenOnHeartbeatJob?.cancel()
        screenOffHeartbeatJob?.cancel()

        val intervalMins = appPreferences.screenOffIntervalMinutes.value
        // Prime exact alarm to wake CPU even when Android Deep Doze freezes coroutine delay
        com.example.core.FcmAlarmScheduler.scheduleNextAlarm(applicationContext, intervalMins)

        screenOffHeartbeatJob = serviceScope.launch {
            while (isActive) {
                val delayMs = appPreferences.screenOffIntervalMinutes.value * 60 * 1000L
                delay(delayMs)

                if (appPreferences.isAutoGuardEnabled.value && !appPreferences.isScreenOn.value) {
                    // Check whitelist during screen-off period
                    withContext(Dispatchers.IO) {
                        GmsWhitelistManager.ensureGmsWhitelisted(applicationContext)
                    }

                    val result = GmsHeartbeatManager.triggerHeartbeat(
                        applicationContext,
                        "PERIODIC_SCREEN_OFF_${appPreferences.screenOffIntervalMinutes.value}M",
                        appDatabase.heartbeatLogDao()
                    )
                    appPreferences.updateLastRepair(
                        System.currentTimeMillis(),
                        result.latencyMs,
                        if (result.success) "OK" else "ERROR",
                        "PERIODIC_SCREEN_OFF_${appPreferences.screenOffIntervalMinutes.value}M"
                    )
                    // Re-prime alarm to ensure continuity during prolonged sleep
                    com.example.core.FcmAlarmScheduler.scheduleNextAlarm(
                        applicationContext,
                        appPreferences.screenOffIntervalMinutes.value
                    )
                }
            }
        }
    }

    private fun restartHeartbeatTimers() {
        if (appPreferences.isScreenOn.value) {
            startScreenOnTimer()
        } else {
            startScreenOffTimer()
        }
    }

    private fun triggerManualRepair() {
        serviceScope.launch {
            // 1. Maintain GMS in MILLET_NO_RESTRICT_APP
            val whitelistResult = withContext(Dispatchers.IO) {
                GmsWhitelistManager.ensureGmsWhitelisted(applicationContext)
            }
            val status = GmsWhitelistManager.getStatus(applicationContext)
            appPreferences.updateWhitelistState(status.isGmsWhitelisted, status.rawValue, whitelistResult)

            // 2. Perform network socket check
            val netResult = GmsHeartbeatManager.triggerHeartbeat(
                applicationContext,
                "MANUAL_REPAIR",
                appDatabase.heartbeatLogDao()
            )
            appPreferences.updateLastRepair(
                System.currentTimeMillis(),
                netResult.latencyMs,
                if (netResult.success) "OK" else "ERROR",
                "MANUAL_REPAIR"
            )

            val msg = when (whitelistResult) {
                WhitelistResult.ADDED -> "Đã thêm GMS vào Whitelist (${netResult.latencyMs}ms)"
                WhitelistResult.ALREADY_PRESENT -> "GMS có trong Whitelist (${netResult.latencyMs}ms)"
                WhitelistResult.NO_PERMISSION -> "Thiếu quyền Sửa đổi cài đặt hệ thống"
                WhitelistResult.WRITE_FAILED -> "Ghi Whitelist thất bại (${netResult.latencyMs}ms)"
                WhitelistResult.KEY_UNSUPPORTED -> "Key hệ thống không được hỗ trợ"
            }
            updateNotification(msg)
        }
    }

    private fun stopForegroundService() {
        isRunning = false
        screenOnHeartbeatJob?.cancel()
        screenOffHeartbeatJob?.cancel()
        whitelistDebounceJob?.cancel()
        com.example.core.FcmAlarmScheduler.cancelAlarm(applicationContext)
        try {
            whitelistObserver?.let { contentResolver.unregisterContentObserver(it) }
        } catch (ignored: Exception) {}
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
