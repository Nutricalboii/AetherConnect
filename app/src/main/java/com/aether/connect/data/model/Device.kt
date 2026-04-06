package com.aether.connect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class Device(
    @PrimaryKey
    val id: String,
    val name: String,
    val platform: String = "unknown",    // android, ios, windows, macos, linux
    val publicKey: String = "",
    val ipAddress: String = "",
    val port: Int = 8888,
    val capabilities: String = "file,clipboard",  // comma-separated
    val trustLevel: Int = 0,             // 0=unknown, 1=paired, 2=trusted
    val discoverySource: String = "ble", // ble, mdns, wifi_direct, manual, nfc
    val lastSeen: Long = System.currentTimeMillis(),
    val pairedAt: Long = 0,
    val isOnline: Boolean = false
) {
    fun getCapabilityList(): List<String> = capabilities.split(",").map { it.trim() }
    fun hasCapability(cap: String): Boolean = getCapabilityList().contains(cap)

    companion object {
        fun generateId(): String = "aether_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
