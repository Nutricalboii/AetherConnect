package com.aether.connect.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * AetherConnect Protocol — Common message format for all communication
 */
object AetherProtocol {
    const val VERSION = "1.0.0"
    const val DEFAULT_PORT = 8888
    const val MDNS_SERVICE_TYPE = "_aetherconnect._tcp."
    const val BLE_SERVICE_UUID = "a3e7f8b0-1234-5678-abcd-aetherconnect"
    const val KDE_SERVICE_TYPE = "_kdeconnect._tcp."

    private val gson = Gson()

    fun serialize(message: AetherMessage): String = gson.toJson(message)
    fun deserialize(json: String): AetherMessage = gson.fromJson(json, AetherMessage::class.java)

    // Create standard messages
    fun pairRequest(deviceId: String, deviceName: String, publicKey: String, ip: String, port: Int): AetherMessage {
        val payload = JsonObject().apply {
            addProperty("device_name", deviceName)
            addProperty("public_key", publicKey)
            addProperty("ip", ip)
            addProperty("port", port)
            addProperty("platform", "android")
            addProperty("version", VERSION)
        }
        return AetherMessage("pair", "request", deviceId, payload)
    }

    fun pairAccept(deviceId: String): AetherMessage {
        return AetherMessage("pair", "accept", deviceId, JsonObject())
    }

    fun fileRequest(deviceId: String, fileName: String, fileSize: Long, fileType: String): AetherMessage {
        val payload = JsonObject().apply {
            addProperty("file_name", fileName)
            addProperty("file_size", fileSize)
            addProperty("file_type", fileType)
            addProperty("chunks", (fileSize / (64 * 1024)) + 1)
        }
        return AetherMessage("file", "request", deviceId, payload)
    }

    fun clipboardUpdate(deviceId: String, content: String, contentType: String): AetherMessage {
        val payload = JsonObject().apply {
            addProperty("content", content)
            addProperty("content_type", contentType)
        }
        return AetherMessage("clipboard", "update", deviceId, payload)
    }

    fun heartbeat(deviceId: String): AetherMessage {
        return AetherMessage("system", "heartbeat", deviceId, JsonObject())
    }

    fun deviceInfo(deviceId: String, name: String, platform: String, capabilities: List<String>): AetherMessage {
        val payload = JsonObject().apply {
            addProperty("name", name)
            addProperty("platform", platform)
            addProperty("capabilities", capabilities.joinToString(","))
            addProperty("version", VERSION)
        }
        return AetherMessage("system", "info", deviceId, payload)
    }

    // V2: Screen Casting
    fun castRequest(deviceId: String): AetherMessage {
        return AetherMessage("cast", "request", deviceId, JsonObject())
    }

    fun castResponse(deviceId: String, accepted: Boolean): AetherMessage {
        return AetherMessage("cast", if (accepted) "accept" else "reject", deviceId, JsonObject())
    }

    // V2: Remote Input
    fun inputEvent(deviceId: String, event: InputEvent): AetherMessage {
        val payload = JsonObject().apply {
            addProperty("type", event.type) // MOVE, DOWN, UP, KEY
            addProperty("x", event.x)
            addProperty("y", event.y)
            addProperty("scrollX", event.scrollX)
            addProperty("scrollY", event.scrollY)
            addProperty("keyCode", event.keyCode)
            addProperty("metaState", event.metaState)
        }
        return AetherMessage("input", "data", deviceId, payload)
    }
}

data class InputEvent(
    val type: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val scrollX: Float = 0f,
    val scrollY: Float = 0f,
    val keyCode: Int = 0,
    val metaState: Int = 0
)

data class AetherMessage(
    val type: String,           // pair, file, clipboard, input, cast, system
    val action: String,         // request, accept, reject, data, cancel, heartbeat, info
    @SerializedName("device_id")
    val deviceId: String,
    val payload: JsonObject = JsonObject(),
    val timestamp: Long = System.currentTimeMillis()
)
