package com.aether.connect.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_history")
data class Transfer(
    @PrimaryKey
    val id: String = "tx_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val deviceId: String,
    val deviceName: String = "",
    val fileName: String,
    val fileSize: Long,
    val fileType: String = "application/octet-stream",
    val direction: String = "outgoing",  // "outgoing" or "incoming"
    val status: String = "pending",      // pending, accepted, transferring, completed, failed, cancelled
    val progress: Int = 0,               // 0-100
    val bytesTransferred: Long = 0,
    val speed: Long = 0,                 // bytes per second
    val savePath: String = "",
    val startedAt: Long = 0,
    val completedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val errorMessage: String = ""
) {
    val isActive: Boolean
        get() = status in listOf("pending", "accepted", "transferring")

    val isComplete: Boolean
        get() = status == "completed"

    fun formattedSize(): String {
        return when {
            fileSize > 1024 * 1024 * 1024 -> "%.1f GB".format(fileSize / (1024.0 * 1024 * 1024))
            fileSize > 1024 * 1024 -> "%.1f MB".format(fileSize / (1024.0 * 1024))
            fileSize > 1024 -> "%.1f KB".format(fileSize / 1024.0)
            else -> "$fileSize B"
        }
    }

    fun formattedSpeed(): String {
        return when {
            speed > 1024 * 1024 -> "%.1f MB/s".format(speed / (1024.0 * 1024))
            speed > 1024 -> "%.1f KB/s".format(speed / 1024.0)
            else -> "$speed B/s"
        }
    }
}
