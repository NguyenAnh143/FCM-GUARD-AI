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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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

    val hasWriteSettings = remember(lastRepairTimestamp) { HyperOsUtils.canWriteSystemSettings(context) }
    val isIgnoringBattery = remember(lastRepairTimestamp) { HyperOsUtils.isIgnoringBatteryOptimizations(context) }

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Minimal Monochrome Hero Status Card
        item {
            MinimalHeroStatusCard(
                isServiceRunning = isServiceRunning && isAutoGuard,
                lastSocketStatus = lastSocketStatus,
                lastRepairTimestamp = lastRepairTimestamp,
                lastLatencyMs = lastLatencyMs,
                isRepairing = isRepairing,
                str = ::str,
                onRepairNow = {
                    if (!isRepairing) {
                        isRepairing = true
                        scope.launch {
                            val db = AppDatabase.getDatabase(context)
                            val res = GmsHeartbeatManager.triggerHeartbeat(
                                context,
                                "MANUAL_REPAIR",
                                db.heartbeatLogDao()
                            )
                            appPreferences.updateLastRepair(
                                System.currentTimeMillis(),
                                res.latencyMs,
                                if (res.success) "OK" else "ERROR"
                            )
                            isRepairing = false
                        }
                    }
                }
            )
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 58.dp)
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
                                modifier = Modifier.weight(1f, fill = false),
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
                        text = "Khi tắt màn hình lâu, HyperOS đưa thiết bị vào chế độ Ngủ sâu (Deep Sleep). FCM Guard đã kích hoạt AlarmManager chính xác để đánh thức socket MCS ngầm. Để Messenger nhận tin nhắn ngay lập tức, bạn bắt buộc cần 2 cài đặt sau:",
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

        // Auto Guard & Notification Controls
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
                        text = str("switch_auto_guard_desc"),
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

                    // Backup Interval Selection
                    Text(
                        text = str("backup_interval_title"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 15, 30).forEach { mins ->
                            val selected = backupInterval == mins
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    appPreferences.setBackupIntervalMinutes(mins)
                                    FcmGuardService.updateConfig(context)
                                },
                                label = {
                                    Text(
                                        text = "$mins ${if (mins == 30) "(Mặc định)" else "phút"}",
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("interval_chip_$mins")
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
                onClick = { HyperOsUtils.openHyperOsAutostartSettings(context) }
            )
        }

        item {
            MinimalShortcutCard(
                title = str("btn_battery"),
                subtitle = "Đặt 'Không hạn chế' để HyperOS không đóng ngắt tiến trình ngầm",
                icon = Icons.Default.BatteryAlert,
                statusText = if (isIgnoringBattery) "Đã tối ưu" else "Cần cấp",
                statusOk = isIgnoringBattery,
                onClick = { HyperOsUtils.openBatteryOptimizationSettings(context) }
            )
        }

        item {
            MinimalShortcutCard(
                title = str("btn_write_settings"),
                subtitle = "Quyền sửa đổi cài đặt hệ thống tiêu chuẩn (thân thiện 100% app ngân hàng)",
                icon = Icons.Default.Settings,
                statusText = if (hasWriteSettings) "Đã cấp" else "Cần cấp",
                statusOk = hasWriteSettings,
                onClick = { HyperOsUtils.openWriteSettingsPermission(context) }
            )
        }
    }
}

@Composable
private fun MinimalHeroStatusCard(
    isServiceRunning: Boolean,
    lastSocketStatus: String,
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Shield Guard",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isServiceRunning) str("status_active") else str("status_inactive"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Socket Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (lastSocketStatus == "OK") StatusGreen else StatusAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lastSocketStatus == "OK") str("socket_online") else str("socket_offline"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (lastRepairTimestamp > 0) {
                val formattedTime = DateFormat.format("HH:mm:ss dd/MM", Date(lastRepairTimestamp)).toString()
                Text(
                    text = "Lần làm mới: $formattedTime • ${if (lastLatencyMs > 0) "${lastLatencyMs}ms" else "OK"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Repair Now
            Button(
                onClick = onRepairNow,
                enabled = !isRepairing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("repair_now_button")
            ) {
                if (isRepairing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = str("repairing"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = str("btn_repair_now"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
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
