package com.aether.connect.data.repository

import com.aether.connect.AetherApp
import com.aether.connect.data.model.ClipboardEntry
import com.aether.connect.util.CryptoUtil
import kotlinx.coroutines.flow.Flow

class ClipboardRepository {
    private val dao = AetherApp.instance.database.clipboardDao()

    fun getRecentEntries(): Flow<List<ClipboardEntry>> = dao.getRecentEntries()

    suspend fun addEntry(content: String, contentType: String = "text",
                         sourceDeviceId: String = "local", sourceDeviceName: String = "This Device",
                         isLocal: Boolean = true): Boolean {
        val hash = CryptoUtil.sha256Short(content)

        // Check for duplicate
        val existing = dao.getByHash(hash)
        if (existing != null) return false

        val entry = ClipboardEntry(
            content = content,
            contentType = contentType,
            sourceDeviceId = sourceDeviceId,
            sourceDeviceName = sourceDeviceName,
            contentHash = hash,
            isLocal = isLocal
        )

        dao.insert(entry)
        dao.trimToSize(50) // Keep only latest 50
        return true
    }

    suspend fun deleteById(id: String) = dao.deleteById(id)
    suspend fun clearAll() = dao.clearAll()
}
