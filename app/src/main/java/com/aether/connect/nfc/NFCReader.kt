package com.aether.connect.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log

/**
 * NFCReader — Reads NDEF tags for AetherConnect pairing and file transfer initiation
 */
object NFCReader {

    private const val TAG = "NFCReader"

    var onPayloadReceived: ((NFCPayload) -> Unit)? = null

    fun enableForegroundDispatch(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return

        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            activity, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val filters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                try {
                    addDataType(NFCPayload.MIME_TYPE)
                } catch (e: IntentFilter.MalformedMimeTypeException) {
                    Log.e(TAG, "Malformed MIME type", e)
                }
            },
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )

        val techLists = arrayOf(arrayOf(Ndef::class.java.name))

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
        Log.d(TAG, "NFC foreground dispatch enabled")
    }

    fun disableForegroundDispatch(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        adapter.disableForegroundDispatch(activity)
    }

    fun handleTag(context: Context, tag: Tag, intent: Intent) {
        Log.d(TAG, "NFC tag detected")

        // Try reading NDEF messages
        val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (messages != null) {
            for (msg in messages) {
                val ndefMessage = msg as? NdefMessage ?: continue
                for (record in ndefMessage.records) {
                    processRecord(record)
                }
            }
            return
        }

        // Try reading from tag directly
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val message = ndef.ndefMessage
                message?.records?.forEach { record ->
                    processRecord(record)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read NFC tag: ${e.message}")
            } finally {
                try { ndef.close() } catch (e: Exception) {}
            }
        }
    }

    private fun processRecord(record: NdefRecord) {
        when (record.tnf) {
            NdefRecord.TNF_MIME_MEDIA -> {
                val mimeType = String(record.type)
                if (mimeType == NFCPayload.MIME_TYPE) {
                    val payload = NFCPayload.fromBytes(record.payload)
                    if (payload != null) {
                        Log.d(TAG, "AetherConnect NFC payload: ${payload.action} from ${payload.deviceName}")
                        onPayloadReceived?.invoke(payload)
                    }
                }
            }
            NdefRecord.TNF_WELL_KNOWN -> {
                // Handle standard text/URI records if needed
                val payload = com.aether.connect.nfc.NFCPayload.fromJson(payloadStr)
                if (payload != null) {
                    onPayloadReceived?.invoke(payload)
                }
            }
            NdefRecord.TNF_EXTERNAL_TYPE -> {
                // V3: Handle HCE / External handshakes
                val type = String(record.type)
                if (type == "aether:handshake") {
                    val payload = com.aether.connect.nfc.NFCPayload(
                        "HANDSHAKE", "Aether Device", android.os.Build.MODEL, "127.0.0.1", 8888
                    )
                    onPayloadReceived?.invoke(payload)
                }
            }
        }
    }

    fun isNfcAvailable(context: Context): Boolean {
        return NfcAdapter.getDefaultAdapter(context) != null
    }

    fun isNfcEnabled(context: Context): Boolean {
        return NfcAdapter.getDefaultAdapter(context)?.isEnabled == true
    }
}
