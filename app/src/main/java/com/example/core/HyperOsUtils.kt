package com.example.core

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log

enum class AutostartStatus {
    ENABLED,
    PARTIAL,
    DISABLED,
    UNKNOWN
}

object HyperOsUtils {
    private const val TAG = "HyperOsUtils"
    // Xiaomi MIUI / HyperOS AppOps constant for autostart
    private const val OP_AUTO_START = 10008

    fun checkAutostart(context: Context, packageName: String): AutostartStatus {
        try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                ?: return AutostartStatus.UNKNOWN
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val uid = applicationInfo.uid

            // Method 1: checkOpNoThrow using reflection with OP_AUTO_START int
            try {
                val checkOpMethod = AppOpsManager::class.java.getMethod(
                    "checkOpNoThrow",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                val mode = checkOpMethod.invoke(appOpsManager, OP_AUTO_START, uid, packageName) as? Int
                if (mode != null) {
                    return when (mode) {
                        AppOpsManager.MODE_ALLOWED -> AutostartStatus.ENABLED
                        AppOpsManager.MODE_IGNORED -> AutostartStatus.DISABLED
                        AppOpsManager.MODE_ERRORED -> AutostartStatus.DISABLED
                        else -> AutostartStatus.PARTIAL
                    }
                }
            } catch (ignored: NoSuchMethodException) {
            } catch (e: Exception) {
                Log.d(TAG, "checkOpNoThrow(int) reflection failed: ${e.message}")
            }

            // Method 2: checkOpNoThrow with String op name "android:auto_start" or "op_auto_start"
            try {
                val checkOpStringMethod = AppOpsManager::class.java.getMethod(
                    "checkOpNoThrow",
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                for (opName in listOf("android:auto_start", "auto_start", "OP_AUTO_START")) {
                    try {
                        val mode = checkOpStringMethod.invoke(appOpsManager, opName, uid, packageName) as? Int
                        if (mode != null) {
                            return when (mode) {
                                AppOpsManager.MODE_ALLOWED -> AutostartStatus.ENABLED
                                AppOpsManager.MODE_IGNORED -> AutostartStatus.DISABLED
                                AppOpsManager.MODE_ERRORED -> AutostartStatus.DISABLED
                                else -> AutostartStatus.PARTIAL
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                }
            } catch (ignored: Exception) {
            }

        } catch (e: Exception) {
            Log.d(TAG, "checkAutostart failed for $packageName: ${e.message}")
        }
        return AutostartStatus.UNKNOWN
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun canWriteSystemSettings(context: Context): Boolean {
        return try {
            Settings.System.canWrite(context)
        } catch (e: Exception) {
            false
        }
    }

    fun openHyperOsAutostartSettings(context: Context): Boolean {
        val intents = listOf(
            // HyperOS / MIUI AutoStart Management
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securityscan.MainActivity")),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (ignored: Exception) {
            }
        }
        return false
    }

    fun openBatteryOptimizationSettings(context: Context): Boolean {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                return true
            } catch (ignored: Exception) {
                return false
            }
        }
    }

    fun openWriteSettingsPermission(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openGcmDiagnostics(context: Context): Boolean {
        // Try opening Play Services GcmDiagnostics activity
        val intents = listOf(
            Intent().setComponent(ComponentName("com.google.android.gms", "com.google.android.gms.gcm.GcmDiagnostics")),
            Intent("com.google.android.gms.gcm.GcmDiagnostics"),
            // Fallback: Open dialer with *#*#426#*#* (*#*#GCM#*#*)
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:*%23*%23426%23*%23*")
            },
            // Fallback: Open Google Play Services App Info
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:com.google.android.gms")
            }
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (ignored: Exception) {
            }
        }
        return false
    }

    fun openAppDetailsSettings(context: Context, packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun openBatteryAndPerformanceAppInfo(context: Context): Boolean {
        val targetPackages = listOf(
            "com.miui.powerkeeper", // Battery and Performance (Pin & hiệu suất)
            "com.miui.securitycenter"
        )
        for (pkg in targetPackages) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$pkg")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (ignored: Exception) {
            }
        }
        return false
    }
}
