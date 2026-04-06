package com.aether.connect.service

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * AetherHCEService — Host Card Emulation for NFC Handshake
 * Emulates an NFC tag that transmits the device's public pairing token.
 * This allows "Tap-to-Connect" even when the app is in the background.
 */
class AetherHCEService : HostApduService() {

    companion object {
        private const val TAG = "AetherHCEService"
        
        // AID for AetherConnect NFC Protocol (Must match manifest)
        private val AID = byteArrayOf(0xA0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte())
        
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            AID.size.toByte(), *AID
        )
        
        private val SUCCESS_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val FAILURE_SW = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return FAILURE_SW
        
        // Check if this is the SELECT AID command
        if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.d(TAG, "AetherConnect NFC Handshake initiated")
            
            // Return device ID and pairing intent
            val payload = "AETHER_CONNECT_V3:HANDSHAKE_TOKEN".toByteArray()
            return payload + SUCCESS_SW
        }
        
        return FAILURE_SW
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "AetherConnect NFC Handshake finished: $reason")
    }
}
