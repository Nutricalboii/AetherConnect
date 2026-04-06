package com.aether.connect.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.aether.connect.data.model.Device
import com.aether.connect.data.repository.DeviceRepository
import com.aether.connect.network.*
import com.aether.connect.util.NotificationUtil
import com.aether.connect.util.NetworkUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * AetherService — Main foreground daemon
 * Orchestrates discovery, clipboard sync, file transfer, NFC
 */
class AetherService : Service() {

    companion object {
        private const val TAG = "AetherService"
        private const val NOTIFICATION_ID = 1001
    }

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val deviceRepo = DeviceRepository()

    // Subsystems
    lateinit var bleScanner: BLEScanner
        private set
    lateinit var mdnsManager: MDNSManager
        private set
    lateinit var wifiDirect: WiFiDirectManager
        private set
    lateinit var webrtc: WebRTCManager
        private set
    lateinit var kdeBridge: KDEBridge
        private set

    // State
    private val _discoveredDevices = MutableStateFlow<List<Device>>(emptyList())
    val discoveredDevices: StateFlow<List<Device>> = _discoveredDevices

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var deviceId: String = ""
    private var deviceName: String = ""

    inner class LocalBinder : Binder() {
        fun getService(): AetherService = this@AetherService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AetherService created")

        deviceId = Device.generateId()
        deviceName = Build.MODEL ?: "Android Device"

        // Initialize all subsystems
        bleScanner = BLEScanner(this).also { it.initialize() }
        mdnsManager = MDNSManager(this).also { it.initialize() }
        wifiDirect = WiFiDirectManager(this).also { it.initialize() }
        webrtc = WebRTCManager(this).also { it.initialize() }
        kdeBridge = KDEBridge()

        // Wire up discovery callbacks
        setupDiscoveryCallbacks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as foreground service
        val notification = NotificationUtil.createServiceNotification(this, 0)
        startForeground(NOTIFICATION_ID, notification)

        startServices()
        _isRunning.value = true

        return START_STICKY
    }

    private fun startServices() {
        scope.launch {
            // Start BLE discovery
            try {
                bleScanner.startAdvertising(deviceName)
                bleScanner.startScanning()
                Log.d(TAG, "BLE started")
            } catch (e: Exception) {
                Log.w(TAG, "BLE failed: ${e.message}")
            }

            // Start mDNS
            try {
                val port = AetherProtocol.DEFAULT_PORT
                mdnsManager.registerService(deviceName, port)
                mdnsManager.startDiscovery()
                Log.d(TAG, "mDNS started on port $port")
            } catch (e: Exception) {
                Log.w(TAG, "mDNS failed: ${e.message}")
            }

            // Start WiFi Direct discovery
            try {
                wifiDirect.discoverPeers()
                Log.d(TAG, "WiFi Direct discovery started")
            } catch (e: Exception) {
                Log.w(TAG, "WiFi Direct failed: ${e.message}")
            }

            // Start clipboard monitoring
            startClipboardMonitoring()

            // Periodic stale device cleanup and notification update
            launch {
                while (isActive) {
                    delay(10_000)
                    deviceRepo.markStaleOffline(30_000)
                    updateNotification()
                }
            }

            Log.d(TAG, "All services started")
        }
    }

    private fun setupDiscoveryCallbacks() {
        // BLE device found
        bleScanner.onDeviceFound = { device ->
            scope.launch {
                addDiscoveredDevice(device)
            }
        }

        // mDNS device found
        mdnsManager.onDeviceFound = { device ->
            scope.launch {
                addDiscoveredDevice(device)
            }
        }

        mdnsManager.onDeviceLost = { id ->
            scope.launch {
                deviceRepo.setOnline(id, false)
                refreshDeviceList()
            }
        }

        // WiFi Direct peers
        wifiDirect.onPeersChanged = { wifiPeers ->
            scope.launch {
                wifiPeers.forEach { p2pDevice ->
                    val device = Device(
                        id = "wfd_${p2pDevice.deviceAddress}",
                        name = p2pDevice.deviceName ?: "WiFi Direct Device",
                        platform = "unknown",
                        discoverySource = "wifi_direct",
                        lastSeen = System.currentTimeMillis(),
                        isOnline = true
                    )
                    addDiscoveredDevice(device)
                }
            }
        }
    }

    private suspend fun addDiscoveredDevice(device: Device) {
        deviceRepo.upsertDevice(device)
        refreshDeviceList()
    }

    private suspend fun refreshDeviceList() {
        // Emit current device list snapshot
        // Note: the Flow-based queries in DeviceDao handle reactive updates automatically
    }

    private fun startClipboardMonitoring() {
        val clipService = ClipboardService(this, scope, deviceId)
        clipService.startMonitoring()
    }

    private fun updateNotification() {
        val count = _discoveredDevices.value.count { it.isOnline }
        val notification = NotificationUtil.createServiceNotification(this, count)
        NotificationUtil.updateNotification(this, NOTIFICATION_ID, notification)
    }

    // Public API for UI
    fun getDeviceId(): String = deviceId
    fun getDeviceName(): String = deviceName

    fun pairDevice(device: Device) {
        scope.launch {
            deviceRepo.pairDevice(device.id)
            Log.d(TAG, "Paired with ${device.name}")
        }
    }

    fun unpairDevice(device: Device) {
        scope.launch {
            deviceRepo.unpairDevice(device.id)
            Log.d(TAG, "Unpaired from ${device.name}")
        }
    }

    fun toggleKDEBridge(enabled: Boolean) {
        if (enabled) kdeBridge.enable() else kdeBridge.disable()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        scope.cancel()
        bleScanner.shutdown()
        mdnsManager.shutdown()
        wifiDirect.shutdown()
        webrtc.shutdown()
        Log.d(TAG, "AetherService destroyed")
    }
}
