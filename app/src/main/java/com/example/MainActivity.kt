package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.adblock.AdBlockEngine
import com.example.data.model.WebApp
import com.example.ui.BrowserViewModel
import com.example.ui.ThemeMode
import com.example.ui.components.AegisAddressBar
import com.example.ui.components.AegisWebViewContainer
import com.example.ui.components.DashboardView
import com.example.ui.dialogs.BookmarksHistoryDialog
import com.example.ui.dialogs.BrowserMenuSheet
import com.example.ui.dialogs.EncryptedSyncDialog
import com.example.ui.dialogs.ExtensionsDialog
import com.example.ui.dialogs.PrivacyShieldDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.TabSwitcherSheet
import com.example.ui.dialogs.VpnProxyDialog
import com.example.ui.dialogs.WebAppsHubDialog
import com.example.ui.theme.AegisCyan
import com.example.ui.theme.AegisEmerald
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var activeWebViewInstance: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: BrowserViewModel = viewModel()

            // Observe intent incoming URL
            LaunchedEffect(intent) {
                intent?.data?.toString()?.let { incomingUrl ->
                    if (incomingUrl.isNotBlank()) {
                        viewModel.openNewTab(incomingUrl)
                    }
                }
            }

            val tabs by viewModel.tabs.collectAsStateWithLifecycle()
            val activeTabId by viewModel.activeTabId.collectAsStateWithLifecycle()
            val activeTab = tabs.find { it.id == activeTabId }

            val privacyMetrics by viewModel.privacyMetrics.collectAsStateWithLifecycle()
            val searchEngine by viewModel.searchEngine.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val forceWebDark by viewModel.forceWebDark.collectAsStateWithLifecycle()
            val batterySaverMode by viewModel.batterySaverMode.collectAsStateWithLifecycle()
            val shieldLevel by viewModel.shieldLevel.collectAsStateWithLifecycle()
            val httpsOnly by viewModel.httpsOnly.collectAsStateWithLifecycle()
            val clearDataOnExit by viewModel.clearDataOnExit.collectAsStateWithLifecycle()
            val standaloneWebApp by viewModel.standaloneWebApp.collectAsStateWithLifecycle()
            val batteryLevel by viewModel.batteryLevel.collectAsStateWithLifecycle()
            val vpnStatus by viewModel.vpnStatus.collectAsStateWithLifecycle()

            val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
            val history by viewModel.history.collectAsStateWithLifecycle()
            val webApps by viewModel.webApps.collectAsStateWithLifecycle()
            val extensions by viewModel.extensions.collectAsStateWithLifecycle()

            // Dialog Visibility Flags
            var showShieldDialog by remember { mutableStateOf(false) }
            var showTabSwitcher by remember { mutableStateOf(false) }
            var showMainMenu by remember { mutableStateOf(false) }
            var showWebAppsHub by remember { mutableStateOf(false) }
            var showExtensions by remember { mutableStateOf(false) }
            var showVpnProxy by remember { mutableStateOf(false) }
            var showEncryptedSync by remember { mutableStateOf(false) }
            var showBookmarksHistory by remember { mutableStateOf(false) }
            var showSettings by remember { mutableStateOf(false) }
            var showInstallWebAppDialog by remember { mutableStateOf(false) }

            MyApplicationTheme(
                themeMode = themeMode,
                isIncognito = activeTab?.isIncognito == true
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // Status bar top spacing
                        Spacer(
                            modifier = Modifier.height(
                                WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                            )
                        )

                        // Standalone Web App Header (if PWA launched in standalone window)
                        if (standaloneWebApp != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = standaloneWebApp?.iconEmoji ?: "🌐", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = standaloneWebApp?.name ?: "Web App",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { activeWebViewInstance?.reload() },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.launchStandaloneWebApp(null) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Exit standalone", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        } else {
                            // Standard Address & Command Bar
                            AegisAddressBar(
                                activeTab = activeTab,
                                totalTabsCount = tabs.size,
                                isVpnActive = vpnStatus.isConnected,
                                onNavigate = { input ->
                                    val target = if (input.contains(".") && !input.contains(" ")) {
                                        if (input.startsWith("http://") || input.startsWith("https://")) input else "https://$input"
                                    } else {
                                        "${searchEngine.searchUrl}${input.trim()}"
                                    }
                                    viewModel.updateActiveTabState(url = target)
                                },
                                onRefresh = { activeWebViewInstance?.reload() },
                                onStop = { activeWebViewInstance?.stopLoading() },
                                onGoBack = {
                                    if (activeWebViewInstance?.canGoBack() == true) {
                                        activeWebViewInstance?.goBack()
                                    }
                                },
                                onGoForward = {
                                    if (activeWebViewInstance?.canGoForward() == true) {
                                        activeWebViewInstance?.goForward()
                                    }
                                },
                                onShieldClick = { showShieldDialog = true },
                                onTabSwitcherClick = { showTabSwitcher = true },
                                onMenuClick = { showMainMenu = true },
                                onHomeClick = { viewModel.updateActiveTabState(url = "about:blank", title = "New Tab") }
                            )
                        }

                        // Main Content Area
                        Box(modifier = Modifier.weight(1f)) {
                            if (activeTab == null || activeTab.url == "about:blank") {
                                // Intuitive Privacy Dashboard (New Tab)
                                DashboardView(
                                    isIncognito = activeTab?.isIncognito == true,
                                    privacyMetrics = privacyMetrics,
                                    searchEngine = searchEngine,
                                    shieldLevel = shieldLevel,
                                    vpnStatus = vpnStatus,
                                    batterySaverActive = batterySaverMode,
                                    batteryLevel = batteryLevel,
                                    webApps = webApps,
                                    onSearch = { targetUrl ->
                                        viewModel.updateActiveTabState(url = targetUrl)
                                    },
                                    onSelectSearchEngine = { engine ->
                                        viewModel.setSearchEngine(engine)
                                    },
                                    onToggleVpn = { viewModel.toggleVpn() },
                                    onOpenVpnDialog = { showVpnProxy = true },
                                    onOpenShieldDialog = { showShieldDialog = true },
                                    onOpenSyncDialog = { showEncryptedSync = true },
                                    onOpenWebAppsDialog = { showWebAppsHub = true },
                                    onOpenExtensionsDialog = { showExtensions = true },
                                    onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                                    onLaunchWebApp = { app ->
                                        viewModel.launchStandaloneWebApp(app)
                                    }
                                )
                            } else {
                                // Chromium WebView
                                AegisWebViewContainer(
                                    tab = activeTab,
                                    adBlockEngine = viewModel.adBlockEngine,
                                    enabledExtensions = extensions.filter { it.enabled },
                                    batterySaverMode = batterySaverMode,
                                    forceWebDark = forceWebDark,
                                    httpsOnly = httpsOnly,
                                    onUpdateState = { url, title, progress, isLoading, canGoBack, canGoForward ->
                                        viewModel.updateActiveTabState(
                                            url = url,
                                            title = title,
                                            progress = progress,
                                            isLoading = isLoading,
                                            canGoBack = canGoBack,
                                            canGoForward = canGoForward
                                        )
                                    },
                                    onRecordBlock = { type ->
                                        viewModel.recordBlock(type)
                                    },
                                    onRecordHttpsUpgrade = {
                                        viewModel.recordHttpsUpgrade()
                                    },
                                    onWebViewCreated = { webView ->
                                        activeWebViewInstance = webView
                                    }
                                )
                            }
                        }
                    }
                }

                // DIALOGS & MODAL SHEETS

                // 1. Privacy Shield
                if (showShieldDialog) {
                    PrivacyShieldDialog(
                        tab = activeTab,
                        shieldLevel = shieldLevel,
                        onShieldLevelChange = { viewModel.setShieldLevel(it) },
                        onClearSiteData = {
                            activeWebViewInstance?.clearCache(true)
                            activeWebViewInstance?.clearFormData()
                            Toast.makeText(this@MainActivity, "Site cookies & cache cleared", Toast.LENGTH_SHORT).show()
                        },
                        onDismiss = { showShieldDialog = false }
                    )
                }

                // 2. Tab Switcher
                if (showTabSwitcher) {
                    TabSwitcherSheet(
                        tabs = tabs,
                        activeTabId = activeTabId,
                        onSelectTab = { viewModel.selectTab(it) },
                        onCloseTab = { viewModel.closeTab(it) },
                        onCloseAll = { incognitoOnly -> viewModel.closeAllTabs(incognitoOnly) },
                        onNewTab = { isIncognito -> viewModel.openNewTab(isIncognito = isIncognito) },
                        onDismiss = { showTabSwitcher = false }
                    )
                }

                // 3. Centralized Main Menu
                if (showMainMenu) {
                    BrowserMenuSheet(
                        activeTab = activeTab,
                        batterySaverActive = batterySaverMode,
                        themeMode = themeMode,
                        isVpnActive = vpnStatus.isConnected,
                        onNewTab = { isIncognito -> viewModel.openNewTab(isIncognito = isIncognito) },
                        onInstallAsWebApp = { showInstallWebAppDialog = true },
                        onOpenWebAppsHub = { showWebAppsHub = true },
                        onOpenExtensions = { showExtensions = true },
                        onOpenVpn = { showVpnProxy = true },
                        onOpenSync = { showEncryptedSync = true },
                        onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                        onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                        onToggleDarkMode = {
                            val nextMode = when (themeMode) {
                                ThemeMode.DARK -> ThemeMode.AMOLED_BLACK
                                ThemeMode.AMOLED_BLACK -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.SYSTEM -> ThemeMode.DARK
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        onToggleBookmark = {
                            activeTab?.let { tab ->
                                viewModel.toggleBookmark(tab.title, tab.url)
                                Toast.makeText(this@MainActivity, "Bookmark toggled", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenBookmarksHistory = { showBookmarksHistory = true },
                        onOpenSettings = { showSettings = true },
                        onDismiss = { showMainMenu = false }
                    )
                }

                // 4. Web Apps Hub (PWA Manager)
                if (showWebAppsHub) {
                    WebAppsHubDialog(
                        webApps = webApps,
                        onLaunchApp = { app -> viewModel.launchStandaloneWebApp(app) },
                        onDeleteApp = { app -> viewModel.deleteWebApp(app) },
                        onInstallNewApp = { name, url, emoji ->
                            viewModel.installCurrentAsWebApp(name, url, emoji)
                        },
                        onDismiss = { showWebAppsHub = false }
                    )
                }

                // 5. Extensions Manager
                if (showExtensions) {
                    ExtensionsDialog(
                        extensions = extensions,
                        onToggleExtension = { viewModel.toggleExtension(it) },
                        onDeleteExtension = { viewModel.deleteExtension(it) },
                        onAddExtension = { name, desc, code ->
                            viewModel.addCustomExtension(name, desc, code)
                        },
                        onDismiss = { showExtensions = false }
                    )
                }

                // 6. VPN & Secure DNS Proxy
                if (showVpnProxy) {
                    VpnProxyDialog(
                        vpnStatus = vpnStatus,
                        onToggleVpn = { viewModel.toggleVpn() },
                        onSelectServer = { viewModel.selectVpnServer(it) },
                        onSelectDns = { viewModel.selectDns(it) },
                        onDismiss = { showVpnProxy = false }
                    )
                }

                // 7. Encrypted Cross-Device Sync
                if (showEncryptedSync) {
                    EncryptedSyncDialog(
                        onCreateBackup = { passphrase -> viewModel.createEncryptedBackup(passphrase) },
                        onRestoreBackup = { bundle, passphrase -> viewModel.restoreEncryptedBackup(bundle, passphrase) },
                        onDismiss = { showEncryptedSync = false }
                    )
                }

                // 8. Bookmarks & History
                if (showBookmarksHistory) {
                    BookmarksHistoryDialog(
                        bookmarks = bookmarks,
                        history = history,
                        onOpenUrl = { url -> viewModel.openNewTab(url) },
                        onDeleteBookmark = { bookmark -> viewModel.toggleBookmark(bookmark.title, bookmark.url) },
                        onDeleteHistoryItem = { item ->
                            viewModel.deleteHistoryItem(item)
                        },
                        onClearHistory = {
                            viewModel.clearHistory()
                        },
                        onDismiss = { showBookmarksHistory = false }
                    )
                }

                // 9. Settings
                if (showSettings) {
                    SettingsDialog(
                        currentSearchEngine = searchEngine,
                        themeMode = themeMode,
                        forceWebDark = forceWebDark,
                        batterySaverMode = batterySaverMode,
                        httpsOnly = httpsOnly,
                        clearDataOnExit = clearDataOnExit,
                        onSearchEngineChange = { viewModel.setSearchEngine(it) },
                        onThemeModeChange = { viewModel.setThemeMode(it) },
                        onToggleForceWebDark = { viewModel.toggleForceWebDark() },
                        onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                        onToggleHttpsOnly = { viewModel.toggleHttpsOnly() },
                        onToggleClearDataOnExit = { viewModel.toggleClearDataOnExit() },
                        onDismiss = { showSettings = false }
                    )
                }

                // 10. Install Current Web App Dialog
                if (showInstallWebAppDialog && activeTab != null) {
                    var appTitle by remember { mutableStateOf(activeTab.title.take(30)) }
                    var appEmoji by remember { mutableStateOf("🌐") }

                    AlertDialog(
                        onDismissRequest = { showInstallWebAppDialog = false },
                        title = { Text("Install Web App", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text("Add '${activeTab.url}' into your centralized Web Apps Hub with standalone window mode.", fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = appTitle,
                                    onValueChange = { appTitle = it },
                                    label = { Text("App Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.installCurrentAsWebApp(appTitle, activeTab.url, appEmoji)
                                    showInstallWebAppDialog = false
                                    Toast.makeText(this@MainActivity, "Installed to Web Apps Hub!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AegisEmerald)
                            ) {
                                Text("Install", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showInstallWebAppDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        activeWebViewInstance?.destroy()
        activeWebViewInstance = null
    }
}
