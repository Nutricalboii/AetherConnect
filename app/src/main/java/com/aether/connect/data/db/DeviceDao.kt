package com.aether.connect.data.db

import androidx.room.*
import com.aether.connect.data.model.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM paired_devices ORDER BY lastSeen DESC")
    fun getAllDevices(): Flow<List<Device>>

    @Query("SELECT * FROM paired_devices WHERE isOnline = 1 ORDER BY lastSeen DESC")
    fun getOnlineDevices(): Flow<List<Device>>

    @Query("SELECT * FROM paired_devices WHERE trustLevel >= 1 ORDER BY lastSeen DESC")
    fun getPairedDevices(): Flow<List<Device>>

    @Query("SELECT * FROM paired_devices WHERE id = :id")
    suspend fun getDeviceById(id: String): Device?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: Device)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<Device>)

    @Update
    suspend fun updateDevice(device: Device)

    @Query("UPDATE paired_devices SET isOnline = :online, lastSeen = :time WHERE id = :id")
    suspend fun updateOnlineStatus(id: String, online: Boolean, time: Long = System.currentTimeMillis())

    @Query("UPDATE paired_devices SET trustLevel = :level, pairedAt = :time WHERE id = :id")
    suspend fun updateTrustLevel(id: String, level: Int, time: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDevice(device: Device)

    @Query("DELETE FROM paired_devices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM paired_devices WHERE isOnline = 1")
    fun getOnlineCount(): Flow<Int>

    @Query("UPDATE paired_devices SET isOnline = 0 WHERE lastSeen < :cutoff")
    suspend fun markStaleOffline(cutoff: Long)
}
