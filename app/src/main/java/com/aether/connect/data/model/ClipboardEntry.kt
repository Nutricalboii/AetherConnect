package com.aether.connect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_history")
data class ClipboardEntry(
    @PrimaryKey
    val id: String = "clip_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val content: String,
    val contentType: String = "text",    // text, url, image
    val sourceDeviceId: String = "local",
    val sourceDeviceName: String = "This Device",
    val contentHash: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isLocal: Boolean = true          // true if originated from this device
) {
    fun preview(maxLength: Int = 100): String {
        return if (content.length > maxLength) {
            content.take(maxLength) + "…"
        } else {
            content
        }
    }
}
