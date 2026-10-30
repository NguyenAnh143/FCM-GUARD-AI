package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.pref.AppPreferences
import com.example.service.FcmGuardService
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    com.example.core.GmsWhitelistManager.ensureGmsWhitelisted(context)
                    val prefs = AppPreferences(context)
                    if (prefs.isAutoGuardEnabled.value) {
                        FcmGuardService.start(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
