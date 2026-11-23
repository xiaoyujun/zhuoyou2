package com.example.network

import com.example.model.NetworkAddressOption
import java.net.Inet4Address
import java.net.NetworkInterface

object LanUtils {

    fun discoverLanAddresses(): List<NetworkAddressOption> {
        val options = mutableListOf<NetworkAddressOption>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback || iface.isVirtual) continue
                val addresses = iface.inetAddresses?.toList().orEmpty()
                for (addr in addresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                        val ip = addr.hostAddress ?: continue
                        val label = when {
                            iface.name.startsWith("wlan", ignoreCase = true) -> "Wi-Fi (${iface.name})"
                            iface.name.startsWith("ap", ignoreCase = true) ||
                                iface.name.startsWith("swlan", ignoreCase = true) -> "个人热点 (${iface.name})"
                            iface.name.startsWith("eth", ignoreCase = true) -> "以太网 (${iface.name})"
                            else -> "局域网 (${iface.name})"
                        }
                        options += NetworkAddressOption(
                            interfaceName = iface.name,
                            ipAddress = ip,
                            displayLabel = label
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Fall through to localhost fallback if interface query fails
        }

        // Prioritize private RFC1918 Wi-Fi / Hotspot addresses
        val sorted = options
            .distinctBy { it.ipAddress }
            .sortedWith(
                compareBy<NetworkAddressOption> {
                    when {
                        it.ipAddress.startsWith("192.168.") -> 0
                        it.ipAddress.startsWith("10.") -> 1
                        it.ipAddress.startsWith("172.") -> 2
                        else -> 3
                    }
                }.thenBy { it.interfaceName }
            )

        return if (sorted.isNotEmpty()) {
            sorted
        } else {
            listOf(
                NetworkAddressOption(
                    interfaceName = "lo",
                    ipAddress = "127.0.0.1",
                    displayLabel = "本机回路 (请连接 Wi-Fi 或开启热点)"
                )
            )
        }
    }
}
