package com.example.data.sync

import android.util.Base64
import com.example.data.model.Bookmark
import com.example.data.model.BrowserExtension
import com.example.data.model.WebApp
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class SyncPayload(
    val bookmarks: List<Bookmark>,
    val webApps: List<WebApp>,
    val extensions: List<BrowserExtension>,
    val settings: Map<String, String>,
    val exportedAt: Long = System.currentTimeMillis()
)

object EncryptedSyncManager {
    private const val PREFIX = "AEGIS-SYNC-V1:"
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val TAG_LEN_BITS = 128
    private const val ITERATIONS = 10000
    private const val KEY_LEN_BITS = 256

    fun encryptPayload(payload: SyncPayload, passphrase: String): String {
        require(passphrase.length >= 4) { "Passphrase must be at least 4 characters" }

        // 1. Serialize payload to JSON
        val root = JSONObject().apply {
            put("version", 1)
            put("exportedAt", payload.exportedAt)

            // Bookmarks
            val bookmarksArr = JSONArray()
            payload.bookmarks.forEach { b ->
                bookmarksArr.put(JSONObject().apply {
                    put("title", b.title)
                    put("url", b.url)
                    put("createdAt", b.createdAt)
                })
            }
            put("bookmarks", bookmarksArr)

            // Web Apps
            val webAppsArr = JSONArray()
            payload.webApps.forEach { w ->
                webAppsArr.put(JSONObject().apply {
                    put("name", w.name)
                    put("url", w.url)
                    put("iconEmoji", w.iconEmoji)
                    put("themeColor", w.themeColor)
                    put("isStandalone", w.isStandalone)
                })
            }
            put("webApps", webAppsArr)

            // Extensions
            val extArr = JSONArray()
            payload.extensions.forEach { e ->
                extArr.put(JSONObject().apply {
                    put("id", e.id)
                    put("name", e.name)
                    put("description", e.description)
                    put("scriptCode", e.scriptCode)
                    put("enabled", e.enabled)
                    put("version", e.version)
                    put("author", e.author)
                    put("isUserCustom", e.isUserCustom)
                })
            }
            put("extensions", extArr)

            // Settings
            val settingsObj = JSONObject()
            payload.settings.forEach { (k, v) -> settingsObj.put(k, v) }
            put("settings", settingsObj)
        }

        val plainTextBytes = root.toString().toByteArray(Charsets.UTF_8)

        // 2. Generate Salt & IV
        val random = SecureRandom()
        val salt = ByteArray(SALT_LEN)
        random.nextBytes(salt)

        val iv = ByteArray(IV_LEN)
        random.nextBytes(iv)

        // 3. PBKDF2 Key derivation
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LEN_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // 4. Encrypt with AES-GCM
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LEN_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val cipherText = cipher.doFinal(plainTextBytes)

        // 5. Pack Salt + IV + CipherText
        val buffer = ByteBuffer.allocate(SALT_LEN + IV_LEN + cipherText.size)
        buffer.put(salt)
        buffer.put(iv)
        buffer.put(cipherText)

        val encoded = Base64.encodeToString(buffer.array(), Base64.NO_WRAP)
        return PREFIX + encoded
    }

    fun decryptPayload(encryptedBundle: String, passphrase: String): SyncPayload {
        val trimmed = encryptedBundle.trim()
        val base64Data = if (trimmed.startsWith(PREFIX)) {
            trimmed.substring(PREFIX.length)
        } else {
            trimmed
        }

        val rawData = Base64.decode(base64Data, Base64.DEFAULT)
        require(rawData.size > SALT_LEN + IV_LEN) { "Invalid sync payload format" }

        val buffer = ByteBuffer.wrap(rawData)
        val salt = ByteArray(SALT_LEN)
        buffer.get(salt)

        val iv = ByteArray(IV_LEN)
        buffer.get(iv)

        val cipherText = ByteArray(buffer.remaining())
        buffer.get(cipherText)

        // Re-derive key
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LEN_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // Decrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(TAG_LEN_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val plainTextBytes = cipher.doFinal(cipherText)

        val jsonStr = String(plainTextBytes, Charsets.UTF_8)
        val root = JSONObject(jsonStr)

        val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

        // Bookmarks
        val bookmarks = mutableListOf<Bookmark>()
        val bookmarksArr = root.optJSONArray("bookmarks") ?: JSONArray()
        for (i in 0 until bookmarksArr.length()) {
            val obj = bookmarksArr.getJSONObject(i)
            bookmarks.add(
                Bookmark(
                    title = obj.getString("title"),
                    url = obj.getString("url"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Web Apps
        val webApps = mutableListOf<WebApp>()
        val webAppsArr = root.optJSONArray("webApps") ?: JSONArray()
        for (i in 0 until webAppsArr.length()) {
            val obj = webAppsArr.getJSONObject(i)
            webApps.add(
                WebApp(
                    name = obj.getString("name"),
                    url = obj.getString("url"),
                    iconEmoji = obj.optString("iconEmoji", "🌐"),
                    themeColor = obj.optLong("themeColor", 0xFF0284C7),
                    isStandalone = obj.optBoolean("isStandalone", true)
                )
            )
        }

        // Extensions
        val extensions = mutableListOf<BrowserExtension>()
        val extArr = root.optJSONArray("extensions") ?: JSONArray()
        for (i in 0 until extArr.length()) {
            val obj = extArr.getJSONObject(i)
            extensions.add(
                BrowserExtension(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    description = obj.optString("description", ""),
                    scriptCode = obj.getString("scriptCode"),
                    enabled = obj.optBoolean("enabled", true),
                    version = obj.optString("version", "1.0.0"),
                    author = obj.optString("author", "Synced"),
                    isUserCustom = obj.optBoolean("isUserCustom", false)
                )
            )
        }

        // Settings
        val settings = mutableMapOf<String, String>()
        val settingsObj = root.optJSONObject("settings")
        if (settingsObj != null) {
            val keys = settingsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                settings[k] = settingsObj.getString(k)
            }
        }

        return SyncPayload(bookmarks, webApps, extensions, settings, exportedAt)
    }
}
