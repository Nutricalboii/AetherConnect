package com.aether.connect.nfc

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * NFC Payload structure for AetherConnect tap-to-pair and tap-to-send
 */
data class NFCPayload(
    val type: String = "aether-connect",
    val action: String = "pair",         // pair, send, mirror
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("device_name")
    val deviceName: String,
    @SerializedName("public_key")
    val publicKey: String = "",
    val ip: String = "",
    val port: Int = 8888,
    val platform: String = "android",
    val version: String = "1.0.0",
    // Extra data for send action
    val fileName: String? = null,
    val fileSize: Long? = null
) {
    fun toJson(): String = Gson().toJson(this)

    fun toNdefBytes(): ByteArray = toJson().toByteArray(Charsets.UTF_8)

    companion object {
        const val MIME_TYPE = "application/vnd.aether.connect"

        fun fromJson(json: String): NFCPayload? {
            return try {
                Gson().fromJson(json, NFCPayload::class.java)
            } catch (e: Exception) {
                null
            }
        }

        fun fromBytes(bytes: ByteArray): NFCPayload? {
            return fromJson(String(bytes, Charsets.UTF_8))
        }

        fun createPairPayload(deviceId: String, deviceName: String, publicKey: String,
                              ip: String, port: Int): NFCPayload {
            return NFCPayload(
                action = "pair",
                deviceId = deviceId,
                deviceName = deviceName,
                publicKey = publicKey,
                ip = ip,
                port = port
            )
        }

        fun createSendPayload(deviceId: String, deviceName: String, ip: String, port: Int,
                              fileName: String, fileSize: Long): NFCPayload {
            return NFCPayload(
                action = "send",
                deviceId = deviceId,
                deviceName = deviceName,
                ip = ip,
                port = port,
                fileName = fileName,
                fileSize = fileSize
            )
        }
    }
}
