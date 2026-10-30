package com.example.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.model.HeartbeatLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class WhitelistResult {
    ALREADY_PRESENT,
    ADDED,
    NO_PERMISSION,
    WRITE_FAILED,
    KEY_UNSUPPORTED
}

data class WhitelistStatus(
    val hasWritePermission: Boolean,
    val isGmsWhitelisted: Boolean,
    val rawValue: String?,
    val packages: List<String>,
    val lastResult: WhitelistResult? = null
)

object GmsWhitelistManager {
    private const val TAG = "GmsWhitelistManager"
    const val KEY_MILLET_NO_RESTRICT_APP = "MILLET_NO_RESTRICT_APP"
    const val TARGET_PACKAGE = "com.google.android.gms"

    fun hasWritePermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun openWriteSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open ACTION_MANAGE_WRITE_SETTINGS: ${e.message}")
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun getStatus(context: Context): WhitelistStatus {
        val hasPermission = hasWritePermission(context)
        val rawValue = try {
            Settings.System.getString(context.contentResolver, KEY_MILLET_NO_RESTRICT_APP)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading $KEY_MILLET_NO_RESTRICT_APP: ${e.message}")
            null
        }

        val packages = parsePackages(rawValue)
        val isWhitelisted = packages.contains(TARGET_PACKAGE)

        return WhitelistStatus(
            hasWritePermission = hasPermission,
            isGmsWhitelisted = isWhitelisted,
            rawValue = rawValue,
            packages = packages
        )
    }

    fun parsePackages(rawValue: String?): List<String> {
        if (rawValue.isNullOrBlank()) return emptyList()
        return rawValue.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Ensures com.google.android.gms is present in Settings.System.MILLET_NO_RESTRICT_APP.
     * Preserves all existing packages in order.
     */
    suspend fun ensureGmsWhitelisted(context: Context): WhitelistResult = withContext(Dispatchers.IO) {
        if (!hasWritePermission(context)) {
            Log.w(TAG, "Cannot write settings: WRITE_SETTINGS permission missing")
            logAction(context, WhitelistResult.NO_PERMISSION, "Thiếu quyền Sửa đổi cài đặt hệ thống")
            return@withContext WhitelistResult.NO_PERMISSION
        }

        val currentRaw: String?
        try {
            currentRaw = Settings.System.getString(context.contentResolver, KEY_MILLET_NO_RESTRICT_APP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read $KEY_MILLET_NO_RESTRICT_APP: ${e.message}")
            logAction(context, WhitelistResult.WRITE_FAILED, "Lỗi đọc key: ${e.message}")
            return@withContext WhitelistResult.WRITE_FAILED
        }

        val packages = parsePackages(currentRaw)
        if (packages.contains(TARGET_PACKAGE)) {
            Log.d(TAG, "$TARGET_PACKAGE is already present in $KEY_MILLET_NO_RESTRICT_APP")
            logAction(context, WhitelistResult.ALREADY_PRESENT, "GMS đã có sẵn trong $KEY_MILLET_NO_RESTRICT_APP")
            return@withContext WhitelistResult.ALREADY_PRESENT
        }

        // Build new value: preserve existing, append GMS, join with ',' without spaces
        val newValue = if (packages.isEmpty()) {
            TARGET_PACKAGE
        } else {
            (packages + TARGET_PACKAGE).joinToString(",")
        }

        try {
            val written = Settings.System.putString(context.contentResolver, KEY_MILLET_NO_RESTRICT_APP, newValue)
            Log.d(TAG, "Settings.System.putString returned: $written")

            // Read back to confirm write
            val readBack = Settings.System.getString(context.contentResolver, KEY_MILLET_NO_RESTRICT_APP)
            val updatedPackages = parsePackages(readBack)

            if (updatedPackages.contains(TARGET_PACKAGE)) {
                Log.i(TAG, "Successfully added $TARGET_PACKAGE to $KEY_MILLET_NO_RESTRICT_APP. Value: $readBack")
                logAction(context, WhitelistResult.ADDED, "Đã ghi thành công GMS vào whitelist ($KEY_MILLET_NO_RESTRICT_APP)")
                return@withContext WhitelistResult.ADDED
            } else {
                Log.e(TAG, "Write verification failed for $KEY_MILLET_NO_RESTRICT_APP. Got: $readBack")
                logAction(context, WhitelistResult.WRITE_FAILED, "Ghi không thành công (xác nhận lại thất bại)")
                return@withContext WhitelistResult.WRITE_FAILED
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Key unsupported by SettingsProvider: ${e.message}")
            logAction(context, WhitelistResult.KEY_UNSUPPORTED, "Hệ thống từ chối key $KEY_MILLET_NO_RESTRICT_APP")
            return@withContext WhitelistResult.KEY_UNSUPPORTED
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException writing $KEY_MILLET_NO_RESTRICT_APP: ${e.message}")
            logAction(context, WhitelistResult.NO_PERMISSION, "SecurityException: ${e.message}")
            return@withContext WhitelistResult.NO_PERMISSION
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error writing $KEY_MILLET_NO_RESTRICT_APP: ${e.message}")
            logAction(context, WhitelistResult.WRITE_FAILED, "Lỗi không xác định: ${e.message}")
            return@withContext WhitelistResult.WRITE_FAILED
        }
    }

    private suspend fun logAction(context: Context, result: WhitelistResult, details: String) {
        try {
            val db = AppDatabase.getDatabase(context)
            db.heartbeatLogDao().insertLog(
                HeartbeatLogEntity(
                    eventType = "GMS_WHITELIST",
                    success = (result == WhitelistResult.ADDED || result == WhitelistResult.ALREADY_PRESENT),
                    latencyMs = 0L,
                    serverIp = KEY_MILLET_NO_RESTRICT_APP,
                    triggerReason = result.name,
                    details = details
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not log whitelist action: ${e.message}")
        }
    }
}
