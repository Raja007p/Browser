package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.adblock.AdBlockEngine
import com.example.data.model.Bookmark
import com.example.data.model.WebApp
import com.example.data.sync.EncryptedSyncManager
import com.example.data.sync.SyncPayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aegis Browser", appName)
    }

    @Test
    fun `ad block engine detects ad and tracker domains`() {
        val engine = AdBlockEngine()
        val adResult = engine.checkUrl("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js")
        assertEquals(AdBlockEngine.BlockType.AD, adResult)

        val trackerResult = engine.checkUrl("https://analytics.google.com/g/collect")
        assertEquals(AdBlockEngine.BlockType.TRACKER, trackerResult)

        val safeResult = engine.checkUrl("https://en.wikipedia.org/wiki/Chromium")
        assertEquals(AdBlockEngine.BlockType.NONE, safeResult)
    }

    @Test
    fun `encrypted sync encrypts and decrypts payload correctly`() {
        val samplePayload = SyncPayload(
            bookmarks = listOf(Bookmark(title = "DuckDuckGo", url = "https://duckduckgo.com")),
            webApps = listOf(WebApp(name = "Test PWA", url = "https://example.com")),
            extensions = emptyList(),
            settings = mapOf("theme" to "DARK")
        )

        val secretPassphrase = "superSecretKey123!"
        val encryptedBundle = EncryptedSyncManager.encryptPayload(samplePayload, secretPassphrase)
        assertTrue(encryptedBundle.startsWith("AEGIS-SYNC-V1:"))

        val decrypted = EncryptedSyncManager.decryptPayload(encryptedBundle, secretPassphrase)
        assertEquals(1, decrypted.bookmarks.size)
        assertEquals("DuckDuckGo", decrypted.bookmarks[0].title)
        assertEquals("https://duckduckgo.com", decrypted.bookmarks[0].url)
        assertEquals(1, decrypted.webApps.size)
        assertEquals("Test PWA", decrypted.webApps[0].name)
        assertEquals("DARK", decrypted.settings["theme"])
    }
}
