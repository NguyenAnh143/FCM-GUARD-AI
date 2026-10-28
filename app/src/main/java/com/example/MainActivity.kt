package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.core.HyperOsUtils
import com.example.service.FcmGuardService
import com.example.ui.FcmGuardApp

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification permission handled
    }

    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshSystemStatus()
    }

    private val exactAlarmLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshSystemStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as FcmGuardApplication
        val preferences = app.appPreferences

        // Request POST_NOTIFICATIONS on Android 13+ if not already granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check PowerManager.isIgnoringBatteryOptimizations() and launch runtime request if false
        checkAndRequestBatteryOptimization()

        // Auto start service if user enabled auto-guard
        if (preferences.isAutoGuardEnabled.value) {
            try {
                FcmGuardService.start(this)
            } catch (e: Exception) {
                // Safely handle service start exceptions
            }
        }

        // Initial permissions refresh
        refreshSystemStatus()

        setContent {
            FcmGuardApp(appPreferences = preferences)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh live status whenever returning from system settings
        refreshSystemStatus()
    }

    /**
     * Refreshes battery optimization and exact alarm permission state into AppPreferences
     */
    fun refreshSystemStatus() {
        val app = application as? FcmGuardApplication ?: return
        val isIgnored = HyperOsUtils.isIgnoringBatteryOptimizations(this)
        val canExactAlarm = HyperOsUtils.canScheduleExactAlarms(this)
        app.appPreferences.updateSystemPermissions(isIgnored, canExactAlarm)
    }

    /**
     * Runtime request to ignore battery optimization:
     * check PowerManager.isIgnoringBatteryOptimizations()
     * if false, launch Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
     */
    fun checkAndRequestBatteryOptimization(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as? PowerManager ?: return false
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            return try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                batteryOptimizationLauncher.launch(intent)
                true
            } catch (e: Exception) {
                HyperOsUtils.openBatteryOptimizationSettings(this)
                false
            }
        }
        return true
    }

    /**
     * Request exact alarm permission on Android 12+ (Build.VERSION_CODES.S)
     */
    fun checkAndRequestExactAlarm(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as? android.app.AlarmManager
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                return try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    exactAlarmLauncher.launch(intent)
                    true
                } catch (e: Exception) {
                    HyperOsUtils.openAppDetailsSettings(this, packageName)
                    false
                }
            }
        }
        return true
    }
}
