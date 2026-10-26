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

    private val _backupIntervalMinutes = MutableStateFlow(prefs.getInt(KEY_BACKUP_INTERVAL, 30))
    val backupIntervalMinutes: StateFlow<Int> = _backupIntervalMinutes.asStateFlow()

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

    fun setAutoGuardEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_GUARD, enabled).apply()
        _isAutoGuardEnabled.value = enabled
    }

    fun setPersistentNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION, enabled).apply()
        _isPersistentNotificationEnabled.value = enabled
    }

    fun setBackupIntervalMinutes(minutes: Int) {
        val valid = if (minutes in listOf(10, 15, 30)) minutes else 30
        prefs.edit().putInt(KEY_BACKUP_INTERVAL, valid).apply()
        _backupIntervalMinutes.value = valid
    }

    fun setThemeMode(theme: String) {
        prefs.edit().putString(KEY_THEME_MODE, theme).apply()
        _themeMode.value = theme
    }

    fun setLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANGUAGE_CODE, code).apply()
        _languageCode.value = code
    }

    fun updateLastRepair(timestamp: Long, latencyMs: Long, status: String) {
        prefs.edit()
            .putLong(KEY_LAST_REPAIR_TIME, timestamp)
            .putLong(KEY_LAST_LATENCY_MS, latencyMs)
            .putString(KEY_LAST_SOCKET_STATUS, status)
            .apply()
        _lastRepairTimestamp.value = timestamp
        _lastLatencyMs.value = latencyMs
        _lastSocketStatus.value = status
    }

    companion object {
        private const val KEY_AUTO_GUARD = "key_auto_guard"
        private const val KEY_NOTIFICATION = "key_persistent_notification"
        private const val KEY_BACKUP_INTERVAL = "key_backup_interval"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_LANGUAGE_CODE = "key_language_code"
        private const val KEY_LAST_REPAIR_TIME = "key_last_repair_time"
        private const val KEY_LAST_LATENCY_MS = "key_last_latency_ms"
        private const val KEY_LAST_SOCKET_STATUS = "key_last_socket_status"
    }
}
