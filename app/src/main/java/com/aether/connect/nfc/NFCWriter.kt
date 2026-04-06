package com.aether.connect.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log

/**
 * NFCWriter — Writes AetherConnect pairing/transfer data to NFC tags
 */
object NFCWriter {

    private const val TAG = "NFCWriter"

    fun writePayload(tag: Tag, payload: NFCPayload): Boolean {
        val record = NdefRecord.createMime(
            NFCPayload.MIME_TYPE,
            payload.toNdefBytes()
        )
        val message = NdefMessage(arrayOf(record))

        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                writeToNdef(ndef, message)
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatAndWrite(formatable, message)
                } else {
                    Log.e(TAG, "Tag is not NDEF compatible")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "NFC write failed: ${e.message}")
            false
        }
    }

    private fun writeToNdef(ndef: Ndef, message: NdefMessage): Boolean {
        return try {
            ndef.connect()
            if (!ndef.isWritable) {
                Log.e(TAG, "Tag is read-only")
                return false
            }
            if (ndef.maxSize < message.toByteArray().size) {
                Log.e(TAG, "Tag too small: ${ndef.maxSize} < ${message.toByteArray().size}")
                return false
            }
            ndef.writeNdefMessage(message)
            Log.d(TAG, "NFC write successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "NDEF write error: ${e.message}")
            false
        } finally {
            try { ndef.close() } catch (e: Exception) {}
        }
    }

    private fun formatAndWrite(formatable: NdefFormatable, message: NdefMessage): Boolean {
        return try {
            formatable.connect()
            formatable.format(message)
            Log.d(TAG, "NFC format + write successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "NDEF format error: ${e.message}")
            false
        } finally {
            try { formatable.close() } catch (e: Exception) {}
        }
    }

    /**
     * Create an NDEF message for Android Beam (deprecated but still functional on older devices)
     */
    fun createBeamMessage(payload: NFCPayload): NdefMessage {
        val record = NdefRecord.createMime(
            NFCPayload.MIME_TYPE,
            payload.toNdefBytes()
        )
        return NdefMessage(arrayOf(record))
    }
}
