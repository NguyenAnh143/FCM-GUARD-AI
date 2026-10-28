package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.GmsHeartbeatManager
import com.example.core.HeartbeatResult
import com.example.core.HyperOsUtils
import com.example.data.db.AppDatabase
import com.example.i18n.LanguageManager
import com.example.ui.theme.StatusGreen
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    languageCode: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<HeartbeatResult?>(null) }
    var consoleLogs by remember { mutableStateOf(listOf("Sẵn sàng kiểm tra socket mtalk.google.com:5228...")) }

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    fun runDiagnostics() {
        if (isTesting) return
        isTesting = true
        consoleLogs = listOf(
            "Bắt đầu phân giải DNS mtalk.google.com...",
            "Đang gửi yêu cầu tra cứu bản ghi A/AAAA..."
        )

        scope.launch {
            val db = AppDatabase.getDatabase(context)
            val result = GmsHeartbeatManager.triggerHeartbeat(
                context,
                "SOCKET_TEST",
                db.heartbeatLogDao()
            )
            testResult = result

            val newLogs = mutableListOf<String>()
            newLogs.add("DNS: Phân giải thành công (${result.dnsLatencyMs}ms)")
            newLogs.add("Máy chủ Google Push IP: ${result.serverIp}")
            if (result.success) {
                newLogs.add("TCP Handshake cổng 5228: Thành công (${result.tcpLatencyMs}ms)")
                newLogs.add("Tổng độ trễ kết nối: ${result.latencyMs}ms")
                newLogs.add("Trạng thái: Socket hoạt động bình thường, sẵn sàng nhận FCM push")
            } else {
                newLogs.add("TCP Handshake: Thất bại hoặc timeout")
                newLogs.add("Lỗi chi tiết: ${result.message}")
            }
            consoleLogs = newLogs
            isTesting = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("diagnostics_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Overview Card
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = str("diag_title"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "mtalk.google.com:5228",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = str("diag_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { runDiagnostics() },
                        enabled = !isTesting,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("run_diagnostics_button")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang kiểm tra...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(str("btn_run_test"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shortcut to Google Play Services GcmDiagnostics
                    OutlinedButton(
                        onClick = { HyperOsUtils.openGcmDiagnostics(context) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 56.dp)
                            .testTag("gcm_diag_shortcut_button")
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
                                    text = "Chẩn đoán Dịch vụ Google Play",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Google Play Services (*#*#426#*#*)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Diagnostic Metrics Card
        testResult?.let { res ->
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
                                text = "Kết quả kiểm tra gần nhất",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (res.success) StatusGreen else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (res.success) "KẾT NỐI TỐT" else "MẤT KẾT NỐI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.success) StatusGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        MetricItem(
                            icon = Icons.Default.Dns,
                            title = str("dns_latency"),
                            value = "${res.dnsLatencyMs} ms"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        MetricItem(
                            icon = Icons.Default.Wifi,
                            title = str("tcp_latency"),
                            value = "${res.tcpLatencyMs} ms"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        MetricItem(
                            icon = Icons.Default.NetworkCheck,
                            title = str("resolved_ip"),
                            value = res.serverIp
                        )
                    }
                }
            }
        }

        // Live Console Terminal Output
        item {
            OutlinedCard(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CONSOLE DIAGNOSTICS LOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    consoleLogs.forEach { line ->
                        Text(
                            text = "> $line",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
