package com.example.ui.screens

import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.GmsHeartbeatManager
import com.example.core.HyperOsUtils
import com.example.data.db.AppDatabase
import com.example.data.pref.AppPreferences
import com.example.i18n.LanguageManager
import com.example.service.FcmGuardService
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun DashboardScreen(
    appPreferences: AppPreferences,
    isServiceRunning: Boolean,
    isAutoGuard: Boolean,
    isNotificationEnabled: Boolean,
    backupInterval: Int,
    lastRepairTimestamp: Long,
    lastLatencyMs: Long,
    lastSocketStatus: String,
    languageCode: String,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRepairing by remember { mutableStateOf(false) }

    val isScreenOn by appPreferences.isScreenOn.collectAsState()
    val isDeviceSleeping by appPreferences.isDeviceSleeping.collectAsState()
    val lastTriggerReason by appPreferences.lastTriggerReason.collectAsState()
    val screenOffInterval by appPreferences.screenOffIntervalMinutes.collectAsState()
    val screenOnInterval by appPreferences.screenOnIntervalMinutes.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val isBatteryOptimizationIgnoredPref by appPreferences.isBatteryOptimizationIgnored.collectAsState()
    val canScheduleExactAlarmsPref by appPreferences.canScheduleExactAlarms.collectAsState()

    var isIgnoringBattery by remember { mutableStateOf(HyperOsUtils.isIgnoringBatteryOptimizations(context)) }
    var canExactAlarm by remember { mutableStateOf(HyperOsUtils.canScheduleExactAlarms(context)) }
    var hasWriteSettings by remember { mutableStateOf(HyperOsUtils.canWriteSystemSettings(context)) }

    val isGmsWhitelisted by appPreferences.isGmsWhitelisted.collectAsState()
    val milletRawValue by appPreferences.milletRawValue.collectAsState()
    val lastWhitelistResult by appPreferences.lastWhitelistResult.collectAsState()

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    fun refreshPermissions() {
        isIgnoringBattery = HyperOsUtils.isIgnoringBatteryOptimizations(context)
        canExactAlarm = HyperOsUtils.canScheduleExactAlarms(context)
        hasWriteSettings = HyperOsUtils.canWriteSystemSettings(context)
        val status = com.example.core.GmsWhitelistManager.getStatus(context)
        appPreferences.updateWhitelistState(status.isGmsWhitelisted, status.rawValue, status.lastResult)
        appPreferences.updateSystemPermissions(isIgnoringBattery, canExactAlarm)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Core Feature Card: Xiaomi HyperOS Whitelist (MILLET_NO_RESTRICT_APP)
        item {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isGmsWhitelisted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.error
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gms_whitelist_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isGmsWhitelisted) StatusGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isGmsWhitelisted) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isGmsWhitelisted) StatusGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Whitelist HyperOS (GMS)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Settings.System.MILLET_NO_RESTRICT_APP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Whitelist status badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isGmsWhitelisted) StatusGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isGmsWhitelisted) StatusGreen else MaterialTheme.colorScheme.error)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isGmsWhitelisted) "GMS: ĐÃ THÊM" else "GMS: CHƯA THÊM",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGmsWhitelisted) StatusGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        // Write settings badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasWriteSettings) StatusGreen.copy(alpha = 0.12f) else StatusAmber.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (hasWriteSettings) StatusGreen else StatusAmber)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasWriteSettings) "Quyền Ghi: Đã cấp" else "Quyền Ghi: Chưa cấp",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasWriteSettings) StatusGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isGmsWhitelisted) {
                            "Dịch vụ Google Play đã có trong danh sách ngoại lệ của HyperOS. Greezer / PowerKeeper sẽ không ngắt kết nối FCM khi tắt màn hình."
                        } else {
                            "CẢNH BÁO: com.google.android.gms chưa có trong MILLET_NO_RESTRICT_APP. HyperOS sẽ đóng băng Google Play Services khi tắt màn hình lâu, làm mất thông báo!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    if (milletRawValue.isNotEmpty()) {
                        val packages = milletRawValue.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Giá trị hiện tại (${packages.size} gói):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            clipboard?.setPrimaryClip(ClipData.newPlainText("MILLET_NO_RESTRICT_APP", milletRawValue))
                                            Toast.makeText(context, "Đã sao chép danh sách gói!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Sao chép",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sao chép",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show each package fully without truncation
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                packages.forEach { pkg ->
                                    val isGms = pkg == "com.google.android.gms"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isGms) Icons.Default.CheckCircle else Icons.Default.Circle,
                                            contentDescription = null,
                                            tint = if (isGms) StatusGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(if (isGms) 14.dp else 6.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pkg,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = if (isGms) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isGms) StatusGreen else MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Giá trị hiện tại: Chưa có gói nào trong danh sách hoặc chưa đọc được",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Action Button: Sửa lỗi ngay
                    Button(
                        onClick = {
                            if (!isRepairing) {
                                isRepairing = true
                                scope.launch {
                                    val writeResult = com.example.core.GmsWhitelistManager.ensureGmsWhitelisted(context)
                                    val db = AppDatabase.getDatabase(context)
                                    val socketRes = GmsHeartbeatManager.triggerHeartbeat(
                                        context,
                                        "MANUAL_REPAIR",
                                        db.heartbeatLogDao()
                                    )
                                    appPreferences.updateLastRepair(
                                        System.currentTimeMillis(),
                                        socketRes.latencyMs,
                                        if (socketRes.success) "OK" else "ERROR",
                                        "MANUAL_REPAIR"
                                    )
                                    refreshPermissions()
                                    isRepairing = false

                                    val msg = when (writeResult) {
                                        com.example.core.WhitelistResult.ALREADY_PRESENT ->
                                            "GMS đã có sẵn trong danh sách MILLET_NO_RESTRICT_APP của HyperOS!"
                                        com.example.core.WhitelistResult.ADDED ->
                                            "Thành công! Đã ghi com.google.android.gms vào MILLET_NO_RESTRICT_APP!"
                                        com.example.core.WhitelistResult.NO_PERMISSION -> {
                                            HyperOsUtils.openWriteSettingsPermission(context)
                                            "Thiếu quyền 'Sửa đổi cài đặt hệ thống'. Vui lòng gạt BẬT quyền cho FCM Guard!"
                                        }
                                        com.example.core.WhitelistResult.WRITE_FAILED,
                                        com.example.core.WhitelistResult.KEY_UNSUPPORTED ->
                                            "Ghi bị từ chối, kiểm tra targetSdk phải là 22 và quyền Sửa đổi cài đặt hệ thống!"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isRepairing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 52.dp)
                            .testTag("whitelist_repair_now_button")
                    ) {
                        if (isRepairing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Đang kiểm tra & ghi Whitelist...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGmsWhitelisted) "Kiểm tra lại & Giữ kết nối ngay" else "Sửa lỗi ngay: Thêm GMS vào Whitelist",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Network Socket Check to Google (Honest Labeling)
        item {
            MinimalHeroStatusCard(
                isServiceRunning = isServiceRunning && isAutoGuard,
                isScreenOn = isScreenOn,
                isDeviceSleeping = isDeviceSleeping,
                lastSocketStatus = lastSocketStatus,
                lastTriggerReason = lastTriggerReason,
                lastRepairTimestamp = lastRepairTimestamp,
                lastLatencyMs = lastLatencyMs,
                isRepairing = isRepairing,
                str = ::str,
                onRepairNow = {
                    if (!isRepairing) {
                        isRepairing = true
                        scope.launch {
                            val writeResult = com.example.core.GmsWhitelistManager.ensureGmsWhitelisted(context)
                            val db = AppDatabase.getDatabase(context)
                            val res = GmsHeartbeatManager.triggerHeartbeat(
                                context,
                                "MANUAL_REPAIR",
                                db.heartbeatLogDao()
                            )
                            appPreferences.updateLastRepair(
                                System.currentTimeMillis(),
                                res.latencyMs,
                                if (res.success) "OK" else "ERROR",
                                "MANUAL_REPAIR"
                            )
                            refreshPermissions()
                            isRepairing = false
                        }
                    }
                }
            )
        }

        // Battery Optimization Warning Card (If not yet ignored)
        item {
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isIgnoringBattery) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isIgnoringBattery) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_optimization_alert_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isIgnoringBattery) StatusGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIgnoringBattery) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                                contentDescription = null,
                                tint = if (isIgnoringBattery) StatusGreen else MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isIgnoringBattery) "Đã bỏ qua tối ưu pin (Hoàn hảo)" else "Bỏ qua tối ưu pin (Bắt buộc)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isIgnoringBattery) "Hệ thống sẽ không bao giờ ngắt FCM Guard khi chạy ngầm" else "Cần cấp để dịch vụ không bị dừng khi tắt màn hình lâu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!isIgnoringBattery) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                HyperOsUtils.requestIgnoreBatteryOptimization(context)
                                refreshPermissions()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 46.dp)
                                .testTag("request_ignore_battery_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bỏ qua tối ưu pin ngay",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Exact Alarm Permission Card (Android 12+) if not yet granted
        if (!canExactAlarm) {
            item {
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exact_alarm_alert_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Báo thức chính xác (Android 12+)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Cần quyền để gửi heartbeat chuẩn xác từng phút khi tắt màn hình lâu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                HyperOsUtils.requestScheduleExactAlarm(context)
                                refreshPermissions()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 46.dp)
                                .testTag("request_exact_alarm_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cấp quyền Báo thức chính xác",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Feature: Clear All Data Battery & Performance
        item {
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_performance_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = str("btn_clear_battery_performance"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "com.miui.powerkeeper",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = str("btn_clear_battery_performance_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button with flexible height and no text clipping
                    Button(
                        onClick = {
                            val opened = HyperOsUtils.openBatteryAndPerformanceAppInfo(context)
                            if (!opened) {
                                Toast.makeText(
                                    context,
                                    "Không tìm thấy ứng dụng Pin & Hiệu suất trên thiết bị này",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 54.dp)
                            .testTag("open_battery_performance_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Mở trang Thông tin ứng dụng để Xóa dữ liệu",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Pin & Hiệu suất (com.miui.powerkeeper)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Messenger & Screen-Off Notification Fix Card
        item {
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("messenger_fix_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sửa lỗi Messenger & Chat khi tắt màn hình",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Facebook Messenger, Zalo, Telegram...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Khi tắt màn hình lâu, HyperOS đưa thiết bị vào chế độ Ngủ sâu (Deep Sleep). FCM Guard đã kích hoạt BroadcastReceiver và AlarmManager chính xác để đánh thức socket MCS ngầm. Để Messenger nhận tin nhắn ngay lập tức, bạn bắt buộc cần cấu hình:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Bước 1: Mở Messenger → Bật 'Tự khởi chạy' và chọn Tiết kiệm pin là 'Không hạn chế'.\n" +
                                "• Bước 2: Trong Cài đặt Pin HyperOS → Đặt 'Tắt dữ liệu di động khi thiết bị bị khóa' là 'Không bao giờ'.\n" +
                                "• Bước 3: Mở Đa nhiệm (Recent apps) → Nhấn giữ ô Messenger → Bấm hình Ổ Khóa.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val opened = HyperOsUtils.openMessengerSettings(context)
                                if (!opened) {
                                    Toast.makeText(
                                        context,
                                        "Chưa cài Messenger hoặc hãy mở mục Ứng dụng để cấu hình",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("btn_open_messenger_settings")
                        ) {
                            Text(
                                text = "Cài đặt Messenger",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val opened = HyperOsUtils.openHyperOsBatterySettings(context)
                                if (!opened) {
                                    Toast.makeText(
                                        context,
                                        "Mở Cài đặt hệ thống → Pin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("btn_open_battery_settings")
                        ) {
                            Text(
                                text = "Cài đặt Pin HyperOS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Auto Guard & Dual Keep-Alive Interval Controls
        item {
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = str("switch_auto_guard"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tự động lắng nghe tắt/bật màn hình, mở khóa và gửi heartbeat định kỳ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAutoGuard) str("status_active") else str("status_inactive"),
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAutoGuard) StatusGreen else MaterialTheme.colorScheme.error
                        )
                        Switch(
                            checked = isAutoGuard,
                            onCheckedChange = { enabled ->
                                appPreferences.setAutoGuardEnabled(enabled)
                                if (enabled) {
                                    FcmGuardService.start(context)
                                } else {
                                    FcmGuardService.stop(context)
                                }
                            },
                            modifier = Modifier.testTag("auto_guard_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Screen-Off Heartbeat Interval (10m / 15m)
                    Text(
                        text = "Chu kỳ khi tắt màn hình (Thiết bị ngủ)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Duy trì socket khi tắt màn hình lâu để không bị trễ tin nhắn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15).forEach { mins ->
                            val selected = screenOffInterval == mins
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    appPreferences.setScreenOffIntervalMinutes(mins)
                                    FcmGuardService.updateConfig(context)
                                },
                                label = {
                                    Text(
                                        text = "$mins ${if (mins == 10) "phút (Tối ưu)" else "phút"}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("screen_off_chip_$mins")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Screen-On Heartbeat Interval (15m / 30m / 60m)
                    Text(
                        text = "Chu kỳ khi màn hình sáng (Đang sử dụng)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 60).forEach { mins ->
                            val selected = screenOnInterval == mins
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    appPreferences.setScreenOnIntervalMinutes(mins)
                                    FcmGuardService.updateConfig(context)
                                },
                                label = {
                                    Text(
                                        text = "$mins ${if (mins == 30) "phút (Chuẩn)" else "phút"}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("screen_on_chip_$mins")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Persistent Notification Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = str("switch_notification"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = str("switch_notification_desc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { enabled ->
                                appPreferences.setPersistentNotificationEnabled(enabled)
                                FcmGuardService.updateConfig(context)
                            },
                            modifier = Modifier.testTag("notification_switch")
                        )
                    }
                }
            }
        }

        // HyperOS System Optimization Shortcuts
        item {
            Text(
                text = str("hyperos_shortcuts"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        item {
            MinimalShortcutCard(
                title = str("btn_autostart"),
                subtitle = "Bật tự khởi chạy cho FCM Guard để dịch vụ luôn duy trì sau khi tắt ứng dụng",
                icon = Icons.Default.Autorenew,
                statusText = "Mở cài đặt",
                onClick = {
                    HyperOsUtils.openHyperOsAutostartSettings(context)
                    refreshPermissions()
                }
            )
        }

        item {
            MinimalShortcutCard(
                title = str("btn_battery"),
                subtitle = "Đặt 'Không hạn chế' để HyperOS không đóng ngắt tiến trình ngầm",
                icon = Icons.Default.BatteryAlert,
                statusText = if (isIgnoringBattery) "Đã tối ưu" else "Cần cấp",
                statusOk = isIgnoringBattery,
                onClick = {
                    HyperOsUtils.openBatteryOptimizationSettings(context)
                    refreshPermissions()
                }
            )
        }

        item {
            MinimalShortcutCard(
                title = str("btn_write_settings"),
                subtitle = "Quyền sửa đổi cài đặt hệ thống tiêu chuẩn (thân thiện 100% app ngân hàng)",
                icon = Icons.Default.Settings,
                statusText = if (hasWriteSettings) "Đã cấp" else "Cần cấp",
                statusOk = hasWriteSettings,
                onClick = {
                    HyperOsUtils.openWriteSettingsPermission(context)
                    refreshPermissions()
                }
            )
        }
    }
}

@Composable
private fun MinimalHeroStatusCard(
    isServiceRunning: Boolean,
    isScreenOn: Boolean,
    isDeviceSleeping: Boolean,
    lastSocketStatus: String,
    lastTriggerReason: String,
    lastRepairTimestamp: Long,
    lastLatencyMs: Long,
    isRepairing: Boolean,
    str: (String) -> String,
    onRepairNow: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield Guard",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isServiceRunning) str("status_active") else str("status_inactive"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Socket Status Badge & Screen State Badge (Balanced 50/50 row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Socket Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (lastSocketStatus) {
                                    "OK" -> StatusGreen
                                    "ERROR" -> StatusRed
                                    else -> StatusAmber
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (lastSocketStatus) {
                            "OK" -> "Socket: Kết nối tốt"
                            "ERROR" -> "Lỗi socket"
                            "DISCONNECTED" -> "Mất mạng"
                            else -> "Đang chờ socket"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Screen & Sleep State Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isScreenOn) StatusGreen else StatusAmber)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScreenOn) "Màn hình: Sáng" else "Màn hình: Tắt (Ngủ)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (lastRepairTimestamp > 0) {
                val formattedTime = DateFormat.format("HH:mm:ss dd/MM", Date(lastRepairTimestamp)).toString()
                val reasonDisplay = when {
                    lastTriggerReason.startsWith("USER_PRESENT") -> "Mở khóa màn hình"
                    lastTriggerReason.startsWith("SCREEN_ON") -> "Bật màn hình"
                    lastTriggerReason.startsWith("SCREEN_OFF_ALARM") -> "Báo thức đánh thức CPU"
                    lastTriggerReason.startsWith("PERIODIC_SCREEN_OFF") -> "Định kỳ khi tắt màn hình"
                    lastTriggerReason.startsWith("PERIODIC_SCREEN_ON") -> "Định kỳ khi màn hình sáng"
                    lastTriggerReason.startsWith("NETWORK_RESTORED") -> "Mạng phục hồi"
                    lastTriggerReason.startsWith("MANUAL_REPAIR") -> "Thủ công"
                    else -> lastTriggerReason
                }

                Text(
                    text = "Lần làm mới: $formattedTime • ${if (lastLatencyMs > 0) "${lastLatencyMs}ms" else "OK"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (reasonDisplay.isNotEmpty()) {
                    Text(
                        text = "Lý do: $reasonDisplay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Kiểm tra kết nối TCP socket mtalk.google.com:5228",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button: Test Network to Google
            Button(
                onClick = onRepairNow,
                enabled = !isRepairing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("repair_now_button")
            ) {
                if (isRepairing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang kiểm tra mạng...",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kiểm tra mạng mtalk.google.com",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}


@Composable
private fun MinimalShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusText: String,
    statusOk: Boolean? = null,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (statusOk) {
                            true -> StatusGreen.copy(alpha = 0.12f)
                            false -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (statusOk) {
                        true -> StatusGreen
                        false -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}
