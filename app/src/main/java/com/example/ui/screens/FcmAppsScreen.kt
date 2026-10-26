package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.core.AutostartStatus
import com.example.core.FcmAppItem
import com.example.core.FcmAppScanner
import com.example.core.HyperOsUtils
import com.example.i18n.LanguageManager
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.launch

@Composable
fun FcmAppsScreen(
    languageCode: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apps by remember { mutableStateOf<List<FcmAppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") } // ALL, ENABLED, DISABLED, UNKNOWN

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    fun loadApps() {
        isLoading = true
        scope.launch {
            apps = FcmAppScanner.scanFcmApps(context)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadApps()
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val enabledApps = remember(filteredApps) {
        filteredApps.filter { it.autostartStatus == AutostartStatus.ENABLED }
    }
    val disabledApps = remember(filteredApps) {
        filteredApps.filter { it.autostartStatus == AutostartStatus.DISABLED || it.autostartStatus == AutostartStatus.PARTIAL }
    }
    val unknownApps = remember(filteredApps) {
        filteredApps.filter { it.autostartStatus == AutostartStatus.UNKNOWN }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fcm_apps_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card with HyperOS All Configure Button
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = str("apps_title"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = str("apps_desc"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { loadApps() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { HyperOsUtils.openHyperOsAutostartSettings(context) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("config_all_autostart_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = str("btn_config_all"),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(str("search_hint"), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_search_field")
            )
        }

        // 3 Distinct Category Selector Tabs directly under the Search Bar
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mục 1: Đã tắt
                    CategorySelectCard(
                        title = "Đã tắt",
                        count = disabledApps.size,
                        dotColor = StatusRed,
                        isSelected = selectedCategory == "DISABLED",
                        onClick = {
                            selectedCategory = if (selectedCategory == "DISABLED") "ALL" else "DISABLED"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_disabled_apps")
                    )

                    // Mục 2: Đã bật
                    CategorySelectCard(
                        title = "Đã bật",
                        count = enabledApps.size,
                        dotColor = StatusGreen,
                        isSelected = selectedCategory == "ENABLED",
                        onClick = {
                            selectedCategory = if (selectedCategory == "ENABLED") "ALL" else "ENABLED"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_enabled_apps")
                    )

                    // Mục 3: Không rõ
                    CategorySelectCard(
                        title = "Không rõ",
                        count = unknownApps.size,
                        dotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        isSelected = selectedCategory == "UNKNOWN",
                        onClick = {
                            selectedCategory = if (selectedCategory == "UNKNOWN") "ALL" else "UNKNOWN"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("filter_unknown_apps")
                    )
                }

                // Filter Active Banner & Reset Button
                if (selectedCategory != "ALL") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedCategory) {
                                "DISABLED" -> "Đang lọc: Ứng dụng Đã tắt (${disabledApps.size})"
                                "ENABLED" -> "Đang lọc: Ứng dụng Đã bật (${enabledApps.size})"
                                "UNKNOWN" -> "Đang lọc: Ứng dụng Không rõ (${unknownApps.size})"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                        TextButton(
                            onClick = { selectedCategory = "ALL" },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Hiện tất cả (${filteredApps.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy ứng dụng phù hợp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Display according to selected category or grouped into 3 distinct sections
            when (selectedCategory) {
                "DISABLED" -> {
                    item { CategorySectionHeader(title = "Ứng dụng Đã tắt tự khởi chạy", count = disabledApps.size, color = StatusRed) }
                    items(disabledApps, key = { it.packageName }) { appItem ->
                        FcmAppRowItem(item = appItem, str = ::str, onClick = { HyperOsUtils.openAppDetailsSettings(context, appItem.packageName) })
                    }
                }
                "ENABLED" -> {
                    item { CategorySectionHeader(title = "Ứng dụng Đã bật tự khởi chạy", count = enabledApps.size, color = StatusGreen) }
                    items(enabledApps, key = { it.packageName }) { appItem ->
                        FcmAppRowItem(item = appItem, str = ::str, onClick = { HyperOsUtils.openAppDetailsSettings(context, appItem.packageName) })
                    }
                }
                "UNKNOWN" -> {
                    item { CategorySectionHeader(title = "Ứng dụng Không rõ trạng thái (Bảo mật ROM)", count = unknownApps.size, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(unknownApps, key = { it.packageName }) { appItem ->
                        FcmAppRowItem(item = appItem, str = ::str, onClick = { HyperOsUtils.openAppDetailsSettings(context, appItem.packageName) })
                    }
                }
                else -> {
                    // ALL: Show the 3 distinct categories clearly divided!
                    if (disabledApps.isNotEmpty()) {
                        item {
                            CategorySectionHeader(
                                title = "Mục 1: Đã tắt tự khởi chạy (Cần bật để nhận thông báo tức thì)",
                                count = disabledApps.size,
                                color = StatusRed
                            )
                        }
                        items(disabledApps, key = { "dis_${it.packageName}" }) { appItem ->
                            FcmAppRowItem(item = appItem, str = ::str, onClick = { HyperOsUtils.openAppDetailsSettings(context, appItem.packageName) })
                        }
                    }

                    if (enabledApps.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            CategorySectionHeader(
                                title = "Mục 2: Đã bật tự khởi chạy (Đang hoạt động tốt)",
                                count = enabledApps.size,
                                color = StatusGreen
                            )
                        }
                        items(enabledApps, key = { "enb_${it.packageName}" }) { appItem ->
                            FcmAppRowItem(item = appItem, str = ::str, onClick = { HyperOsUtils.openAppDetailsSettings(context, appItem.packageName) })
                        }
                    }

                    if (unknownApps.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            CategorySectionHeader(
                                title = "Mục 3: Không rõ trạng thái (Hệ thống HyperOS bảo mật)",
                                count = unknownApps.size,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(unknownApps, key = { "unk_${it.packageName}" }) { appItem ->
                            FcmAppRowItem(item = appItem, str = ::str, onClick = { HyperOsUtils.openAppDetailsSettings(context, appItem.packageName) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(
    title: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun FcmAppRowItem(
    item: FcmAppItem,
    str: (String) -> String,
    onClick: () -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("app_item_${item.packageName}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            val bitmap = remember(item.icon) {
                try {
                    item.icon?.toBitmap(48, 48)
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = item.appName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = item.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Autostart Status Badge
            val (badgeBg, badgeFg, labelText) = when (item.autostartStatus) {
                AutostartStatus.ENABLED -> Triple(StatusGreen.copy(alpha = 0.12f), StatusGreen, "Đã bật")
                AutostartStatus.PARTIAL -> Triple(StatusAmber.copy(alpha = 0.12f), StatusAmber, "Một phần")
                AutostartStatus.DISABLED -> Triple(StatusRed.copy(alpha = 0.12f), StatusRed, "Đã tắt")
                AutostartStatus.UNKNOWN -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "Không rõ")
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = labelText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeFg
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CategorySelectCard(
    title: String,
    count: Int,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        ),
        modifier = modifier.defaultMinSize(minHeight = 54.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 15.sp
            )
        }
    }
}

