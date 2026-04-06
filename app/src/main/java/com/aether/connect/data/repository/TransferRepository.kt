package com.aether.connect.data.repository

import com.aether.connect.AetherApp
import com.aether.connect.data.model.Transfer
import kotlinx.coroutines.flow.Flow

class TransferRepository {
    private val dao = AetherApp.instance.database.transferDao()

    fun getAllTransfers(): Flow<List<Transfer>> = dao.getAllTransfers()
    fun getActiveTransfers(): Flow<List<Transfer>> = dao.getActiveTransfers()
    fun getTransfersForDevice(deviceId: String): Flow<List<Transfer>> = dao.getTransfersForDevice(deviceId)
    fun getActiveCount(): Flow<Int> = dao.getActiveCount()

    suspend fun getTransferById(id: String): Transfer? = dao.getTransferById(id)
    suspend fun createTransfer(transfer: Transfer) = dao.upsert(transfer)

    suspend fun updateProgress(id: String, status: String, progress: Int, bytes: Long, speed: Long) {
        dao.updateProgress(id, status, progress, bytes, speed)
    }

    suspend fun markCompleted(id: String) = dao.markCompleted(id)
    suspend fun markFailed(id: String, error: String) = dao.markFailed(id, error)

    suspend fun cleanup(maxAge: Long = 7 * 24 * 60 * 60 * 1000) {
        dao.deleteOldTransfers(System.currentTimeMillis() - maxAge)
    }
}
