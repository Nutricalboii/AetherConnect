package com.aether.connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aether.connect.ui.theme.*
import com.aether.connect.util.NetworkUtil

@Composable
fun SettingsScreen() {
    var kdeBridgeEnabled by remember { mutableStateOf(false) }
    var autoStart by remember { mutableStateOf(true) }
    var clipboardSync by remember { mutableStateOf(true) }
    var nfcEnabled by remember { mutableStateOf(true) }
    var bleDiscovery by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = AetherTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Device Info
        SettingsSection("Device Info") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Device", NetworkUtil.getDeviceName())
                    InfoRow("Platform", NetworkUtil.getAndroidVersion())
                    InfoRow("IP Address", NetworkUtil.getLocalIpAddress() ?: "Not connected")
                    InfoRow("Port", "8888")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Discovery
        SettingsSection("Discovery") {
            SettingsToggle(
                icon = Icons.Default.Bluetooth,
                title = "BLE Discovery",
                subtitle = "Discover nearby devices via Bluetooth Low Energy",
                checked = bleDiscovery,
                onCheckedChange = { bleDiscovery = it }
            )
            SettingsToggle(
                icon = Icons.Default.Nfc,
                title = "NFC",
                subtitle = "Enable tap-to-pair and tap-to-send",
                checked = nfcEnabled,
                onCheckedChange = { nfcEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sync
        SettingsSection("Sync") {
            SettingsToggle(
                icon = Icons.Default.ContentPaste,
                title = "Clipboard Sync",
                subtitle = "Auto-sync clipboard with paired devices",
                checked = clipboardSync,
                onCheckedChange = { clipboardSync = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System
        SettingsSection("System") {
            SettingsToggle(
                icon = Icons.Default.PowerSettingsNew,
                title = "Auto-Start on Boot",
                subtitle = "Start AetherConnect when device boots",
                checked = autoStart,
                onCheckedChange = { autoStart = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Integrations
        SettingsSection("Integrations") {
            SettingsToggle(
                icon = Icons.Default.Extension,
                title = "KDE Connect Bridge",
                subtitle = "Interop with KDE Connect devices on the network",
                checked = kdeBridgeEnabled,
                onCheckedChange = { kdeBridgeEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // About
        SettingsSection("About") {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Version", "1.0.0")
                    InfoRow("Package", "com.aether.connect")
                    InfoRow("License", "MIT")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Open-source cross-device ecosystem",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = AetherTextMuted,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    content()
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AetherCard),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) AetherCyan else AetherTextMuted,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = AetherTextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AetherTextMuted)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AetherCyan,
                    checkedTrackColor = AetherCyan.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AetherTextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AetherTextPrimary)
    }
}
