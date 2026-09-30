package com.example.ui

import android.app.Application
import android.content.Context
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.adblock.AdBlockEngine
import com.example.data.local.AegisDatabase
import com.example.data.model.Bookmark
import com.example.data.model.BrowserExtension
import com.example.data.model.HistoryItem
import com.example.data.model.PrivacyMetrics
import com.example.data.model.SearchEngine
import com.example.data.model.TabItem
import com.example.data.model.WebApp
import com.example.data.sync.EncryptedSyncManager
import com.example.data.sync.SyncPayload
import com.example.data.vpn.VpnProxyManager
import com.example.data.vpn.VpnServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ThemeMode {
    SYSTEM, DARK, AMOLED_BLACK, LIGHT
}

enum class ShieldLevel {
    AGGRESSIVE, STANDARD, PAUSED
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AegisDatabase.getInstance(application)
    val bookmarkDao = db.bookmarkDao()
    val historyDao = db.historyDao()
    val webAppDao = db.webAppDao()
    val extensionDao = db.extensionDao()

    val adBlockEngine = AdBlockEngine()

    // Database reactive streams
    val bookmarks = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history = historyDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val webApps = webAppDao.getAllWebApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val extensions = extensionDao.getAllExtensions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tabs management
    private val initialTab = TabItem(id = UUID.randomUUID().toString(), url = "about:blank", title = "New Tab")
    private val _tabs = MutableStateFlow(listOf(initialTab))
    val tabs: StateFlow<List<TabItem>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow(initialTab.id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // Privacy Metrics
    private val _privacyMetrics = MutableStateFlow(PrivacyMetrics(adsBlocked = 142L, trackersBlocked = 389L, httpsUpgrades = 64L, dataSavedKb = 2450L))
    val privacyMetrics: StateFlow<PrivacyMetrics> = _privacyMetrics.asStateFlow()

    // Settings
    private val _searchEngine = MutableStateFlow(SearchEngine.DUCKDUCKGO)
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _forceWebDark = MutableStateFlow(true)
    val forceWebDark: StateFlow<Boolean> = _forceWebDark.asStateFlow()

    private val _batterySaverMode = MutableStateFlow(false)
    val batterySaverMode: StateFlow<Boolean> = _batterySaverMode.asStateFlow()

    private val _shieldLevel = MutableStateFlow(ShieldLevel.AGGRESSIVE)
    val shieldLevel: StateFlow<ShieldLevel> = _shieldLevel.asStateFlow()

    private val _httpsOnly = MutableStateFlow(true)
    val httpsOnly: StateFlow<Boolean> = _httpsOnly.asStateFlow()

    private val _clearDataOnExit = MutableStateFlow(false)
    val clearDataOnExit: StateFlow<Boolean> = _clearDataOnExit.asStateFlow()

    // Standalone Web App mode (if launched as dedicated PWA)
    private val _standaloneWebApp = MutableStateFlow<WebApp?>(null)
    val standaloneWebApp: StateFlow<WebApp?> = _standaloneWebApp.asStateFlow()

    // Battery percentage
    private val _batteryLevel = MutableStateFlow(85)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    // VPN state
    val vpnStatus = VpnProxyManager.status

    init {
        updateBatteryState()
    }

    private fun updateBatteryState() {
        try {
            val bm = getApplication<Application>().getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
            _batteryLevel.value = level
            if (level <= 20) {
                _batterySaverMode.value = true
            }
        } catch (_: Exception) {}
    }

    // Active tab accessor
    val activeTab: TabItem?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    fun openNewTab(url: String = "about:blank", isIncognito: Boolean = false) {
        val newTab = TabItem(
            id = UUID.randomUUID().toString(),
            url = url,
            title = if (url == "about:blank") (if (isIncognito) "Private Tab" else "New Tab") else url,
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) {
            // Keep at least one tab
            val replacement = TabItem(id = UUID.randomUUID().toString(), url = "about:blank", title = "New Tab")
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
            return
        }

        val closingIndex = currentTabs.indexOfFirst { it.id == tabId }
        val updatedTabs = currentTabs.filter { it.id != tabId }
        _tabs.value = updatedTabs

        if (_activeTabId.value == tabId) {
            val newIndex = (closingIndex.coerceAtMost(updatedTabs.size - 1)).coerceAtLeast(0)
            _activeTabId.value = updatedTabs[newIndex].id
        }
    }

    fun closeAllTabs(incognitoOnly: Boolean = false) {
        if (incognitoOnly) {
            val remaining = _tabs.value.filter { !it.isIncognito }
            if (remaining.isEmpty()) {
                val replacement = TabItem(id = UUID.randomUUID().toString(), url = "about:blank", title = "New Tab")
                _tabs.value = listOf(replacement)
                _activeTabId.value = replacement.id
            } else {
                _tabs.value = remaining
                if (_tabs.value.none { it.id == _activeTabId.value }) {
                    _activeTabId.value = remaining.first().id
                }
            }
        } else {
            val replacement = TabItem(id = UUID.randomUUID().toString(), url = "about:blank", title = "New Tab")
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
        }
    }

    fun selectTab(tabId: String) {
        _activeTabId.value = tabId
    }

    fun updateActiveTabState(
        url: String? = null,
        title: String? = null,
        progress: Int? = null,
        isLoading: Boolean? = null,
        canGoBack: Boolean? = null,
        canGoForward: Boolean? = null,
        isDesktopMode: Boolean? = null,
        isReaderMode: Boolean? = null
    ) {
        val id = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == id) {
                val newUrl = url ?: tab.url
                val isHttps = newUrl.startsWith("https://")
                tab.copy(
                    url = newUrl,
                    title = title ?: tab.title,
                    progress = progress ?: tab.progress,
                    isLoading = isLoading ?: tab.isLoading,
                    canGoBack = canGoBack ?: tab.canGoBack,
                    canGoForward = canGoForward ?: tab.canGoForward,
                    isDesktopMode = isDesktopMode ?: tab.isDesktopMode,
                    isReaderMode = isReaderMode ?: tab.isReaderMode,
                    isHttps = isHttps
                )
            } else tab
        }

