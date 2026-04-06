package com.aether.connect.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.aether.connect.data.model.Device

/**
 * mDNS Manager — Register and discover AetherConnect services on LAN via NsdManager
 */
class MDNSManager(private val context: Context) {

    companion object {
        private const val TAG = "MDNSManager"
        private const val SERVICE_TYPE = "_aetherconnect._tcp."
        private const val SERVICE_NAME_PREFIX = "AetherConnect"
    }

    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isRegistered = false
    private var isDiscovering = false

    var onDeviceFound: ((Device) -> Unit)? = null
    var onDeviceLost: ((String) -> Unit)? = null

    fun initialize() {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
        Log.d(TAG, "mDNS initialized")
    }

    fun registerService(deviceName: String, port: Int) {
        if (isRegistered) return

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "$SERVICE_NAME_PREFIX-$deviceName"
            serviceType = SERVICE_TYPE
            setPort(port)
            setAttribute("name", deviceName)
            setAttribute("platform", "android")
            setAttribute("caps", "file,clipboard,nfc")
            setAttribute("version", AetherProtocol.VERSION)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                isRegistered = true
                Log.d(TAG, "mDNS service registered: ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "mDNS registration failed: $errorCode")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                isRegistered = false
                Log.d(TAG, "mDNS service unregistered")
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "mDNS unregistration failed: $errorCode")
            }
        }

        nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    fun startDiscovery() {
        if (isDiscovering) return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                isDiscovering = true
                Log.d(TAG, "mDNS discovery started for $serviceType")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType == SERVICE_TYPE) {
                    resolveService(serviceInfo)
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                val id = "mdns_${serviceInfo.serviceName}"
                onDeviceLost?.invoke(id)
                Log.d(TAG, "mDNS service lost: ${serviceInfo.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                isDiscovering = false
                Log.d(TAG, "mDNS discovery stopped")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "mDNS start discovery failed: $errorCode")
                isDiscovering = false
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "mDNS stop discovery failed: $errorCode")
            }
        }

        nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "mDNS resolve failed: $errorCode")
            }

            override fun onServiceResolved(info: NsdServiceInfo) {
                val name = info.getAttribute("name")?.let { String(it) }
                    ?: info.serviceName.removePrefix("$SERVICE_NAME_PREFIX-")
                val platform = info.getAttribute("platform")?.let { String(it) } ?: "unknown"
                val caps = info.getAttribute("caps")?.let { String(it) } ?: "file"

                val device = Device(
                    id = "mdns_${info.serviceName}",
                    name = name,
                    platform = platform,
                    capabilities = caps,
                    ipAddress = info.host?.hostAddress ?: "",
                    port = info.port,
                    discoverySource = "mdns",
                    lastSeen = System.currentTimeMillis(),
                    isOnline = true
                )

                onDeviceFound?.invoke(device)
                Log.d(TAG, "mDNS resolved: $name @ ${device.ipAddress}:${device.port}")
            }
        })
    }

    private fun NsdServiceInfo.getAttribute(key: String): ByteArray? {
        return try {
            attributes[key]
        } catch (e: Exception) {
            null
        }
    }

    fun stopDiscovery() {
        if (isDiscovering) {
            try {
                nsdManager?.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.w(TAG, "Stop discovery error: ${e.message}")
            }
            isDiscovering = false
        }
    }

    fun unregisterService() {
        if (isRegistered) {
            try {
                nsdManager?.unregisterService(registrationListener)
            } catch (e: Exception) {
                Log.w(TAG, "Unregister error: ${e.message}")
            }
            isRegistered = false
        }
    }

    fun shutdown() {
        stopDiscovery()
        unregisterService()
    }
}
