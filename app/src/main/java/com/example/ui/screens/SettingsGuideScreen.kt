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
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showReadmeSheet by remember { mutableStateOf(false) }

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

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
                version = "1.3.0",
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
                        text = "Phiên bản 1.3.0 (Build 4) • Xiaomi HyperOS & Android",
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
                text = "Phiên bản $version (Build 4) • Xiaomi HyperOS & Android",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Giải pháp tối ưu hóa và duy trì thông báo đẩy Firebase Cloud Messaging (FCM) dành riêng cho các thiết bị Xiaomi HyperOS / MIUI và Android — Tiết kiệm pin, không cần Root, thân thiện 100% với ứng dụng ngân hàng.",
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
                               "• Vị trí: Đặt trực tiếp tại thư mục gốc của dự án (/\n" +
                               "• Phiên bản: $version (Build 4)\n" +
                               "• Cách tải: Click chuột phải vào file FCM_Guard.apk trong cây thư mục bên trái → Chọn 'Download' (hoặc xuất file ZIP dự án).",
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
                text = "Trên hệ điều hành Xiaomi HyperOS và MIUI, cơ chế quản lý pin nghiêm ngặt thường xuyên ngắt kết nối socket ngầm của Google Play Services (GMS) tới máy chủ Google FCM (mtalk.google.com:5228), khiến thông báo từ các ứng dụng như Facebook Messenger, Zalo, Telegram, WhatsApp... bị trễ hoặc chỉ xuất hiện khi người dùng mở ứng dụng.\n\n" +
                        "FCM Guard được thiết kế theo triết lý: Đặc quyền thấp nhất – Độ tin cậy cao nhất – Tuyệt đối an toàn cho tài chính. Ứng dụng khôi phục và duy trì kênh nhận tin push bằng các Intent hệ thống tiêu chuẩn mà không can thiệp sâu vào nhân hệ điều hành.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        // Section: 8 Key Features
        item {
            Text(
                text = "🚀 Các tính năng chính",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))

            val features = listOf(
                "1. Thân thiện tuyệt đối với ứng dụng ngân hàng" to "Không Root, Không Shizuku, Không Accessibility Service (trợ năng), Không VPN ngầm, Không Device Admin. Tuyệt đối không bị các ứng dụng ngân hàng (VCB Digibank, Techcombank, MB Bank, BIDV, VPBank, MoMo, VNPay...) phát hiện hay từ chối mở.",
                "2. Giám sát thông minh theo sự kiện & Siêu tiết kiệm pin" to "Chỉ kích hoạt kiểm tra khi có sự kiện mạng phục hồi hoặc khi bật sáng màn hình. Tùy chọn chu kỳ bảo vệ dự phòng: 10 phút, 15 phút, 30 phút (Mặc định). Tích hợp AlarmManager chính xác với WakeLock ngắn để duy trì socket khi tắt màn hình lâu mà không tốn pin.",
                "3. Thông báo chạy ngầm tùy chọn (Persistent Notification)" to "Cho phép bật/tắt hiển thị thông báo chạy ngầm tùy ý để thanh thông báo luôn gọn gàng.",
                "4. Trợ lý ứng dụng FCM & Đọc trạng thái Autostart HyperOS" to "Tự động quét toàn bộ ứng dụng sử dụng FCM. Đọc trạng thái Tự khởi chạy HyperOS (OP_AUTO_START = 10008) và phân loại 3 danh mục rõ ràng: Đã bật, Đã tắt, Không rõ. Nhấn vào bất kỳ app nào để mở trang Cài đặt cấu hình nhanh.",
                "5. Chẩn đoán kết nối Socket thời gian thực" to "Kiểm tra trực tiếp cổng TCP socket mtalk.google.com:5228, thời gian phân giải DNS, độ trễ TCP Handshake và phím tắt gọi giao diện chẩn đoán Google Play Services (*#*#426#*#*).",
                "6. Nhật ký theo dõi độ trễ & Mất kết nối" to "Lưu trữ lịch sử các lần kiểm tra, đánh thức socket, ghi nhận sự cố mạng giúp người dùng đánh giá độ ổn định kết nối FCM.",
                "7. Giao diện Đa ngôn ngữ & Chế độ Đen Trắng tối giản" to "Hỗ trợ 11 ngôn ngữ (Tiếng Việt, English, 中文, Español, 日本語, 한국어, Français, Deutsch, Русский, Português, Bahasa Indonesia). Phong cách Đen Trắng đơn sắc tối giản, dịu mắt ở cả chế độ Sáng và chế độ Tối.",
                "8. Tối ưu cho màn hình nhỏ & dòng máy Xiaomi" to "Toàn bộ nút bấm, hộp thoại, thanh điều hướng và nhãn chữ được tối ưu để không bị che mất chữ trên các màn hình từ 320dp đến 480dp."
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
