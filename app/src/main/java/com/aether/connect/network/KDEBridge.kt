package com.aether.connect.network

import android.util.Log

/**
 * KDE Connect Compatibility Bridge
 * Discovers KDE Connect devices and translates protocol messages
 * This is an OPTIONAL module — can be enabled/disabled in settings
 */
class KDEBridge {

    companion object {
        private const val TAG = "KDEBridge"
        const val KDE_SERVICE_TYPE = "_kdeconnect._tcp."
        const val KDE_DEFAULT_PORT = 1716

        // KDE Connect packet types we support
        const val KDE_TYPE_IDENTITY = "kdeconnect.identity"
        const val KDE_TYPE_PAIR = "kdeconnect.pair"
        const val KDE_TYPE_CLIPBOARD = "kdeconnect.clipboard"
        const val KDE_TYPE_CLIPBOARD_CONNECT = "kdeconnect.clipboard.connect"
        const val KDE_TYPE_SHARE = "kdeconnect.share.request"
        const val KDE_TYPE_PING = "kdeconnect.ping"
        const val KDE_TYPE_NOTIFICATION = "kdeconnect.notification"
    }

    var enabled = false
        private set

    fun enable() {
        enabled = true
        Log.d(TAG, "KDE Connect bridge enabled")
    }

    fun disable() {
        enabled = false
        Log.d(TAG, "KDE Connect bridge disabled")
    }

    /**
     * Convert an AetherConnect message to a KDE Connect-compatible JSON packet
     */
    fun toKDEPacket(message: AetherMessage): String? {
        if (!enabled) return null

        return when (message.type) {
            "clipboard" -> {
                val content = message.payload.get("content")?.asString ?: return null
                """{"id":${System.currentTimeMillis()},"type":"$KDE_TYPE_CLIPBOARD","body":{"content":"${escapeJson(content)}"}}"""
            }
            "pair" -> {
                when (message.action) {
                    "request" -> """{"id":${System.currentTimeMillis()},"type":"$KDE_TYPE_PAIR","body":{"pair":true}}"""
                    "reject" -> """{"id":${System.currentTimeMillis()},"type":"$KDE_TYPE_PAIR","body":{"pair":false}}"""
                    else -> null
                }
            }
            "system" -> {
                if (message.action == "heartbeat") {
                    """{"id":${System.currentTimeMillis()},"type":"$KDE_TYPE_PING","body":{}}"""
                } else null
            }
            else -> null
        }
    }

    /**
     * Convert a KDE Connect JSON packet to an AetherConnect message
     */
    fun fromKDEPacket(json: String, sourceDeviceId: String): AetherMessage? {
        if (!enabled) return null

        try {
            val type = extractJsonString(json, "type") ?: return null
            return when (type) {
                KDE_TYPE_CLIPBOARD -> {
                    val content = extractJsonString(json, "content") ?: return null
                    AetherProtocol.clipboardUpdate(sourceDeviceId, content, "text")
                }
                KDE_TYPE_PAIR -> {
                    val pair = json.contains("\"pair\":true")
                    if (pair) AetherProtocol.pairRequest(sourceDeviceId, "KDE Device", "", "", KDE_DEFAULT_PORT)
                    else AetherMessage("pair", "reject", sourceDeviceId)
                }
                KDE_TYPE_PING -> {
                    AetherProtocol.heartbeat(sourceDeviceId)
                }
                KDE_TYPE_IDENTITY -> {
                    val name = extractJsonString(json, "deviceName") ?: "KDE Device"
                    AetherProtocol.deviceInfo(sourceDeviceId, name, "linux", listOf("file", "clipboard"))
                }
                else -> {
                    Log.d(TAG, "Unsupported KDE packet type: $type")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse KDE packet: ${e.message}")
            return null
        }
    }

    private fun extractJsonString(json: String, key: String): String? {
        val regex = """"$key"\s*:\s*"([^"]*?)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
