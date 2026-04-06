package com.aether.connect.network

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.aether.connect.data.model.Device
import java.util.UUID

/**
 * BLE Scanner — Discover nearby AetherConnect devices via Bluetooth Low Energy
 */
class BLEScanner(private val context: Context) {

    companion object {
        private const val TAG = "BLEScanner"
        val SERVICE_UUID: UUID = UUID.fromString("a3e7f8b0-1234-5678-abcd-000000000001")
    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var isScanning = false
    private var isAdvertising = false

    var onDeviceFound: ((Device) -> Unit)? = null

    fun initialize() {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = manager?.adapter
        scanner = bluetoothAdapter?.bluetoothLeScanner
        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        Log.d(TAG, "BLE initialized. Adapter: ${bluetoothAdapter != null}")
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (isScanning || scanner == null) return

        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        scanner?.startScan(listOf(filter), settings, scanCallback)
        isScanning = true
        Log.d(TAG, "BLE scanning started")
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!isScanning) return
        scanner?.stopScan(scanCallback)
        isScanning = false
        Log.d(TAG, "BLE scanning stopped")
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(deviceName: String) {
        if (isAdvertising || advertiser == null) return

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addServiceData(
                ParcelUuid(SERVICE_UUID),
                "Aether-$deviceName".toByteArray().take(20).toByteArray()
            )
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
        Log.d(TAG, "BLE advertising started as Aether-$deviceName")
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        if (!isAdvertising) return
        advertiser?.stopAdvertising(advertiseCallback)
        isAdvertising = false
        Log.d(TAG, "BLE advertising stopped")
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val serviceData = result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))
            val name = serviceData?.let { String(it).trim() } ?: result.device?.name ?: "Unknown"
            val address = result.device?.address ?: return

            val device = Device(
                id = "ble_$address",
                name = name.removePrefix("Aether-"),
                platform = "android",
                discoverySource = "ble",
                ipAddress = "",
                lastSeen = System.currentTimeMillis(),
                isOnline = true
            )

            onDeviceFound?.invoke(device)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed: $errorCode")
            isScanning = false
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "BLE advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE advertising failed: $errorCode")
            isAdvertising = false
        }
    }

    fun shutdown() {
        stopScanning()
        stopAdvertising()
    }
}
