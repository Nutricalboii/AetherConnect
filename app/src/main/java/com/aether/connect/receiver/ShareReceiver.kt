package com.aether.connect.receiver

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aether.connect.data.model.Device
import com.aether.connect.data.repository.DeviceRepository
import com.aether.connect.ui.theme.AetherConnectTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ShareReceiver — Handles Android share sheet intents
 * Shows device picker and initiates file transfer
 */
class ShareReceiver : ComponentActivity() {

    companion object {
        private const val TAG = "ShareReceiver"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedUris = getSharedUris(intent)
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        Log.d(TAG, "Share received: ${sharedUris.size} files, text=${sharedText != null}")

        setContent {
            AetherConnectTheme {
                SharePickerUI(
                    uris = sharedUris,
                    text = sharedText,
                    onDeviceSelected = { device ->
                        initiateTransfer(device, sharedUris, sharedText)
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun getSharedUris(intent: Intent): List<Uri> {
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                listOfNotNull(uri)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()
            }
            else -> emptyList()
        }
    }

    private fun initiateTransfer(device: Device, uris: List<Uri>, text: String?) {
        Log.d(TAG, "Transfer to ${device.name}: ${uris.size} files")
        // TODO: Connect to AetherService and initiate transfer
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePickerUI(
    uris: List<Uri>,
    text: String?,
    onDeviceSelected: (Device) -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }

    LaunchedEffect(Unit) {
        val repo = DeviceRepository()
        devices = repo.getPairedDevices().first()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Send via AetherConnect",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${uris.size} file${if (uris.size != 1) "s" else ""}${if (text != null) " + text" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (devices.isEmpty()) {
                Text(
                    "No paired devices found.\nPair a device first in AetherConnect.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                devices.forEach { device ->
                    Card(
                        onClick = { onDeviceSelected(device) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    device.platform,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (device.isOnline) {
                                Badge { Text("Online") }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    }
}
