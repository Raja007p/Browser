package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TabItem
import com.example.ui.ThemeMode
import com.example.ui.theme.AegisAmber
import com.example.ui.theme.AegisCyan
import com.example.ui.theme.AegisEmerald
import com.example.ui.theme.AegisIncognitoPurple
import com.example.ui.theme.AegisIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenuSheet(
    activeTab: TabItem?,
    batterySaverActive: Boolean,
    themeMode: ThemeMode,
    isVpnActive: Boolean,
    onNewTab: (Boolean) -> Unit,
    onInstallAsWebApp: () -> Unit,
    onOpenWebAppsHub: () -> Unit,
    onOpenExtensions: () -> Unit,
    onOpenVpn: () -> Unit,
    onOpenSync: () -> Unit,
    onToggleBatterySaver: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarksHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val canInstallWebApp = activeTab != null && activeTab.url.isNotBlank() && activeTab.url != "about:blank"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("browser_main_menu_sheet")
        ) {
            // New Tab Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuItem(
                    icon = Icons.Default.Add,
                    title = "New Tab",
                    tint = AegisCyan,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onNewTab(false)
                        onDismiss()
                    }
                )
                MenuItem(
                    icon = Icons.Default.Lock,
                    title = "Private Tab",
                    tint = AegisIncognitoPurple,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onNewTab(true)
                        onDismiss()
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Web Apps Hub & Install action
            if (canInstallWebApp) {
                MenuItem(
                    icon = Icons.AutoMirrored.Filled.AddToHomeScreen,
                    title = "Install as Web App (PWA)",
                    subtitle = "Add '${activeTab?.title?.take(24)}…' to Central Hub",
                    tint = AegisEmerald,
                    onClick = {
                        onInstallAsWebApp()
                        onDismiss()
                    }
                )
            }

            MenuItem(
                icon = Icons.Default.Widgets,
                title = "Web Apps Hub",
                subtitle = "Manage & launch installed standalone web apps",
                tint = AegisEmerald,
                onClick = {
                    onOpenWebAppsHub()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.Extension,
                title = "Extensions & UserScripts",
                subtitle = "Dark Reader, Ad-Skip, Canvas Guard",
                tint = AegisIndigo,
                onClick = {
                    onOpenExtensions()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.VpnLock,
                title = "Secure DNS / VPN Proxy",
                subtitle = if (isVpnActive) "Connected • Swiss Privacy Vault" else "Disabled • Tap to configure tunnel",
                tint = if (isVpnActive) AegisCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    onOpenVpn()
                    onDismiss()
                }
            )

            MenuItem(
                icon = Icons.Default.CloudSync,
                title = "Encrypted Sync & Backup",
                subtitle = "Zero-knowledge AES-256 cross-device sync",
                tint = AegisCyan,
                onClick = {
                    onOpenSync()
                    onDismiss()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Quick Toggles
            // Battery Saver
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleBatterySaver)
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = if (batterySaverActive) AegisAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Battery Saver Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Throttle background JS timers & heavy animations", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = batterySaverActive,
                    onCheckedChange = { onToggleBatterySaver() },
                    colors = SwitchDefaults.colors(checkedThumbColor = AegisAmber, checkedTrackColor = AegisAmber.copy(alpha = 0.3f))
                )
            }

            // Desktop Site toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleDesktopMode)
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DesktopMac,
                    contentDescription = null,
                    tint = if (activeTab?.isDesktopMode == true) AegisCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Desktop Site", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Request full desktop rendering", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = activeTab?.isDesktopMode == true,
                    onCheckedChange = { onToggleDesktopMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = AegisCyan, checkedTrackColor = AegisCyan.copy(alpha = 0.3f))
                )
            }

            // Dark Mode / AMOLED toggle
            MenuItem(
                icon = Icons.Default.DarkMode,
                title = "Theme: ${themeMode.name.replace('_', ' ')}",
                subtitle = "Switch between Dark, AMOLED Black, & Light",
                tint = AegisCyan,
                onClick = onToggleDarkMode
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

            // Bookmarks & History
            MenuItem(
                icon = Icons.Default.Bookmark,
                title = if (canInstallWebApp) "Bookmark this Page" else "Bookmarks & History",
                subtitle = "View saved links and browsing records",
                tint = AegisAmber,
                onClick = {
                    if (canInstallWebApp) onToggleBookmark()
                    onOpenBookmarksHistory()
                    onDismiss()
                }
            )

            // Settings
            MenuItem(
                icon = Icons.Default.Settings,
                title = "Settings",
                subtitle = "Search engine, shield level, cache & privacy rules",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = {
                    onOpenSettings()
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
