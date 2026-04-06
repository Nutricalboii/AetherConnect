package com.aether.connect.network

import android.content.Context
import android.util.Log
import org.webrtc.*
import java.nio.ByteBuffer

/**
 * WebRTC Manager — Handles peer connections, data channels, and media streams
 */
class WebRTCManager(private val context: Context) {

    companion object {
        private const val TAG = "WebRTCManager"
        private const val CHUNK_SIZE = 64 * 1024 // 64KB
        private val ICE_SERVERS = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
    }

    private var factory: PeerConnectionFactory? = null
    private val peerConnections = mutableMapOf<String, PeerConnection>()
    private val dataChannels = mutableMapOf<String, DataChannel>()

    var onIceCandidate: ((String, IceCandidate) -> Unit)? = null
    var onSdpOffer: ((String, SessionDescription) -> Unit)? = null
    var onSdpAnswer: ((String, SessionDescription) -> Unit)? = null
    var onDataReceived: ((String, ByteArray) -> Unit)? = null
    var onStringReceived: ((String, String) -> Unit)? = null
    var onConnectionStateChanged: ((String, PeerConnection.PeerConnectionState) -> Unit)? = null

    fun initialize() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )

        factory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        Log.d(TAG, "WebRTC initialized")
    }

    fun createPeerConnection(peerId: String): PeerConnection? {
        val config = PeerConnection.RTCConfiguration(ICE_SERVERS).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                onIceCandidate?.invoke(peerId, candidate)
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                Log.d(TAG, "Peer $peerId state: $newState")
                onConnectionStateChanged?.invoke(peerId, newState)
            }

            override fun onDataChannel(channel: DataChannel) {
                Log.d(TAG, "Data channel received from $peerId")
                dataChannels[peerId] = channel
                setupDataChannel(peerId, channel)
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {}
        }

        val pc = factory?.createPeerConnection(config, observer)
        if (pc != null) {
            peerConnections[peerId] = pc
        }
        return pc
    }

    fun createDataChannel(peerId: String, label: String = "fileTransfer"): DataChannel? {
        val pc = peerConnections[peerId] ?: return null
        val config = DataChannel.Init().apply {
            ordered = true
        }
        val channel = pc.createDataChannel(label, config)
        dataChannels[peerId] = channel
        setupDataChannel(peerId, channel)
        return channel
    }

    private fun setupDataChannel(peerId: String, channel: DataChannel) {
        channel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(amount: Long) {}

            override fun onStateChange() {
                Log.d(TAG, "DataChannel $peerId state: ${channel.state()}")
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                val data = ByteArray(buffer.data.remaining())
                buffer.data.get(data)

                if (buffer.binary) {
                    onDataReceived?.invoke(peerId, data)
                } else {
                    onStringReceived?.invoke(peerId, String(data))
                }
            }
        })
    }

    fun createOffer(peerId: String, callback: (SessionDescription) -> Unit) {
        val pc = peerConnections[peerId] ?: return
        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                pc.setLocalDescription(SimpleSdpObserver(), sdp)
                callback(sdp)
            }
            override fun onCreateFailure(error: String) { Log.e(TAG, "Offer failed: $error") }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, MediaConstraints())
    }

    fun createAnswer(peerId: String, callback: (SessionDescription) -> Unit) {
        val pc = peerConnections[peerId] ?: return
        pc.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                pc.setLocalDescription(SimpleSdpObserver(), sdp)
                callback(sdp)
            }
            override fun onCreateFailure(error: String) { Log.e(TAG, "Answer failed: $error") }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, MediaConstraints())
    }

    fun setRemoteDescription(peerId: String, sdp: SessionDescription) {
        peerConnections[peerId]?.setRemoteDescription(SimpleSdpObserver(), sdp)
    }

    fun addIceCandidate(peerId: String, candidate: IceCandidate) {
        peerConnections[peerId]?.addIceCandidate(candidate)
    }

    fun sendData(peerId: String, data: ByteArray) {
        val channel = dataChannels[peerId] ?: return
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(data), true)
        channel.send(buffer)
    }

    fun sendString(peerId: String, message: String) {
        val channel = dataChannels[peerId] ?: return
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(message.toByteArray()), false)
        channel.send(buffer)
    }

    fun closePeer(peerId: String) {
        dataChannels[peerId]?.close()
        dataChannels.remove(peerId)
        peerConnections[peerId]?.close()
        peerConnections.remove(peerId)
    }

    fun shutdown() {
        dataChannels.values.forEach { it.close() }
        dataChannels.clear()
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        factory?.dispose()
        factory = null
    }

    private class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String) { Log.e("SDP", "Create failed: $error") }
        override fun onSetFailure(error: String) { Log.e("SDP", "Set failed: $error") }
    }
}