        // Record history if valid non-incognito url
        if (url != null && url != "about:blank" && !url.startsWith("data:") && activeTab?.isIncognito == false) {
            viewModelScope.launch {
                historyDao.insertHistory(
                    HistoryItem(
                        title = title ?: url,
                        url = url
                    )
                )
            }
        }
    }

    fun recordBlock(type: AdBlockEngine.BlockType) {
        if (type == AdBlockEngine.BlockType.NONE) return
        val currentTabId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentTabId) {
                when (type) {
                    AdBlockEngine.BlockType.AD -> tab.copy(blockedAdsCount = tab.blockedAdsCount + 1)
                    AdBlockEngine.BlockType.TRACKER -> tab.copy(blockedTrackersCount = tab.blockedTrackersCount + 1)
                    else -> tab
                }
            } else tab
        }

        _privacyMetrics.value = when (type) {
            AdBlockEngine.BlockType.AD -> _privacyMetrics.value.copy(
                adsBlocked = _privacyMetrics.value.adsBlocked + 1,
                dataSavedKb = _privacyMetrics.value.dataSavedKb + 45
            )
            AdBlockEngine.BlockType.TRACKER -> _privacyMetrics.value.copy(
                trackersBlocked = _privacyMetrics.value.trackersBlocked + 1,
                dataSavedKb = _privacyMetrics.value.dataSavedKb + 12
            )
            else -> _privacyMetrics.value
        }

        VpnProxyManager.addEncryptedTraffic(3500)
    }

    fun recordHttpsUpgrade() {
        _privacyMetrics.value = _privacyMetrics.value.copy(
            httpsUpgrades = _privacyMetrics.value.httpsUpgrades + 1
        )
    }

    // Toggle Desktop Mode on active tab
    fun toggleDesktopMode() {
        val tab = activeTab ?: return
        updateActiveTabState(isDesktopMode = !tab.isDesktopMode)
    }

    // Toggle Reader Mode on active tab
    fun toggleReaderMode() {
        val tab = activeTab ?: return
        updateActiveTabState(isReaderMode = !tab.isReaderMode)
    }

    // Bookmarks
    fun toggleBookmark(title: String, url: String) {
        viewModelScope.launch {
            val existing = bookmarkDao.getBookmarkByUrl(url)
            if (existing != null) {
                bookmarkDao.deleteBookmark(existing)
            } else {
                bookmarkDao.insertBookmark(Bookmark(title = title, url = url))
            }
        }
    }

    fun deleteHistoryItem(item: HistoryItem) {
        viewModelScope.launch {
            historyDao.deleteHistory(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyDao.clearAll()
        }
    }

    // Web Apps Hub
    fun installCurrentAsWebApp(name: String, url: String, emoji: String = "🌐") {
        viewModelScope.launch {
            webAppDao.insertWebApp(
                WebApp(
                    name = name.ifBlank { "Web App" },
                    url = url,
                    iconEmoji = emoji,
                    themeColor = 0xFF0284C7,
                    isStandalone = true
                )
            )
        }
    }

    fun deleteWebApp(webApp: WebApp) {
        viewModelScope.launch {
            webAppDao.deleteWebApp(webApp)
            if (_standaloneWebApp.value?.id == webApp.id) {
                _standaloneWebApp.value = null
            }
        }
    }

    fun launchStandaloneWebApp(webApp: WebApp?) {
        _standaloneWebApp.value = webApp
        if (webApp != null) {
            openNewTab(url = webApp.url, isIncognito = false)
        }
    }

    // Extensions
    fun toggleExtension(extension: BrowserExtension) {
        viewModelScope.launch {
            extensionDao.updateExtension(extension.copy(enabled = !extension.enabled))
        }
    }

    fun addCustomExtension(name: String, description: String, scriptCode: String) {
        viewModelScope.launch {
            val ext = BrowserExtension(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = name,
                description = description,
                scriptCode = scriptCode,
                enabled = true,
                version = "1.0.0",
                author = "User",
                isUserCustom = true
            )
            extensionDao.insertExtension(ext)
        }
    }

    fun deleteExtension(extension: BrowserExtension) {
        viewModelScope.launch {
            extensionDao.deleteExtension(extension)
        }
    }

    // Settings modifiers
    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleForceWebDark() {
        _forceWebDark.value = !_forceWebDark.value
    }

    fun toggleBatterySaver() {
        _batterySaverMode.value = !_batterySaverMode.value
    }

    fun setShieldLevel(level: ShieldLevel) {
        _shieldLevel.value = level
    }

    fun toggleHttpsOnly() {
        _httpsOnly.value = !_httpsOnly.value
    }

    fun toggleClearDataOnExit() {
        _clearDataOnExit.value = !_clearDataOnExit.value
    }

    // VPN Controls
    fun toggleVpn() {
        VpnProxyManager.toggleConnection()
    }

    fun selectVpnServer(server: VpnServer) {
        VpnProxyManager.selectServer(server)
    }

    fun selectDns(dns: String) {
        VpnProxyManager.selectDns(dns)
    }

    // Encrypted Sync & Backup
    suspend fun createEncryptedBackup(passphrase: String): String {
        val currentBookmarks = bookmarkDao.getAllBookmarks().firstOrNull() ?: emptyList()
        val currentWebApps = webAppDao.getAllWebApps().firstOrNull() ?: emptyList()
        val currentExtensions = extensionDao.getAllExtensions().firstOrNull() ?: emptyList()

        val settingsMap = mapOf(
            "searchEngine" to _searchEngine.value.name,
            "themeMode" to _themeMode.value.name,
            "forceWebDark" to _forceWebDark.value.toString(),
            "batterySaver" to _batterySaverMode.value.toString(),
            "shieldLevel" to _shieldLevel.value.name,
            "httpsOnly" to _httpsOnly.value.toString(),
            "clearOnExit" to _clearDataOnExit.value.toString(),
            "dnsProvider" to VpnProxyManager.status.value.selectedDnsProvider
        )

        val payload = SyncPayload(
            bookmarks = currentBookmarks,
            webApps = currentWebApps,
            extensions = currentExtensions,
            settings = settingsMap
        )

        return EncryptedSyncManager.encryptPayload(payload, passphrase)
    }

    suspend fun restoreEncryptedBackup(bundleString: String, passphrase: String): Result<String> {
        return try {
            val payload = EncryptedSyncManager.decryptPayload(bundleString, passphrase)

            // Restore bookmarks
            for (b in payload.bookmarks) {
                bookmarkDao.insertBookmark(b)
            }

            // Restore web apps
            for (w in payload.webApps) {
                webAppDao.insertWebApp(w)
            }

            // Restore extensions
            for (e in payload.extensions) {
                extensionDao.insertExtension(e)
            }

            // Restore settings
            payload.settings["searchEngine"]?.let { engineStr ->
                try { _searchEngine.value = SearchEngine.valueOf(engineStr) } catch (_: Exception) {}
            }
            payload.settings["themeMode"]?.let { themeStr ->
                try { _themeMode.value = ThemeMode.valueOf(themeStr) } catch (_: Exception) {}
            }
            payload.settings["forceWebDark"]?.let { darkStr -> _forceWebDark.value = darkStr.toBoolean() }
            payload.settings["batterySaver"]?.let { saverStr -> _batterySaverMode.value = saverStr.toBoolean() }
            payload.settings["shieldLevel"]?.let { shieldStr ->
                try { _shieldLevel.value = ShieldLevel.valueOf(shieldStr) } catch (_: Exception) {}
            }
            payload.settings["httpsOnly"]?.let { httpsStr -> _httpsOnly.value = httpsStr.toBoolean() }
            payload.settings["dnsProvider"]?.let { dnsStr -> VpnProxyManager.selectDns(dnsStr) }

            Result.success("Restored ${payload.bookmarks.size} bookmarks, ${payload.webApps.size} web apps, and ${payload.extensions.size} extensions seamlessly!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
