package com.aether.connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aether.connect.nfc.NFCPayload
import com.aether.connect.nfc.NFCReader
import com.aether.connect.ui.components.QrCodeView
import com.aether.connect.ui.theme.*
import com.aether.connect.util.NetworkUtil

@Composable
fun PairingScreen() {
    val context = LocalContext.current
    val ip = remember { NetworkUtil.getLocalIpAddress() ?: "0.0.0.0" }
    val deviceName = remember { NetworkUtil.getDeviceName() }
    val nfcAvailable = remember { NFCReader.isNfcAvailable(context) }
    val nfcEnabled = remember { NFCReader.isNfcEnabled(context) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val pairingData = remember {
        NFCPayload.createPairPayload(
            deviceId = "aether_local",
            deviceName = deviceName,
            publicKey = "",
            ip = ip,
            port = 8888
        ).toJson()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherDeep)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Pair Device",
            style = MaterialTheme.typography.headlineLarge,
            color = AetherTextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Connect a new device to AetherConnect",
            style = MaterialTheme.typography.bodySmall,
            color = AetherTextMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AetherSurface,
            contentColor = AetherCyan
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("QR Code")
                }
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NFC")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> QRPairingTab(pairingData, ip)
            1 -> NFCPairingTab(nfcAvailable, nfcEnabled, deviceName)
        }
    }
}

@Composable
private fun QRPairingTab(pairingData: String, ip: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AetherCard)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QrCodeView(data = pairingData, size = 220)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Scan this code with another device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AetherTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AetherSurfaceAlt
                ) {
                    Text(
                        ip,
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scan button
        OutlinedButton(
            onClick = { /* TODO: open camera QR scanner */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AetherCyan)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Another Device's QR")
        }
    }
}

@Composable
private fun NFCPairingTab(nfcAvailable: Boolean, nfcEnabled: Boolean, deviceName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AetherCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!nfcAvailable) {
                    Icon(
                        Icons.Default.NfcOutlined,
                        contentDescription = null,
                        tint = AetherRed,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "NFC Not Available",
                        style = MaterialTheme.typography.titleMedium,
                        color = AetherTextPrimary
                    )
                    Text(
                        "This device does not support NFC",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted,
                        textAlign = TextAlign.Center
                    )
                } else if (!nfcEnabled) {
                    Icon(
                        Icons.Default.NfcOutlined,
                        contentDescription = null,
                        tint = AetherAmber,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "NFC is Disabled",
                        style = MaterialTheme.typography.titleMedium,
                        color = AetherTextPrimary
                    )
                    Text(
                        "Enable NFC in system settings to use tap-to-pair",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // NFC ready
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(AetherCyan.copy(alpha = 0.2f), AetherDeep)
                                ),
                                shape = RoundedCornerShape(50.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = AetherCyan,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Ready to Tap",
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Hold your device near another AetherConnect device to pair instantly",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AetherTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AetherSurfaceAlt
                    ) {
                        Text(
                            "Broadcasting as: $deviceName",
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (nfcAvailable && nfcEnabled) {
            Spacer(modifier = Modifier.height(20.dp))

            // NFC modes
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    NfcModeItem("Tap to Pair", "Touch devices to pair instantly", Icons.Default.Handshake, AetherCyan)
                    HorizontalDivider(color = AetherBorder, modifier = Modifier.padding(vertical = 8.dp))
                    NfcModeItem("Tap to Send", "Select a file, then tap to transfer", Icons.Default.Send, AetherViolet)
                    HorizontalDivider(color = AetherBorder, modifier = Modifier.padding(vertical = 8.dp))
                    NfcModeItem("Tap to Mirror", "Coming in V2", Icons.Default.ScreenShare, AetherTextMuted)
                }
            }
        }
    }
}

@Composable
private fun NfcModeItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = AetherTextPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = AetherTextMuted)
        }
    }
}
