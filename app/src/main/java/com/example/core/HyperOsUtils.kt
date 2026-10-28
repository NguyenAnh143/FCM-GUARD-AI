package com.example.core

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            val uid = packageInfo.applicationInfo?.uid ?: return AutostartStatus.UNKNOWN

            val method = appOpsManager.javaClass.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val mode = method.invoke(appOpsManager, OP_AUTO_START, uid, packageName) as? Int
                ?: return AutostartStatus.UNKNOWN

            return when (mode) {
                AppOpsManager.MODE_ALLOWED -> AutostartStatus.ENABLED
                AppOpsManager.MODE_IGNORED -> AutostartStatus.DISABLED
                AppOpsManager.MODE_ERRORED -> AutostartStatus.DISABLED
                2 -> AutostartStatus.PARTIAL
                else -> AutostartStatus.UNKNOWN
            }
        } catch (e: Exception) {
            Log.d(TAG, "checkAutostart failed for $packageName: ${e.message}")
            return AutostartStatus.UNKNOWN
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun canWriteSystemSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun openHyperOsAutostartSettings(context: Context): Boolean {
        val intents = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.securityscan.MainActivity"
                )
            ),
            Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT)
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
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (ex: Exception) {
                return openAppDetailsSettings(context, context.packageName)
            }
        }
    }

    /**
     * Opens Xiaomi / HyperOS Battery Saver settings directly to set 'No Restrictions' (Không hạn chế)
     */
    fun openHyperOsBatteryNoRestrictions(context: Context, targetPackage: String = context.packageName): Boolean {
        val intents = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
            ).putExtra("package_name", targetPackage),
            Intent().setComponent(
                ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.PowerKeeperSettingsActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.powercenter.PowerCenter"
                )
            )
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (ignored: Exception) {}
        }
        return openAppDetailsSettings(context, targetPackage)
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
        val intents = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.google.android.gms",
                    "com.google.android.gms.gcm.GcmDiagnostics"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.google.android.gms",
                    "com.google.android.gms.cloudmessaging.GcmDiagnostics"
                )
            ),
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tel:*#*#426#*#*")
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

    /**
     * Checks if Facebook Messenger is installed on device
     */
    fun isMessengerInstalled(context: Context): Boolean {
        val messengerPackages = listOf(
            "com.facebook.orca",
            "com.facebook.mlite",
            "com.facebook.katana"
        )
        val pm = context.packageManager
        for (pkg in messengerPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return true
            } catch (ignored: Exception) {}
        }
        return false
    }

    /**
     * Opens Facebook Messenger's App Info directly so the user can set:
     * 1. Autostart = ON
     * 2. Battery = No restrictions
     * 3. Notifications = Allow on lock screen & floating banners
     */
    fun openMessengerSettings(context: Context): Boolean {
        val messengerPackages = listOf(
            "com.facebook.orca",
            "com.facebook.mlite",
            "com.facebook.katana"
        )
        val pm = context.packageManager
        for (pkg in messengerPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return openAppDetailsSettings(context, pkg)
            } catch (ignored: Exception) {}
        }
        return false
    }

    /**
     * Opens HyperOS / MIUI Battery & Performance Settings (Power Center)
     * where "Tắt dữ liệu di động khi thiết bị bị khóa" (Turn off mobile data when device is locked)
     * and "Xóa bộ nhớ đệm khi khóa thiết bị" are configured.
     */
    fun openHyperOsBatterySettings(context: Context): Boolean {
        val intents = listOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.powercenter.PowerCenter"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.PowerKeeperSettingsActivity"
                )
            ),
            Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (ignored: Exception) {}
        }
        return false
    }
}
