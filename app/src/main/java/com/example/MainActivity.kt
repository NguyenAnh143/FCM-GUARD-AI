package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.service.FcmGuardService
import com.example.ui.FcmGuardApp

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled gracefully
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

        // Auto start service if user enabled auto-guard
        if (preferences.isAutoGuardEnabled.value) {
            try {
                FcmGuardService.start(this)
            } catch (e: Exception) {
                // Safely handle service start exceptions
            }
        }

        setContent {
            FcmGuardApp(appPreferences = preferences)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh preferences or triggers on resume if needed
    }
}
