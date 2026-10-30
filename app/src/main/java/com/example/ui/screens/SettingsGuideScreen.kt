package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.BuildConfig
import com.example.core.HyperOsUtils
import com.example.data.pref.AppPreferences
import com.example.i18n.LanguageManager
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsGuideScreen(
    appPreferences: AppPreferences,
    themeMode: String,
    languageCode: String
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showReadmeSheet by remember { mutableStateOf(false) }

    var isIgnoringBattery by remember { mutableStateOf(HyperOsUtils.isIgnoringBatteryOptimizations(context)) }
    var canExactAlarm by remember { mutableStateOf(HyperOsUtils.canScheduleExactAlarms(context)) }
    var hasWriteSettings by remember { mutableStateOf(HyperOsUtils.canWriteSystemSettings(context)) }

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    fun refreshPermissions() {
        isIgnoringBattery = HyperOsUtils.isIgnoringBatteryOptimizations(context)
        canExactAlarm = HyperOsUtils.canScheduleExactAlarms(context)
        hasWriteSettings = HyperOsUtils.canWriteSystemSettings(context)
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

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(str("lang_selector"), fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(LanguageManager.supportedLanguages.size) { index ->
                        val lang = LanguageManager.supportedLanguages[index]
                        val isSelected = languageCode == lang.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    appPreferences.setLanguageCode(lang.code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = lang.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = lang.nativeName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (showReadmeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReadmeSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ReadmeSheetContent(
                version = "1.6.1",
                onDismiss = { showReadmeSheet = false }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_guide_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Permissions & Battery Optimization Card
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
                    .testTag("settings_permissions_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Quyền hệ thống & Tối ưu pin",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Battery Optimization Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIgnoringBattery) Icons.Default.CheckCircle else Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = if (isIgnoringBattery) StatusGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bỏ qua tối ưu hóa pin",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isIgnoringBattery) "Đã bỏ qua tối ưu pin (Hoàn hảo - Không bị hạn chế chạy ngầm)" else "Đang bị tối ưu hóa (Có thể làm trễ thông báo khi tắt màn hình)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isIgnoringBattery) StatusGreen else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
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
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("settings_request_ignore_battery_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bỏ qua tối ưu pin ngay",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Exact Alarm Permission (Android 12+)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (canExactAlarm) Icons.Default.CheckCircle else Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (canExactAlarm) StatusGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Báo thức chính xác (SCHEDULE_EXACT_ALARM)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (canExactAlarm) "Đã cấp quyền (Đánh thức định kỳ chuẩn xác khi ngủ sâu)" else "Chưa cấp quyền (Cần thiết trên Android 12+ khi màn hình tắt)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (canExactAlarm) StatusGreen else MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (!canExactAlarm) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("settings_request_exact_alarm_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cấp quyền Báo thức chính xác",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. HyperOS Autostart Shortcut
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Tự khởi chạy HyperOS",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Bật Tự khởi chạy để FCM Guard luôn tự khởi động cùng hệ thống",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { HyperOsUtils.openHyperOsAutostartSettings(context) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Mở cài đặt Tự khởi chạy HyperOS", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. HyperOS Battery Saver: No Restrictions
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Tiết kiệm pin: Không hạn chế",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Chọn 'Không hạn chế' trong Pin & Hiệu suất để HyperOS không đóng tiến trình",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { HyperOsUtils.openHyperOsBatteryNoRestrictions(context) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Đặt Pin: Không hạn chế (No restrictions)", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // App Preferences (Theme & Language) Card
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
                        text = "Tùy biến ứng dụng",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showLanguageDialog = true }
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = str("lang_selector"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        val currentLangObj = LanguageManager.supportedLanguages.firstOrNull { it.code == languageCode }
                        Text(
                            text = currentLangObj?.displayName ?: "Tiếng Việt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme Selector Chips
                    Text(
                        text = str("theme_selector"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("SYSTEM", "Hệ thống", Icons.Default.Palette),
                            Triple("LIGHT", "Sáng", Icons.Default.LightMode),
                            Triple("DARK", "Tối", Icons.Default.DarkMode)
                        ).forEach { (mode, label, icon) ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { appPreferences.setThemeMode(mode) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Banking & Security Guarantee Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = str("security_assurance"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = str("security_assurance_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 4-Step Quick Setup Guide
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = str("guide_title"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GuideStepItem(
                        step = 1,
                        title = "Cài đặt & Cấp quyền",
                        desc = "Vào 'Sửa đổi cài đặt hệ thống' và gạt bật cho FCM Guard."
                    )
                    GuideStepItem(
                        step = 2,
                        title = "Sửa kết nối lần đầu",
                        desc = "Nhấn 'Sửa lỗi ngay' tại trang Bảng điều khiển để kích hoạt socket GMS."
                    )
                    GuideStepItem(
                        step = 3,
                        title = "Bật Tự động bảo vệ",
                        desc = "Đảm bảo công tắc Tự động bảo vệ thông minh luôn được bật."
                    )
                    GuideStepItem(
                        step = 4,
                        title = "Cấu hình HyperOS",
                        desc = "Bật Tự khởi chạy cho FCM Guard và chọn Pin: Không hạn chế."
                    )
                }
            }
        }

        // Troubleshooting Individual Delayed Apps (Zalo, Telegram...)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = str("troubleshoot_title"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Nếu có 1 ứng dụng cá biệt (ví dụ Zalo, Telegram, WhatsApp) bị chậm:\n" +
                                "1. Vào Thông tin ứng dụng đó → Tiết kiệm pin → Chọn 'Không hạn chế'.\n" +
                                "2. Vào Bảo mật HyperOS → Tự khởi chạy → Bật cho ứng dụng đó.\n" +
                                "3. Mở Đa nhiệm (Recent Apps) → Giữ ô ứng dụng → Nhấn biểu tượng Ổ Khóa.\n" +
                                "4. Mở FCM Guard nhấn 'Sửa lỗi ngay' để làm mới socket toàn máy.\n" +
                                "5. Hoặc nhấn 'Xóa dữ liệu Pin & Hiệu suất' tại Bảng điều khiển để reset giới hạn ngắt tiến trình.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Application Version & Readme Button
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FCM Guard — Push Notification Guardian",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Phiên bản 1.5.0 (Build 6) • Xiaomi HyperOS & Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showReadmeSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("open_readme_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = str("btn_view_readme"),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStepItem(
    step: Int,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReadmeSheetContent(
    version: String,
    onDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "🛡️ FCM Guard — Push Notification Guardian",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Phiên bản $version (Build 8) • Xiaomi HyperOS & Android",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Giải pháp tối ưu hóa và duy trì thông báo đẩy Firebase Cloud Messaging (FCM) dành riêng cho các thiết bị Xiaomi HyperOS / MIUI (kể cả bản nội địa Trung Quốc) và Android — Tiết kiệm pin, không cần Root, thân thiện 100% với ứng dụng ngân hàng.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }

        // Section: File APK
        item {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📦 File APK cài đặt (FCM_Guard.apk)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• File APK: FCM_Guard.apk\n" +
                               "• Vị trí: Đặt trực tiếp tại thư mục gốc của dự án (/)\n" +
                               "• Phiên bản: $version (Build 8)\n" +
                               "• targetSdk: 35 (Android 15 tiêu chuẩn, cập nhật mượt mà trực tiếp đè lên bản cũ không bị lỗi -26, giao diện tràn viền toàn màn hình không bị vệt đen)\n" +
                               "• Cách tải: Click chuột phải vào file FCM_Guard.apk trong cây thư mục bên trái → Chọn 'Download' (hoặc xuất file ZIP dự án).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section: HyperOS MILLET_NO_RESTRICT_APP & TargetSdk 35 Breakdown
        item {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "⚡ Đột phá cơ chế: MILLET_NO_RESTRICT_APP & ContentObserver",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Nguyên nhân gốc lỗi tắt màn hình mất thông báo: Trên Xiaomi HyperOS China ROM, tiến trình Greezer và PowerKeeper tự động đóng băng Google Play Services (GMS) và cắt socket MCS khi 'com.google.android.gms' không nằm trong danh sách cài đặt hệ thống Settings.System.MILLET_NO_RESTRICT_APP.\n\n" +
                               "• Chuẩn targetSdk 35 hiện đại: Giữ nguyên permission model hiện đại giúp việc cập nhật đè lên app cũ diễn ra bình thường (không bị lỗi -26 Can't downgrade permissions), đồng thời hỗ trợ giao diện tràn viền mượt mà (Edge-to-Edge) không bị vệt đen letterbox.\n\n" +
                               "• Giám sát thời gian thực (ContentObserver Watchdog): HyperOS thường xuyên tự động dựng lại danh sách MILLET_NO_RESTRICT_APP và xóa GMS ra ngoài. FCM Guard đăng ký ContentObserver lắng nghe URI cài đặt này; ngay khi HyperOS xóa GMS, FCM Guard tự động ghi lại GMS ngay lập tức (debounce ~1s)!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section: Overview
        item {
            Text(
                text = "🌟 Giới thiệu tổng quan",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "FCM Guard được thiết kế theo triết lý: Đặc quyền thấp nhất – Độ tin cậy cao nhất – Tuyệt đối an toàn cho tài chính. Ứng dụng khôi phục và duy trì kênh nhận tin push bằng whitelist hệ thống và Intent broadcast tiêu chuẩn mà không can thiệp sâu vào nhân hệ điều hành.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        // Section: Key Features
        item {
            Text(
                text = "🚀 Các tính năng chính v1.6.0",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))

            val features = listOf(
                "1. Whitelist HyperOS & ContentObserver tự động" to "Tự động ghi 'com.google.android.gms' vào Settings.System.MILLET_NO_RESTRICT_APP và canh chừng 24/7 để khôi phục ngay khi HyperOS cố ý xóa bỏ.",
                "2. Thân thiện tuyệt đối với ứng dụng ngân hàng" to "Không Root, Không Shizuku, Không Accessibility Service, Không VPN ngầm, Không Device Admin. 100% an toàn với tất cả các ứng dụng ngân hàng và ví điện tử.",
                "3. Chống hao pin thông minh" to "Hủy chu kỳ báo thức CPU ngay khi màn hình sáng hoặc người dùng mở khóa máy (ACTION_SCREEN_ON, ACTION_USER_PRESENT). Chỉ duy trì chu kỳ đánh thức ngắn khi màn hình tắt lâu.",
                "4. Nút xóa dữ liệu Pin & Hiệu suất (Powerkeeper)" to "Mở trực tiếp trang thông tin ứng dụng com.miui.powerkeeper để xóa sạch dữ liệu giới hạn tiến trình tích tụ của HyperOS.",
                "5. Quét & Phân loại ứng dụng FCM" to "Phân loại 3 danh mục rõ ràng: Đã bật, Đã tắt, Không rõ. Nhấn vào bất kỳ app nào để mở trang Cài đặt cấu hình nhanh.",
                "6. Chẩn đoán kết nối Socket thời gian thực" to "Kiểm tra cổng TCP socket mtalk.google.com:5228, độ trễ DNS và phím tắt chẩn đoán Google Play Services (*#*#426#*#*).",
                "7. Giao diện Đen Trắng tối giản & Đa ngôn ngữ" to "Hỗ trợ 11 ngôn ngữ, thiết kế đen trắng sắc nét không gây lóa mắt, bố cục phím bấm mở rộng chống tràn chữ trên mọi kích thước màn hình."
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                features.forEach { (title, desc) ->
                    OutlinedCard(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Screen-Off Messenger Troubleshooting
        item {
            OutlinedCard(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 Mẹo sửa lỗi Messenger & Chat khi tắt màn hình lâu",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nếu Messenger (Facebook Messenger), Zalo, Telegram không thông báo khi để tắt màn hình lâu:\n\n" +
                                "1️⃣ Tự khởi chạy: Vào Thông tin ứng dụng Messenger → Bật 'Tự khởi chạy'. (Bắt buộc để HyperOS không chặn Google Play Services đánh thức Messenger).\n" +
                                "2️⃣ Tiết kiệm pin: Chọn 'Không hạn chế' (No restrictions).\n" +
                                "3️⃣ Cài đặt Pin HyperOS: Vào ứng dụng Bảo mật → Pin → Cài đặt (icon bánh răng) → Đặt 'Tắt dữ liệu di động khi thiết bị bị khóa' là 'Không bao giờ'.\n" +
                                "4️⃣ Khóa trong Đa nhiệm: Mở màn hình Đa nhiệm (Recent apps) → Nhấn giữ ô Messenger → Bấm biểu tượng Ổ Khóa.\n" +
                                "5️⃣ Xóa dữ liệu Pin & Hiệu suất: Tại Bảng điều khiển FCM Guard, nhấn 'Mở trang Thông tin ứng dụng để Xóa dữ liệu' của Pin & Hiệu suất (com.miui.powerkeeper) để xóa các giới hạn ngắt tiến trình tích tụ.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Section: 4-Step Quick Setup
        item {
            Text(
                text = "🛠️ Hướng dẫn cài đặt nhanh 4 bước",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Bước 1: Mở ứng dụng, tại Bảng điều khiển cấp quyền 'Sửa đổi cài đặt hệ thống' (nếu có).\n" +
                        "• Bước 2: Nhấn 'Sửa lỗi ngay' để khởi động lại socket MCS của Google Play Services.\n" +
                        "• Bước 3: Đảm bảo công tắc 'Tự động bảo vệ thông minh' luôn được BẬT.\n" +
                        "• Bước 4: Cấp quyền 'Tự khởi chạy' cho FCM Guard và chọn Tiết kiệm pin là 'Không hạn chế'.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        // Section: Tech Stack & Security
        item {
            Text(
                text = "🏗️ Công nghệ & An toàn",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Ngôn ngữ: 100% Kotlin hiện đại\n" +
                        "• UI: Jetpack Compose + Material Design 3 đơn sắc tối giản\n" +
                        "• Kiến trúc: MVVM / Clean Architecture\n" +
                        "• Dịch vụ ngầm: Foreground Service + AlarmManager + WakeLock ngắn bảo vệ Doze\n" +
                        "• Cơ sở dữ liệu: Room Database (lưu trữ nhật ký cục bộ, tuyệt đối không gửi dữ liệu ra ngoài)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Đã hiểu và Đóng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
