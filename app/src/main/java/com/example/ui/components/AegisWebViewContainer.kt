package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.adblock.AdBlockEngine
import com.example.data.model.BrowserExtension
import com.example.data.model.TabItem

private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 AegisBrowser/1.0"
private const val MOBILE_PRIVACY_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AegisWebViewContainer(
    tab: TabItem,
    adBlockEngine: AdBlockEngine,
    enabledExtensions: List<BrowserExtension>,
    batterySaverMode: Boolean,
    forceWebDark: Boolean,
    httpsOnly: Boolean,
    onUpdateState: (
        url: String?,
        title: String?,
        progress: Int?,
        isLoading: Boolean?,
        canGoBack: Boolean?,
        canGoForward: Boolean?
    ) -> Unit,
    onRecordBlock: (AdBlockEngine.BlockType) -> Unit,
    onRecordHttpsUpgrade: () -> Unit,
    onWebViewCreated: (WebView) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var webViewRef: WebView? = remember { null }

    BackHandler(enabled = tab.canGoBack) {
        webViewRef?.let {
            if (it.canGoBack()) {
                it.goBack()
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Privacy & Performance settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    mediaPlaybackRequiresUserGesture = batterySaverMode
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    cacheMode = if (batterySaverMode) WebSettings.LOAD_CACHE_ELSE_NETWORK else WebSettings.LOAD_DEFAULT

                    // Incognito isolation
                    if (tab.isIncognito) {
                        saveFormData = false
                        CookieManager.getInstance().setAcceptCookie(false)
                    } else {
                        CookieManager.getInstance().setAcceptCookie(true)
                    }

                    userAgentString = if (tab.isDesktopMode) DESKTOP_USER_AGENT else MOBILE_PRIVACY_USER_AGENT
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val requestUrl = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

                        // HTTPS Upgrade
                        if (httpsOnly && requestUrl.startsWith("http://")) {
                            onRecordHttpsUpgrade()
                        }

                        // Ad & Tracker Blocking
                        val blockType = adBlockEngine.checkUrl(requestUrl)
                        if (blockType != AdBlockEngine.BlockType.NONE) {
                            view?.post { onRecordBlock(blockType) }
                            return adBlockEngine.getEmptyResponse()
                        }

                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onUpdateState(
                            url,
                            view?.title,
                            10,
                            true,
                            view?.canGoBack(),
                            view?.canGoForward()
                        )
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onUpdateState(
                            url,
                            view?.title,
                            100,
                            false,
                            view?.canGoBack(),
                            view?.canGoForward()
                        )

                        // Inject Cosmetic Ad-blocking Filter
                        view?.evaluateJavascript(AdBlockEngine.COSMETIC_FILTER_JS, null)

                        // Inject Anti-Fingerprinting Shield
                        view?.evaluateJavascript(AdBlockEngine.PRIVACY_HARDENING_JS, null)

                        // Inject Force Web Dark mode if active
                        if (forceWebDark) {
                            view?.evaluateJavascript(
                                """
                                (function() {
                                    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) return;
                                    const meta = document.createElement('meta');
                                    meta.name = 'color-scheme';
                                    meta.content = 'dark';
                                    (document.head || document.documentElement).appendChild(meta);
                                })();
                                """.trimIndent(),
                                null
                            )
                        }

                        // Inject Enabled Browser Extensions
                        enabledExtensions.forEach { ext ->
                            if (ext.enabled && ext.scriptCode.isNotBlank()) {
                                view?.evaluateJavascript(ext.scriptCode, null)
                            }
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onUpdateState(
                            view?.url,
                            view?.title,
                            newProgress,
                            newProgress < 100,
                            view?.canGoBack(),
                            view?.canGoForward()
                        )
                    }

                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        onUpdateState(
                            view?.url,
                            title,
                            null,
                            null,
                            view?.canGoBack(),
                            view?.canGoForward()
                        )
                    }
                }

                webViewRef = this
                onWebViewCreated(this)

                if (tab.url.isNotBlank() && tab.url != "about:blank") {
                    loadUrl(tab.url)
                }
            }
        },
        update = { webView ->
            webViewRef = webView
            // Update User Agent if desktop mode changed
            val targetUa = if (tab.isDesktopMode) DESKTOP_USER_AGENT else MOBILE_PRIVACY_USER_AGENT
            if (webView.settings.userAgentString != targetUa) {
                webView.settings.userAgentString = targetUa
                webView.reload()
            }

            // If tab url changed externally and doesn't match current webView url
            if (tab.url.isNotBlank() && tab.url != "about:blank" && webView.url != tab.url) {
                webView.loadUrl(tab.url)
            }
        }
    )

    DisposableEffect(tab.id) {
        onDispose {
            if (tab.isIncognito) {
                webViewRef?.clearCache(true)
                webViewRef?.clearFormData()
                webViewRef?.clearHistory()
                CookieManager.getInstance().removeAllCookies(null)
            }
        }
    }
}
