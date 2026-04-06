package com.aether.connect.data.repository

import com.aether.connect.AetherApp
import com.aether.connect.data.model.Device
import kotlinx.coroutines.flow.Flow

class DeviceRepository {
    private val dao = AetherApp.instance.database.deviceDao()

    fun getAllDevices(): Flow<List<Device>> = dao.getAllDevices()
    fun getOnlineDevices(): Flow<List<Device>> = dao.getOnlineDevices()
    fun getPairedDevices(): Flow<List<Device>> = dao.getPairedDevices()
    fun getOnlineCount(): Flow<Int> = dao.getOnlineCount()

    suspend fun getDeviceById(id: String): Device? = dao.getDeviceById(id)
    suspend fun upsertDevice(device: Device) = dao.upsertDevice(device)
    suspend fun upsertDevices(devices: List<Device>) = dao.upsertDevices(devices)
    suspend fun updateDevice(device: Device) = dao.updateDevice(device)

    suspend fun setOnline(id: String, online: Boolean) = dao.updateOnlineStatus(id, online)
    suspend fun pairDevice(id: String) = dao.updateTrustLevel(id, 1)
    suspend fun trustDevice(id: String) = dao.updateTrustLevel(id, 2)
    suspend fun unpairDevice(id: String) = dao.updateTrustLevel(id, 0)

    suspend fun deleteDevice(device: Device) = dao.deleteDevice(device)
    suspend fun deleteById(id: String) = dao.deleteById(id)

    suspend fun markStaleOffline(timeoutMs: Long = 30_000) {
        dao.markStaleOffline(System.currentTimeMillis() - timeoutMs)
    }
}
