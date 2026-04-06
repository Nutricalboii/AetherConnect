package com.aether.connect.data.db

import androidx.room.*
import com.aether.connect.data.model.Transfer
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer_history ORDER BY createdAt DESC")
    fun getAllTransfers(): Flow<List<Transfer>>

    @Query("SELECT * FROM transfer_history WHERE status IN ('pending', 'accepted', 'transferring') ORDER BY createdAt DESC")
    fun getActiveTransfers(): Flow<List<Transfer>>

    @Query("SELECT * FROM transfer_history WHERE deviceId = :deviceId ORDER BY createdAt DESC")
    fun getTransfersForDevice(deviceId: String): Flow<List<Transfer>>

    @Query("SELECT * FROM transfer_history WHERE id = :id")
    suspend fun getTransferById(id: String): Transfer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transfer: Transfer)

    @Update
    suspend fun update(transfer: Transfer)

    @Query("UPDATE transfer_history SET status = :status, progress = :progress, bytesTransferred = :bytes, speed = :speed WHERE id = :id")
    suspend fun updateProgress(id: String, status: String, progress: Int, bytes: Long, speed: Long)

    @Query("UPDATE transfer_history SET status = 'completed', progress = 100, completedAt = :time WHERE id = :id")
    suspend fun markCompleted(id: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE transfer_history SET status = 'failed', errorMessage = :error, completedAt = :time WHERE id = :id")
    suspend fun markFailed(id: String, error: String, time: Long = System.currentTimeMillis())

    @Query("DELETE FROM transfer_history WHERE completedAt > 0 AND completedAt < :cutoff")
    suspend fun deleteOldTransfers(cutoff: Long)

    @Query("SELECT COUNT(*) FROM transfer_history WHERE status IN ('pending', 'accepted', 'transferring')")
    fun getActiveCount(): Flow<Int>
}
