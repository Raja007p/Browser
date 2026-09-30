package com.example.data.adblock

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.util.Locale

class AdBlockEngine {

    companion object {
        private val TRACKER_DOMAINS = hashSetOf(
            "google-analytics.com",
            "googletagmanager.com",
            "analytics.google.com",
            "hotjar.com",
            "mixpanel.com",
            "segment.io",
            "segment.com",
            "amplitude.com",
            "scorecardresearch.com",
            "quantserve.com",
            "crazyegg.com",
            "fullstory.com",
            "mouseflow.com",
            "yandex.ru/metrika",
            "mc.yandex.ru",
            "clarity.ms",
            "newrelic.com",
            "appsflyer.com",
            "adjust.com",
            "branch.io",
            "braze.com",
            "sentry.io",
            "bugsnag.com"
        )

        private val AD_DOMAINS = hashSetOf(
            "doubleclick.net",
            "googleadservices.com",
            "googlesyndication.com",
            "pagead2.googlesyndication.com",
            "adservice.google.com",
            "criteo.com",
            "criteo.net",
            "taboola.com",
            "outbrain.com",
            "rubiconproject.com",
            "pubmatic.com",
            "openx.net",
            "amazon-adsystem.com",
            "media.net",
            "adnxs.com",
            "applovin.com",
            "unityads.unity3d.com",
            "ironsrc.com",
            "vungle.com",
            "adcolony.com",
            "chartboost.com",
            "inmobi.com",
            "popads.net",
            "propellerads.com",
            "adsterra.com",
            "zergnet.com",
            "revcontent.com",
            "mgid.com",
            "adroll.com",
            "smartadserver.com",
            "exponential.com",
            "casalemedia.com",
            "bidswitch.net",
            "moatads.com",
            "advertising.com",
            "yieldmo.com",
            "sharethrough.com",
            "sovrn.com"
        )

        private val EMPTY_RESPONSE: WebResourceResponse by lazy {
            WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
        }

        val COSMETIC_FILTER_JS: String = """
            (function() {
                if (window.__aegisCosmeticFiltered) return;
                window.__aegisCosmeticFiltered = true;
                const style = document.createElement('style');
                style.id = 'aegis-adblock-cosmetic';
                style.textContent = `
                    .adsbygoogle, [id^="google_ads"], [id*="-ad-"], [class*="-ad-"],
                    [class*="sponsored"], [id*="sponsored"], [class*="advertisement"],
                    .ad-banner, .advertisement-holder, .ad-container,
                    [aria-label*="advertisement" i], [aria-label*="sponsored" i],
                    .taboola-placeholder, #taboola-below-article-thumbnails,
                    .OUTBRAIN, [data-ad-unit], [data-ad-client] {
                        display: none !important;
                        visibility: hidden !important;
                        height: 0 !important;
                        min-height: 0 !important;
                        pointer-events: none !important;
                    }
                `;
                (document.head || document.documentElement).appendChild(style);
            })();
        """.trimIndent()

        val PRIVACY_HARDENING_JS: String = """
            (function() {
                try {
                    // Global Privacy Control (GPC) & DNT
                    Object.defineProperty(navigator, 'globalPrivacyControl', { value: true, configurable: false });
                    Object.defineProperty(navigator, 'doNotTrack', { value: '1', configurable: false });
                    // Battery API tracking defense
                    if (navigator.getBattery) {
                        navigator.getBattery = () => Promise.resolve({
                            charging: true,
                            chargingTime: 0,
                            dischargingTime: Infinity,
                            level: 1.0,
                            addEventListener: () => {}
                        });
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }

    enum class BlockType {
        NONE, AD, TRACKER
    }

    fun checkUrl(urlStr: String?): BlockType {
        if (urlStr.isNullOrBlank()) return BlockType.NONE
        val host = try {
            Uri.parse(urlStr).host?.lowercase(Locale.ROOT) ?: return BlockType.NONE
        } catch (e: Exception) {
            return BlockType.NONE
        }

        // Check Ad domains
        for (ad in AD_DOMAINS) {
            if (host == ad || host.endsWith(".$ad")) {
                return BlockType.AD
            }
        }

        // Check Tracker domains
        for (tracker in TRACKER_DOMAINS) {
            if (host == tracker || host.endsWith(".$tracker")) {
                return BlockType.TRACKER
            }
        }

        // URL keywords
        val lower = urlStr.lowercase(Locale.ROOT)
        if (lower.contains("/pagead/") || lower.contains("/doubleclick/") || lower.contains("/ad_server/")) {
            return BlockType.AD
        }
        if (lower.contains("/gtag/js") || lower.contains("/analytics.js") || lower.contains("/pixel.gif")) {
            return BlockType.TRACKER
        }

        return BlockType.NONE
    }

    fun getEmptyResponse(): WebResourceResponse = EMPTY_RESPONSE
}
