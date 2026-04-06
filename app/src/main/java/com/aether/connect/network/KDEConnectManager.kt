package com.aether.connect.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * KDEConnectManager — Interoperability layer for KDE Connect protocol (Port 1714-1764)
 * Allows AetherConnect to discover and be discovered by KDE Connect clients.
 */
class KDEConnectManager {

    companion object {
        private const val TAG = "KDEConnectManager"
        private const val KDE_PORT = 1716
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var udpSocket: DatagramSocket? = null
    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true
        
        startUdpDiscovery()
        startTcpServer()
        Log.d(TAG, "KDE Bridge started on port $KDE_PORT")
    }

    private fun startUdpDiscovery() {
        scope.launch {
            try {
                udpSocket = DatagramSocket(KDE_PORT).apply { broadcast = true }
                val buffer = ByteArray(4096)
                
                while (isActive) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    udpSocket?.receive(packet)
                    val data = String(packet.data, 0, packet.length)
                    handleKdePacket(data, packet.address)
                }
            } catch (e: Exception) {
                Log.e(TAG, "KDE UDP Failure: ${e.message}")
            }
        }
    }

    private fun startTcpServer() {
        scope.launch {
            try {
                serverSocket = ServerSocket(KDE_PORT)
                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    handleTcpConnection(socket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "KDE TCP Failure: ${e.message}")
            }
        }
    }

    private fun handleTcpConnection(socket: Socket) {
        scope.launch {
            try {
                val input = socket.getInputStream().bufferedReader()
                val output = socket.getOutputStream().bufferedWriter()
                
                val line = input.readLine()
                if (line != null) {
                    Log.d(TAG, "KDE TCP Packet: $line")
                    // Basic handshake logic
                }
                socket.close()
            } catch (e: Exception) {
                Log.w(TAG, "KDE Connection closed: ${e.message}")
            }
        }
    }

    private fun handleKdePacket(json: String, address: InetAddress) {
        try {
            val root = Gson().fromJson(json, JsonObject::class.java)
            val type = root.get("type")?.asString
            
            if (type == "kdeconnect.identity") {
                val body = root.getAsJsonObject("body")
                val deviceName = body.get("deviceName")?.asString ?: "KDE Device"
                val deviceId = body.get("deviceId")?.asString ?: address.hostAddress
                
                Log.d(TAG, "KDE Device Discovered: $deviceName ($deviceId)")
                // Map to Aether device list in AetherService
            }
        } catch (e: Exception) {
            // Not a KDE packet or malformed
        }
    }

    fun stop() {
        isRunning = false
        scope.cancel()
        udpSocket?.close()
        serverSocket?.close()
    }
}
