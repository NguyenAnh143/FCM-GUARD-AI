package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.db.AppDatabase
import com.example.data.model.HeartbeatLogEntity
import com.example.i18n.LanguageManager
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun HistoryLogScreen(
    languageCode: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val logsFlow = remember { db.heartbeatLogDao().getRecentLogs() }
    val logs by logsFlow.collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showClearDialog by remember { mutableStateOf(false) }

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    val filteredLogs = remember(logs, selectedFilter) {
        when (selectedFilter) {
            "SUCCESS" -> logs.filter { it.success }
            "FAILED" -> logs.filter { !it.success }
            "PERIODIC" -> logs.filter { it.eventType.startsWith("PERIODIC") || it.eventType == "SCREEN_ON" }
            else -> logs
        }
    }

    val totalEvents = logs.size
    val successCount = logs.count { it.success }
    val avgLatency = remember(logs) {
        val successLogs = logs.filter { it.success && it.latencyMs > 0 }
        if (successLogs.isNotEmpty()) successLogs.map { it.latencyMs }.average().toInt() else 0
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(str("btn_clear_logs"), fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn xóa toàn bộ lịch sử sự kiện và nhật ký socket không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            db.heartbeatLogDao().clearAll()
                            showClearDialog = false
                        }
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_log_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
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
                        Text(
                            text = str("history_title"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear logs",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            title = str("stat_total"),
                            value = totalEvents.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = str("stat_success"),
                            value = successCount.toString(),
                            color = StatusGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = str("stat_avg_latency"),
                            value = if (avgLatency > 0) "${avgLatency}ms" else "--",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "Tất cả",
                    "SUCCESS" to "Thành công",
                    "FAILED" to "Mất kết nối",
                    "PERIODIC" to "Định kỳ / Mở máy"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Logs List or Empty State
        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = str("empty_logs"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                LogItemRow(log = log)
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LogItemRow(log: HeartbeatLogEntity) {
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
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
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, iconTint) = when {
                !log.success -> Pair(Icons.Default.Error, StatusRed)
                log.eventType == "MANUAL_REPAIR" || log.eventType == "REPAIR_NOW" -> Pair(Icons.Default.Bolt, StatusGreen)
                log.eventType == "NETWORK_RESTORED" -> Pair(Icons.Default.NetworkWifi, MaterialTheme.colorScheme.primary)
                log.eventType == "SCREEN_ON" -> Pair(Icons.Default.Smartphone, MaterialTheme.colorScheme.onSurface)
                else -> Pair(Icons.Default.Schedule, MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val reasonLabel = when (log.eventType) {
                        "MANUAL_REPAIR", "REPAIR_NOW" -> "Sửa thủ công (MCS Heartbeat)"
                        "NETWORK_RESTORED" -> "Mạng phục hồi (Auto Guard)"
                        "SCREEN_ON" -> "Mở màn hình (Screen On)"
                        "SOCKET_TEST" -> "Kiểm tra chẩn đoán"
                        else -> "Dự phòng định kỳ (${log.eventType.substringAfter("PERIODIC_TIMER_", "30M")})"
                    }
                    Text(
                        text = reasonLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (log.latencyMs > 0) "${log.latencyMs}ms" else if (log.success) "OK" else "Lỗi",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.success) StatusGreen else StatusRed
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                val formattedTime = DateFormat.format("HH:mm:ss - dd/MM/yyyy", Date(log.timestamp)).toString()
                Text(
                    text = "$formattedTime • IP: ${log.serverIp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
