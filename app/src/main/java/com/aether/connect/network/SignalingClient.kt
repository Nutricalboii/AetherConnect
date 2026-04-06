package com.aether.connect.network

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * WebSocket signaling client for WebRTC SDP/ICE exchange
 */
class SignalingClient(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "SignalingClient"
        private const val RECONNECT_DELAY = 5000L
    }

    private var client: WebSocketClient? = null
    private var serverUri: String? = null
    private var running = false

    private val _messages = MutableSharedFlow<AetherMessage>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<AetherMessage> = _messages

    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState: SharedFlow<ConnectionState> = _connectionState

    enum class ConnectionState { CONNECTING, CONNECTED, DISCONNECTED, ERROR }

    fun connect(uri: String) {
        serverUri = uri
        running = true
        doConnect(uri)
    }

    private fun doConnect(uri: String) {
        scope.launch(Dispatchers.IO) {
            try {
                _connectionState.emit(ConnectionState.CONNECTING)

                client = object : WebSocketClient(URI(uri)) {
                    override fun onOpen(handshake: ServerHandshake?) {
                        Log.d(TAG, "Connected to signaling server")
                        scope.launch { _connectionState.emit(ConnectionState.CONNECTED) }
                    }

                    override fun onMessage(message: String?) {
                        message?.let {
                            try {
                                val msg = AetherProtocol.deserialize(it)
                                scope.launch { _messages.emit(msg) }
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to parse message: ${e.message}")
                            }
                        }
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        Log.d(TAG, "Disconnected: $reason (code=$code)")
                        scope.launch {
                            _connectionState.emit(ConnectionState.DISCONNECTED)
                            if (running) {
                                delay(RECONNECT_DELAY)
                                serverUri?.let { doConnect(it) }
                            }
                        }
                    }

                    override fun onError(ex: Exception?) {
                        Log.e(TAG, "WebSocket error: ${ex?.message}")
                        scope.launch { _connectionState.emit(ConnectionState.ERROR) }
                    }
                }

                client?.connect()
            } catch (e: Exception) {
                Log.e(TAG, "Connection failed: ${e.message}")
                _connectionState.emit(ConnectionState.ERROR)
                if (running) {
                    delay(RECONNECT_DELAY)
                    serverUri?.let { doConnect(it) }
                }
            }
        }
    }

    fun send(message: AetherMessage) {
        try {
            client?.send(AetherProtocol.serialize(message))
        } catch (e: Exception) {
            Log.e(TAG, "Send failed: ${e.message}")
        }
    }

    fun disconnect() {
        running = false
        client?.close()
        client = null
    }

    val isConnected: Boolean
        get() = client?.isOpen == true
}
