package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Bookmark
import com.example.data.model.BrowserExtension
import com.example.data.model.HistoryItem
import com.example.data.model.WebApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Bookmark::class, HistoryItem::class, WebApp::class, BrowserExtension::class],
    version = 1,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun webAppDao(): WebAppDao
    abstract fun extensionDao(): ExtensionDao

    companion object {
        @Volatile
        private var INSTANCE: AegisDatabase? = null

        fun getInstance(context: Context): AegisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AegisDatabase::class.java,
                    "aegis_browser.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDefaults(getInstance(context))
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDefaults(database: AegisDatabase) {
            val extensionDao = database.extensionDao()
            val webAppDao = database.webAppDao()
            val bookmarkDao = database.bookmarkDao()

            // Default privacy extensions
            val defaultExtensions = listOf(
                BrowserExtension(
                    id = "dark_reader",
                    name = "Dark Reader Pro",
                    description = "Inverts light web backgrounds and enhances text contrast for comfortable night reading.",
                    scriptCode = """
                        (function() {
                            if (window.__aegisDarkInjected) return;
                            window.__aegisDarkInjected = true;
                            const style = document.createElement('style');
                            style.id = 'aegis-dark-reader-style';
                            style.textContent = `
                                html {
                                    filter: invert(90%) hue-rotate(180deg) !important;
                                    background-color: #121212 !important;
                                }
                                img, video, canvas, svg, [style*="background-image"], iframe {
                                    filter: invert(111%) hue-rotate(180deg) !important;
                                }
                            `;
                            (document.head || document.documentElement).appendChild(style);
                        })();
                    """.trimIndent(),
                    enabled = true,
                    version = "2.4.1",
                    author = "Aegis Privacy Labs"
                ),
                BrowserExtension(
                    id = "cookie_banner_killer",
                    name = "Cookie Notice Obliterator",
                    description = "Automatically removes nagging GDPR, CCPA, and cookie consent overlays from pages.",
                    scriptCode = """
                        (function() {
                            const selectors = [
                                '#onetrust-consent-sdk', '.cookie-banner', '#cookie-banner',
                                '[aria-label*="cookie" i]', '[id*="cookie-notice" i]',
                                '.cc-banner', '.qc-cmp2-container', '#cmpbox'
                            ];
                            function clean() {
                                selectors.forEach(sel => {
                                    document.querySelectorAll(sel).forEach(el => el.remove());
                                });
                                document.body.style.overflow = 'auto';
                            }
                            if (document.readyState === 'loading') {
                                document.addEventListener('DOMContentLoaded', clean);
                            } else {
                                clean();
                            }
                            setInterval(clean, 1500);
                        })();
                    """.trimIndent(),
                    enabled = true,
                    version = "1.8.0",
                    author = "Aegis Open Web"
                ),
                BrowserExtension(
                    id = "fingerprint_defender",
                    name = "Anti-Canvas Fingerprinter",
                    description = "Injects microscopic imperceptible noise into HTML5 Canvas to prevent cross-site identity tracking.",
                    scriptCode = """
                        (function() {
                            const origToDataURL = HTMLCanvasElement.prototype.toDataURL;
                            HTMLCanvasElement.prototype.toDataURL = function() {
                                const ctx = this.getContext('2d');
                                if (ctx) {
                                    const imgData = ctx.getImageData(0, 0, Math.min(this.width, 10), Math.min(this.height, 10));
                                    for (let i = 0; i < imgData.data.length; i += 4) {
                                        imgData.data[i] = (imgData.data[i] + (Math.random() > 0.5 ? 1 : 0)) % 256;
                                    }
                                }
                                return origToDataURL.apply(this, arguments);
                            };
                        })();
                    """.trimIndent(),
                    enabled = true,
                    version = "3.1.0",
                    author = "Aegis Security"
                ),
                BrowserExtension(
                    id = "sponsor_block_lite",
                    name = "Video Ad-Skip & Clean Overlay",
                    description = "Auto-skips skippable ads and removes floating sponsor overlays on web players.",
                    scriptCode = """
                        (function() {
                            setInterval(() => {
                                const skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-ad-skip-button-modern, .videoAdUiSkipButton');
                                if (skipBtn) {
                                    skipBtn.click();
                                }
                                const adOverlay = document.querySelector('.ytp-ad-overlay-container');
                                if (adOverlay) adOverlay.style.display = 'none';
                            }, 1000);
                        })();
                    """.trimIndent(),
                    enabled = true,
                    version = "1.2.5",
                    author = "Aegis Community"
                )
            )
            extensionDao.insertAll(defaultExtensions)

            // Default Web Apps in Hub
            val defaultWebApps = listOf(
                WebApp(name = "DuckDuckGo", url = "https://duckduckgo.com", iconEmoji = "🦆", themeColor = 0xFFDE5833),
                WebApp(name = "Wikipedia", url = "https://en.m.wikipedia.org", iconEmoji = "📚", themeColor = 0xFF3366CC),
                WebApp(name = "Hacker News", url = "https://news.ycombinator.com", iconEmoji = "⚡", themeColor = 0xFFFF6600),
                WebApp(name = "Proton Mail", url = "https://mail.proton.me", iconEmoji = "🔒", themeColor = 0xFF6D4AFF),
                WebApp(name = "GitHub", url = "https://github.com", iconEmoji = "🐙", themeColor = 0xFF24292E),
                WebApp(name = "Weather Earth", url = "https://earth.nullschool.net", iconEmoji = "🌍", themeColor = 0xFF0D9488)
            )
            defaultWebApps.forEach { webAppDao.insertWebApp(it) }

            // Default Bookmarks
            bookmarkDao.insertBookmark(Bookmark(title = "DuckDuckGo Privacy Search", url = "https://duckduckgo.com"))
            bookmarkDao.insertBookmark(Bookmark(title = "Electronic Frontier Foundation", url = "https://www.eff.org"))
            bookmarkDao.insertBookmark(Bookmark(title = "Privacy Guides", url = "https://www.privacyguides.org"))
        }
    }
}
