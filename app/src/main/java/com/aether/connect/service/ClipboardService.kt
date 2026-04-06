package com.aether.connect.service

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import com.aether.connect.data.repository.ClipboardRepository
import com.aether.connect.util.CryptoUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ClipboardService — Monitors system clipboard and syncs across devices
 */
class ClipboardService(
    private val context: Context,
    private val scope: CoroutineScope,
    private val deviceId: String
) {
    companion object {
        private const val TAG = "ClipboardService"
    }

    private val clipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    private val repository = ClipboardRepository()
    private var lastHash: String = ""

    var onClipboardChanged: ((String, String) -> Unit)? = null // content, contentType

    fun startMonitoring() {
        // Register system clipboard listener
        clipboardManager.addPrimaryClipChangedListener {
            handleClipboardChange()
        }
        Log.d(TAG, "Clipboard monitoring started")
    }

    private fun handleClipboardChange() {
        try {
            val clip = clipboardManager.primaryClip ?: return
            if (clip.itemCount == 0) return

            val item = clip.getItemAt(0)
            val text = item.coerceToText(context)?.toString() ?: return

            if (text.isBlank() || text.length > 5 * 1024 * 1024) return // Skip empty or >5MB

            val hash = CryptoUtil.sha256Short(text)
            if (hash == lastHash) return // Dedup
            lastHash = hash

            // Detect content type
            val contentType = when {
                text.startsWith("http://") || text.startsWith("https://") -> "url"
                text.startsWith("data:image") -> "image"
                else -> "text"
            }

            // Save locally
            scope.launch {
                val isNew = repository.addEntry(
                    content = text,
                    contentType = contentType,
                    sourceDeviceId = deviceId,
                    sourceDeviceName = "This Device",
                    isLocal = true
                )

                if (isNew) {
                    onClipboardChanged?.invoke(text, contentType)
                    Log.d(TAG, "Clipboard captured: $contentType (${text.length} chars)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Clipboard read error: ${e.message}")
        }
    }

    /**
     * Called when receiving clipboard from a remote device
     */
    fun onRemoteClipboard(content: String, contentType: String, sourceDeviceId: String, sourceDeviceName: String) {
        scope.launch {
            val hash = CryptoUtil.sha256Short(content)
            if (hash == lastHash) return@launch // Prevent echo
            lastHash = hash

            // Save to history
            repository.addEntry(
                content = content,
                contentType = contentType,
                sourceDeviceId = sourceDeviceId,
                sourceDeviceName = sourceDeviceName,
                isLocal = false
            )

            // Write to system clipboard
            try {
                val clip = android.content.ClipData.newPlainText("AetherConnect", content)
                clipboardManager.setPrimaryClip(clip)
                Log.d(TAG, "Remote clipboard applied: $contentType from $sourceDeviceName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set clipboard: ${e.message}")
            }
        }
    }
}
