package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrivacyMetrics
import com.example.data.model.SearchEngine
import com.example.data.model.WebApp
import com.example.data.vpn.VpnStatus
import com.example.ui.ShieldLevel
import com.example.ui.theme.AegisAmber
import com.example.ui.theme.AegisCyan
import com.example.ui.theme.AegisEmerald
import com.example.ui.theme.AegisIncognitoPurple
import com.example.ui.theme.AegisIndigo
import com.example.ui.theme.AegisRose

data class SpeedDial(
    val title: String,
    val url: String,
    val emoji: String,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardView(
    isIncognito: Boolean,
    privacyMetrics: PrivacyMetrics,
    searchEngine: SearchEngine,
    shieldLevel: ShieldLevel,
    vpnStatus: VpnStatus,
    batterySaverActive: Boolean,
    batteryLevel: Int,
    webApps: List<WebApp>,
    onSearch: (String) -> Unit,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onToggleVpn: () -> Unit,
    onOpenVpnDialog: () -> Unit,
    onOpenShieldDialog: () -> Unit,
    onOpenSyncDialog: () -> Unit,
    onOpenWebAppsDialog: () -> Unit,
    onOpenExtensionsDialog: () -> Unit,
    onToggleBatterySaver: () -> Unit,
    onLaunchWebApp: (WebApp) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    val defaultSpeedDials = remember {
        listOf(
            SpeedDial("DuckDuckGo", "https://duckduckgo.com", "🦆", Color(0xFFDE5833)),
            SpeedDial("Wikipedia", "https://en.m.wikipedia.org", "📚", Color(0xFF3366CC)),
            SpeedDial("EFF Privacy", "https://www.eff.org", "🛡️", Color(0xFFE11D48)),
            SpeedDial("HackerNews", "https://news.ycombinator.com", "⚡", Color(0xFFFF6600)),
            SpeedDial("Proton", "https://mail.proton.me", "🔒", Color(0xFF6D4AFF)),
            SpeedDial("GitHub", "https://github.com", "🐙", Color(0xFF24292E)),
            SpeedDial("PrivacyGuides", "https://www.privacyguides.org", "🧭", Color(0xFF0D9488)),
            SpeedDial("Brave Search", "https://search.brave.com", "🦁", Color(0xFFFB542B))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Bar: Battery Saver + Stealth Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Incognito / Private status pill
                Surface(
                    color = if (isIncognito) AegisIncognitoPurple.copy(alpha = 0.2f) else AegisCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isIncognito) AegisIncognitoPurple.copy(alpha = 0.5f) else AegisCyan.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isIncognito) AegisIncognitoPurple else AegisEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isIncognito) "Incognito Isolation Active" else "Chromium Core • Safe Browsing",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isIncognito) AegisIncognitoPurple else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Battery Saver toggle chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onToggleBatterySaver)
                        .testTag("battery_saver_toggle_chip"),
                    color = if (batterySaverActive) AegisAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery Saver",
                            tint = if (batterySaverActive) AegisAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (batterySaverActive) "Eco Mode: $batteryLevel%" else "$batteryLevel%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (batterySaverActive) AegisAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Hero Brand Title
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = if (isIncognito) AegisIncognitoPurple.copy(alpha = 0.2f) else AegisCyan.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isIncognito) Icons.Default.Security else Icons.Default.Shield,
                            contentDescription = "Aegis",
                            tint = if (isIncognito) AegisIncognitoPurple else AegisCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isIncognito) "Aegis Stealth" else "Aegis Browser",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Encrypted • Ad-Free • Zero Telemetry",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar & Engine selector
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .testTag("dashboard_search_input"),
                placeholder = {
                    Text("Search with ${searchEngine.title} or enter URL…", fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AegisCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (searchQuery.isNotBlank()) {
                                    val target = if (searchQuery.contains(".") && !searchQuery.contains(" ")) {
                                        if (searchQuery.startsWith("http://") || searchQuery.startsWith("https://")) searchQuery else "https://$searchQuery"
                                    } else {
                                        "${searchEngine.searchUrl}${searchQuery.trim()}"
                                    }
                                    onSearch(target)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AegisCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .padding(end = 4.dp)
                        ) {
                            Text("Go", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AegisCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            val target = if (searchQuery.contains(".") && !searchQuery.contains(" ")) {
                                if (searchQuery.startsWith("http://") || searchQuery.startsWith("https://")) searchQuery else "https://$searchQuery"
                            } else {
                                "${searchEngine.searchUrl}${searchQuery.trim()}"
                            }
                            onSearch(target)
                        }
                    }
                )
            )

            // Search Engine Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SearchEngine.entries.forEach { engine ->
                    FilterChip(
                        selected = searchEngine == engine,
                        onClick = { onSelectSearchEngine(engine) },
                        label = { Text(engine.title, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AegisCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AegisCyan
                        ),
                        modifier = Modifier.testTag("chip_engine_${engine.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // VPN & Secure Proxy Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenVpnDialog)
                    .testTag("vpn_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (vpnStatus.isConnected) AegisIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (vpnStatus.isConnected) AegisIndigo else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (vpnStatus.isConnected) AegisIndigo.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (vpnStatus.isConnected) Icons.Default.VpnLock else Icons.Default.VpnKey,
                            contentDescription = "VPN Proxy",
                            tint = if (vpnStatus.isConnected) AegisCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (vpnStatus.isConnected) "Secure Tunnel Connected" else "Secure DNS / VPN Proxy Off",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (vpnStatus.isConnected) {
                                Text(
                                    text = "${vpnStatus.currentServer?.flag} ${vpnStatus.currentServer?.city}",
                                    fontSize = 12.sp,
                                    color = AegisCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Text(
                            text = if (vpnStatus.isConnected) "${vpnStatus.selectedDnsProvider} • ${vpnStatus.encryptedDataKb} KB encrypted" else "Tap to route through zero-log Swiss or Iceland relay",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onToggleVpn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (vpnStatus.isConnected) AegisRose else AegisIndigo
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("vpn_toggle_button")
                    ) {
                        Text(
                            text = if (vpnStatus.isConnected) "Disconnect" else "Connect",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aegis Privacy Shield Card (Live Metrics)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenShieldDialog)
                    .testTag("privacy_shield_stats_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = AegisCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aegis Shield Protection",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            color = AegisCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = shieldLevel.name,
                                color = AegisCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Metric Counters Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            value = "${privacyMetrics.adsBlocked}",
                            label = "Ads Blocked",
                            color = AegisCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            value = "${privacyMetrics.trackersBlocked}",
                            label = "Trackers",
                            color = AegisEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            value = "${privacyMetrics.dataSavedKb / 1024} MB",
                            label = "Data Saved",
                            color = AegisAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricItem(
                            value = "${privacyMetrics.estimatedTimeSavedSeconds}s",
                            label = "Time Saved",
                            color = AegisIndigo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Speed Dials / Quick Access
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Text(
                    text = "Favorites & Privacy Sites",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    maxItemsInEachRow = 4
                ) {
                    defaultSpeedDials.forEach { dial ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(74.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSearch(dial.url) }
                                .padding(vertical = 4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = dial.color.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, dial.color.copy(alpha = 0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = dial.emoji, fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dial.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Centralized Web Apps Hub Ribbon
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "Web Apps Hub",
                                tint = AegisEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Installed Web Apps (PWAs)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = "Manage (${webApps.size})",
                            fontSize = 12.sp,
                            color = AegisCyan,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onOpenWebAppsDialog)
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        webApps.take(6).forEach { app ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onLaunchWebApp(app) },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = app.iconEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = app.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions: Encrypted Sync & Extensions Manager
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Encrypted Sync
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenSyncDialog)
                        .testTag("action_encrypted_sync"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Encrypted Sync",
                            tint = AegisCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Encrypted Sync",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "AES-GCM backup across devices",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Extensions Manager
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenExtensionsDialog)
                        .testTag("action_extensions_manager"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = "Extensions",
                            tint = AegisIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extensions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Dark Reader, ad-skips & scripts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun MetricItem(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
