package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.pref.AppPreferences
import com.example.i18n.LanguageManager
import com.example.service.FcmGuardService
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.FcmAppsScreen
import com.example.ui.screens.HistoryLogScreen
import com.example.ui.screens.SettingsGuideScreen
import com.example.ui.theme.FcmGuardTheme

data class NavItem(
    val titleKey: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FcmGuardApp(
    appPreferences: AppPreferences
) {
    val themeMode by appPreferences.themeMode.collectAsState()
    val languageCode by appPreferences.languageCode.collectAsState()

    val isAutoGuard by appPreferences.isAutoGuardEnabled.collectAsState()
    val isNotificationEnabled by appPreferences.isPersistentNotificationEnabled.collectAsState()
    val backupInterval by appPreferences.backupIntervalMinutes.collectAsState()
    val lastRepairTimestamp by appPreferences.lastRepairTimestamp.collectAsState()
    val lastLatencyMs by appPreferences.lastLatencyMs.collectAsState()
    val lastSocketStatus by appPreferences.lastSocketStatus.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // If user presses back button on secondary tabs, return to Dashboard (tab 0)
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    val navItems = listOf(
        NavItem("tab_dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
        NavItem("tab_apps", Icons.Filled.Apps, Icons.Outlined.Apps, "nav_apps"),
        NavItem("tab_diagnostics", Icons.Filled.NetworkCheck, Icons.Outlined.NetworkCheck, "nav_diagnostics"),
        NavItem("tab_history", Icons.Filled.History, Icons.Outlined.History, "nav_history"),
        NavItem("tab_settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
    )

    fun str(key: String): String = LanguageManager.getString(key, languageCode)

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    FcmGuardTheme(themeMode = themeMode) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = str(item.titleKey)
                                )
                            },
                            label = { Text(str(item.titleKey), fontSize = 11.sp) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_app_guard_icon_1793044678144),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FCM Guard",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = str(navItems[selectedTab].titleKey),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            navItems.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = str(item.titleKey)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = str(item.titleKey),
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> DashboardScreen(
                            appPreferences = appPreferences,
                            isServiceRunning = FcmGuardService.isRunning,
                            isAutoGuard = isAutoGuard,
                            isNotificationEnabled = isNotificationEnabled,
                            backupInterval = backupInterval,
                            lastRepairTimestamp = lastRepairTimestamp,
                            lastLatencyMs = lastLatencyMs,
                            lastSocketStatus = lastSocketStatus,
                            languageCode = languageCode,
                            onNavigateToTab = { selectedTab = it }
                        )
                        1 -> FcmAppsScreen(
                            languageCode = languageCode
                        )
                        2 -> DiagnosticsScreen(
                            languageCode = languageCode
                        )
                        3 -> HistoryLogScreen(
                            languageCode = languageCode
                        )
                        4 -> SettingsGuideScreen(
                            appPreferences = appPreferences,
                            themeMode = themeMode,
                            languageCode = languageCode
                        )
                    }
                }
            }
        }
    }
}
