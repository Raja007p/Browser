package com.example.data.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VpnServer(
    val id: String,
    val name: String,
    val flag: String,
    val country: String,
    val city: String,
    val pingMs: Int,
    val ipMask: String,
    val features: List<String>
)

data class VpnStatus(
    val isConnected: Boolean = false,
    val currentServer: VpnServer? = null,
    val selectedDnsProvider: String = "Cloudflare (1.1.1.1 - Privacy+)",
    val connectionType: String = "WireGuard® / Encrypted DoH",
    val encryptedDataKb: Long = 0L,
    val uptimeSeconds: Long = 0L,
    val virtualIp: String = "192.168.1.1"
)

object VpnProxyManager {

    val SERVERS = listOf(
        VpnServer("ch", "Swiss Vault", "🇨🇭", "Switzerland", "Zurich", 28, "185.125.190.4", listOf("Strict Privacy", "Zero-Logs", "10 Gbps")),
        VpnServer("is", "Arctic Fortress", "🇮🇸", "Iceland", "Reykjavik", 42, "194.38.20.12", listOf("Freedom Shield", "No Censorship")),
        VpnServer("de", "Rhine Guard", "🇩🇪", "Germany", "Frankfurt", 34, "85.214.132.88", listOf("GDPR Shield", "Low Latency")),
        VpnServer("nl", "Amsterdam Pier", "🇳🇱", "Netherlands", "Amsterdam", 31, "185.220.101.5", listOf("P2P Friendly", "Multi-Hop")),
        VpnServer("sg", "Lion City", "🇸🇬", "Singapore", "Singapore", 58, "139.99.120.3", listOf("Asia Privacy Hub", "Fast Streaming")),
        VpnServer("ca", "Maple Shield", "🇨🇦", "Canada", "Toronto", 49, "198.50.200.12", listOf("Neutral Relay", "Clean DNS"))
    )

    val DNS_PROVIDERS = listOf(
        "Cloudflare (1.1.1.1 - Privacy+)",
        "AdGuard DNS (Anti-Tracker & Malware)",
        "Quad9 (9.9.9.9 - Threat Intelligence)",
        "Mullvad DoH (Strict Zero-Log)",
        "Custom DoH / WireGuard Relay"
    )

    private val _status = MutableStateFlow(
        VpnStatus(
            isConnected = false,
            currentServer = SERVERS.first()
        )
    )
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    fun toggleConnection() {
        val current = _status.value
        if (current.isConnected) {
            _status.value = current.copy(isConnected = false)
        } else {
            val server = current.currentServer ?: SERVERS.first()
            _status.value = current.copy(
                isConnected = true,
                currentServer = server,
                virtualIp = server.ipMask,
                encryptedDataKb = current.encryptedDataKb + 256
            )
        }
    }

    fun selectServer(server: VpnServer) {
        val current = _status.value
        _status.value = current.copy(
            currentServer = server,
            virtualIp = if (current.isConnected) server.ipMask else current.virtualIp
        )
    }

    fun selectDns(dnsProvider: String) {
        _status.value = _status.value.copy(selectedDnsProvider = dnsProvider)
    }

    fun addEncryptedTraffic(bytes: Long) {
        if (_status.value.isConnected) {
            val addedKb = (bytes / 1024L).coerceAtLeast(1L)
            _status.value = _status.value.copy(
                encryptedDataKb = _status.value.encryptedDataKb + addedKb
            )
        }
    }
}
