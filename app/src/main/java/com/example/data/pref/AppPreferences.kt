package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fcm_guard_prefs", Context.MODE_PRIVATE)

    private val _isAutoGuardEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_GUARD, true))
    val isAutoGuardEnabled: StateFlow<Boolean> = _isAutoGuardEnabled.asStateFlow()

    private val _isPersistentNotificationEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATION, true))
    val isPersistentNotificationEnabled: StateFlow<Boolean> = _isPersistentNotificationEnabled.asStateFlow()

    // Screen-off heartbeat interval (default: 10 minutes)
    private val _screenOffIntervalMinutes = MutableStateFlow(prefs.getInt(KEY_SCREEN_OFF_INTERVAL, 10))
    val screenOffIntervalMinutes: StateFlow<Int> = _screenOffIntervalMinutes.asStateFlow()

    // Screen-on heartbeat interval (default: 30 minutes)
    private val _screenOnIntervalMinutes = MutableStateFlow(prefs.getInt(KEY_SCREEN_ON_INTERVAL, 30))
    val screenOnIntervalMinutes: StateFlow<Int> = _screenOnIntervalMinutes.asStateFlow()

    // Backward-compatibility alias
    val backupIntervalMinutes: StateFlow<Int> = _screenOffIntervalMinutes

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _languageCode = MutableStateFlow(prefs.getString(KEY_LANGUAGE_CODE, "AUTO") ?: "AUTO")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    private val _lastRepairTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_REPAIR_TIME, 0L))
    val lastRepairTimestamp: StateFlow<Long> = _lastRepairTimestamp.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow(prefs.getLong(KEY_LAST_LATENCY_MS, -1L))
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs.asStateFlow()

    private val _lastSocketStatus = MutableStateFlow(prefs.getString(KEY_LAST_SOCKET_STATUS, "STANDBY") ?: "STANDBY")
    val lastSocketStatus: StateFlow<String> = _lastSocketStatus.asStateFlow()

    private val _lastTriggerReason = MutableStateFlow(prefs.getString(KEY_LAST_TRIGGER_REASON, "INITIAL") ?: "INITIAL")
    val lastTriggerReason: StateFlow<String> = _lastTriggerReason.asStateFlow()

    // Live runtime screen and device sleeping states
    private val _isScreenOn = MutableStateFlow(true)
    val isScreenOn: StateFlow<Boolean> = _isScreenOn.asStateFlow()

    private val _isDeviceSleeping = MutableStateFlow(false)
    val isDeviceSleeping: StateFlow<Boolean> = _isDeviceSleeping.asStateFlow()

    // Live system permissions
    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _canScheduleExactAlarms = MutableStateFlow(true)
    val canScheduleExactAlarms: StateFlow<Boolean> = _canScheduleExactAlarms.asStateFlow()

    fun updateSystemPermissions(isBatteryIgnored: Boolean, canExactAlarm: Boolean) {
        _isBatteryOptimizationIgnored.value = isBatteryIgnored
        _canScheduleExactAlarms.value = canExactAlarm
    }

    fun setAutoGuardEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_GUARD, enabled).apply()
        _isAutoGuardEnabled.value = enabled
    }

    fun setPersistentNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION, enabled).apply()
        _isPersistentNotificationEnabled.value = enabled
    }

    fun setScreenOffIntervalMinutes(minutes: Int) {
        val valid = if (minutes in listOf(5, 10, 15, 20)) minutes else 10
        prefs.edit().putInt(KEY_SCREEN_OFF_INTERVAL, valid).apply()
        _screenOffIntervalMinutes.value = valid
    }

    fun setScreenOnIntervalMinutes(minutes: Int) {
        val valid = if (minutes in listOf(15, 30, 60)) minutes else 30
        prefs.edit().putInt(KEY_SCREEN_ON_INTERVAL, valid).apply()
        _screenOnIntervalMinutes.value = valid
    }

    fun setBackupIntervalMinutes(minutes: Int) {
        setScreenOffIntervalMinutes(minutes)
    }

    fun setThemeMode(theme: String) {
        prefs.edit().putString(KEY_THEME_MODE, theme).apply()
        _themeMode.value = theme
    }

    fun setLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE_CODE, code).apply()
        _languageCode.value = code
    }

    fun updateScreenState(isScreenOn: Boolean, isDeviceSleeping: Boolean) {
        _isScreenOn.value = isScreenOn
        _isDeviceSleeping.value = isDeviceSleeping
    }

    fun updateLastRepair(timestamp: Long, latencyMs: Long, status: String, triggerReason: String = "") {
        val editor = prefs.edit()
            .putLong(KEY_LAST_REPAIR_TIME, timestamp)
            .putLong(KEY_LAST_LATENCY_MS, latencyMs)
            .putString(KEY_LAST_SOCKET_STATUS, status)

        if (triggerReason.isNotEmpty()) {
            editor.putString(KEY_LAST_TRIGGER_REASON, triggerReason)
            _lastTriggerReason.value = triggerReason
        }
        editor.apply()

        _lastRepairTimestamp.value = timestamp
        _lastLatencyMs.value = latencyMs
        _lastSocketStatus.value = status
    }

    companion object {
        private const val KEY_AUTO_GUARD = "key_auto_guard"
        private const val KEY_NOTIFICATION = "key_persistent_notification"
        private const val KEY_SCREEN_OFF_INTERVAL = "key_screen_off_interval"
        private const val KEY_SCREEN_ON_INTERVAL = "key_screen_on_interval"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_LANGUAGE_CODE = "key_language_code"
        private const val KEY_LAST_REPAIR_TIME = "key_last_repair_time"
        private const val KEY_LAST_LATENCY_MS = "key_last_latency_ms"
        private const val KEY_LAST_SOCKET_STATUS = "key_last_socket_status"
        private const val KEY_LAST_TRIGGER_REASON = "key_last_trigger_reason"
    }
}
