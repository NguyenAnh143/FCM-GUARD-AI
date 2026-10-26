package com.example.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FcmAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val autostartStatus: AutostartStatus,
    val isSystemApp: Boolean
)

object FcmAppScanner {

    suspend fun scanFcmApps(context: Context): List<FcmAppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val foundPackages = mutableSetOf<String>()

        // 1. Query services matching com.google.firebase.MESSAGING_EVENT
        try {
            val serviceIntent = Intent("com.google.firebase.MESSAGING_EVENT")
            val serviceInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentServices(serviceIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentServices(serviceIntent, 0)
            }
            for (info in serviceInfos) {
                info.serviceInfo?.packageName?.let { foundPackages.add(it) }
            }
        } catch (ignored: Exception) {
        }

        // 2. Query broadcast receivers matching com.google.android.c2dm.intent.RECEIVE
        try {
            val receiverIntent = Intent("com.google.android.c2dm.intent.RECEIVE")
            val receiverInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryBroadcastReceivers(receiverIntent, PackageManager.ResolveInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryBroadcastReceivers(receiverIntent, 0)
            }
            for (info in receiverInfos) {
                info.activityInfo?.packageName?.let { foundPackages.add(it) }
            }
        } catch (ignored: Exception) {
        }

        // 3. Include popular messaging & notification apps
        val popularPackages = listOf(
            "com.zing.zalo",
            "org.telegram.messenger",
            "com.whatsapp",
            "com.facebook.orca",
            "com.facebook.katana",
            "com.google.android.gm",
            "com.google.android.apps.messaging",
            "com.instagram.android",
            "com.shopee.vn",
            "com.lazada.android",
            "com.vng.zalopay",
            "com.mservice.momotransfer",
            "com.vcb.digibank",
            "com.mbmobile",
            "com.discord",
            "com.Slack"
        )
        for (pkg in popularPackages) {
            try {
                packageManager.getPackageInfo(pkg, 0)
                foundPackages.add(pkg)
            } catch (ignored: Exception) {
            }
        }

        // Build list of items
        val result = mutableListOf<FcmAppItem>()
        for (pkg in foundPackages) {
            if (pkg == context.packageName) continue
            try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (e: Exception) {
                    null
                }
                val autostart = HyperOsUtils.checkAutostart(context, pkg)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                result.add(
                    FcmAppItem(
                        packageName = pkg,
                        appName = label.ifEmpty { pkg },
                        icon = icon,
                        autostartStatus = autostart,
                        isSystemApp = isSystem
                    )
                )
            } catch (ignored: Exception) {
            }
        }

        result.sortedWith(
            compareBy<FcmAppItem> { it.isSystemApp }
                .thenBy {
                    when (it.autostartStatus) {
                        AutostartStatus.DISABLED -> 0
                        AutostartStatus.PARTIAL -> 1
                        AutostartStatus.UNKNOWN -> 2
                        AutostartStatus.ENABLED -> 3
                    }
                }
                .thenBy { it.appName.lowercase() }
        )
    }
}
