package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "web_apps")
data class WebApp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val iconEmoji: String = "🌐",
    val themeColor: Long = 0xFF0284C7,
    val installedAt: Long = System.currentTimeMillis(),
    val isStandalone: Boolean = true
)

@Entity(tableName = "extensions")
data class BrowserExtension(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val scriptCode: String,
    val enabled: Boolean = true,
    val version: String = "1.0.0",
    val author: String = "Aegis",
    val isUserCustom: Boolean = false
)

data class TabItem(
    val id: String,
    val title: String = "New Tab",
    val url: String = "about:blank",
    val isIncognito: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val blockedAdsCount: Int = 0,
    val blockedTrackersCount: Int = 0,
    val isDesktopMode: Boolean = false,
    val isReaderMode: Boolean = false,
    val isHttps: Boolean = false
)

enum class SearchEngine(val title: String, val searchUrl: String, val homeUrl: String) {
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=", "https://duckduckgo.com"),
    BRAVE("Brave Search", "https://search.brave.com/search?q=", "https://search.brave.com"),
    STARTPAGE("Startpage", "https://www.startpage.com/sp/search?query=", "https://www.startpage.com"),
    SEARX("SearXNG", "https://searx.be/search?q=", "https://searx.be"),
    ECOSIA("Ecosia", "https://www.ecosia.org/search?q=", "https://www.ecosia.org"),
    GOOGLE("Google", "https://www.google.com/search?q=", "https://www.google.com")
}

data class PrivacyMetrics(
    val adsBlocked: Long = 0L,
    val trackersBlocked: Long = 0L,
    val httpsUpgrades: Long = 0L,
    val dataSavedKb: Long = 0L
) {
    val estimatedTimeSavedSeconds: Long
        get() = (adsBlocked * 0.15 + trackersBlocked * 0.08).toLong()
}
