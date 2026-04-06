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
    var onAddTrack: ((String, MediaStream) -> Unit)? = null

    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    fun initialize() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )

        factory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoEncoderFactory(HardwareVideoEncoderFactory(
                EglBase.create().eglBaseContext, 
                true,  // enableIntelVp8Encoder
                true   // enableH264HighProfile
            ))
            .setVideoDecoderFactory(HardwareVideoDecoderFactory(EglBase.create().eglBaseContext))
            .createPeerConnectionFactory()

        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext)
        Log.d(TAG, "WebRTC V3: H264 Hardware Acceleration active")
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
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
            override fun onAddStream(stream: MediaStream) {
                Log.d(TAG, "MediaStream added from $peerId")
                onAddTrack?.invoke(peerId, stream)
            }
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
                if (streams.isNotEmpty()) {
                    onAddTrack?.invoke(peerId, streams[0])
                }
            }
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

    // V2: Screen Casting
    fun startScreenCapture(peerId: String, capturer: VideoCapturer) {
        val pc = peerConnections[peerId] ?: return
        
        val videoSource = factory?.createVideoSource(true) ?: return
        localVideoSource = videoSource
        
        capturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
        // High quality: 1080p @ 60fps (if supported)
        capturer.startCapture(1920, 1080, 60)

        localVideoTrack = factory?.createVideoTrack("VIDEO_TRACK", videoSource)
        
        // SDP Munging for Bitrate Control (Adaptive 1.5 - 8 Mbps)
        val transceiverInit = RtpTransceiver.RtpTransceiverInit(
            RtpTransceiver.RtpTransceiverDirection.SEND_ONLY,
            listOf("STREAM_ID"),
            listOf(RtpParameters.Encoding("1", true, 1.0).apply {
                minBitrateBps = 1_500_000
                maxBitrateBps = 8_000_000
                maxFramerate = 60
            })
        )
        pc.addTransceiver(localVideoTrack, transceiverInit)
        
        Log.d(TAG, "V3 Screen capture started: 1080p/60fps @ 1.5-8Mbps")
    }

    fun stopScreenCapture() {
        localVideoTrack?.dispose()
        localVideoSource?.dispose()
        localVideoTrack = null
        localVideoSource = null
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
