package com.aether.connect.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.*
import android.os.Looper
import android.util.Log

/**
 * WiFi Direct Manager — P2P file transfer fallback when WebRTC STUN not available
 */
class WiFiDirectManager(private val context: Context) {

    companion object {
        private const val TAG = "WiFiDirect"
    }

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null
    private var peers = mutableListOf<WifiP2pDevice>()

    var onPeersChanged: ((List<WifiP2pDevice>) -> Unit)? = null
    var onConnectionResult: ((WifiP2pInfo) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun initialize() {
        manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        channel = manager?.initialize(context, Looper.getMainLooper(), null)
        Log.d(TAG, "WiFi Direct initialized")
    }

    @SuppressLint("MissingPermission")
    fun discoverPeers() {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Peer discovery started")
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "Peer discovery failed: $reason")
            }
        })
    }

    fun updatePeerList(deviceList: WifiP2pDeviceList) {
        peers.clear()
        peers.addAll(deviceList.deviceList)
        onPeersChanged?.invoke(peers.toList())
        Log.d(TAG, "Found ${peers.size} WiFi Direct peers")
    }

    @SuppressLint("MissingPermission")
    fun connectToPeer(device: WifiP2pDevice) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }

        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d(TAG, "Connection initiated to ${device.deviceName}")
            }
            override fun onFailure(reason: Int) {
                Log.e(TAG, "Connection failed: $reason")
            }
        })
    }

    fun handleConnectionInfo(info: WifiP2pInfo) {
        onConnectionResult?.invoke(info)
        if (info.groupFormed) {
            Log.d(TAG, "Group formed. Owner: ${info.isGroupOwner}, IP: ${info.groupOwnerAddress}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        manager?.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() { Log.d(TAG, "Disconnected from WiFi Direct group") }
            override fun onFailure(reason: Int) { Log.w(TAG, "Disconnect failed: $reason") }
        })
    }

    fun stopDiscovery() {
        manager?.stopPeerDiscovery(channel, null)
    }

    fun shutdown() {
        stopDiscovery()
        disconnect()
    }
}
