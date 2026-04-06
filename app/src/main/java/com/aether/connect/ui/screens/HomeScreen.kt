package com.aether.connect.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aether.connect.data.model.Device
import com.aether.connect.data.model.Transfer
import com.aether.connect.data.repository.DeviceRepository
import com.aether.connect.data.repository.TransferRepository
import com.aether.connect.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToDevices: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToClipboard: () -> Unit,
    onNavigateToPairing: () -> Unit
) {
    val deviceRepo = remember { DeviceRepository() }
    val transferRepo = remember { TransferRepository() }

    val onlineDevices by deviceRepo.getOnlineDevices().collectAsState(initial = emptyList())
    val activeTransfers by transferRepo.getActiveTransfers().collectAsState(initial = emptyList())
    val onlineCount by deviceRepo.getOnlineCount().collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherDeep)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "AetherConnect",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (onlineCount > 0) "$onlineCount device${if (onlineCount > 1) "s" else ""} nearby"
                    else "Scanning for devices…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AetherTextSecondary
                )
            }
            IconButton(onClick = onNavigateToPairing) {
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = "Pair",
                    tint = AetherCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AetherCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AetherCyan.copy(alpha = 0.08f),
                                AetherViolet.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AetherCyan, AetherViolet))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = null,
                            tint = AetherDeep,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Service Active",
                            style = MaterialTheme.typography.titleMedium,
                            color = AetherTextPrimary
                        )
                        Text(
                            "BLE · mDNS · WiFi Direct",
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions
        Text(
            "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            color = AetherTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickAction(
                icon = Icons.Default.Send,
                label = "Send File",
                color = AetherCyan,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTransfer
            )
            QuickAction(
                icon = Icons.Default.ContentPaste,
                label = "Clipboard",
                color = AetherViolet,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToClipboard
            )
            QuickAction(
                icon = Icons.Default.Nfc,
                label = "NFC Tap",
                color = AetherAmber,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPairing
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nearby Devices Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nearby Devices",
                style = MaterialTheme.typography.titleMedium,
                color = AetherTextPrimary
            )
            TextButton(onClick = onNavigateToDevices) {
                Text("See All", color = AetherCyan)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (onlineDevices.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.RadarOutlined,
                        contentDescription = null,
                        tint = AetherTextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No devices found yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AetherTextMuted
                    )
                    Text(
                        "Make sure other devices are on the same network",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted
                    )
                }
            }
        } else {
            onlineDevices.take(3).forEach { device ->
                DevicePreviewItem(device = device)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Active Transfers
        if (activeTransfers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Active Transfers",
                style = MaterialTheme.typography.titleMedium,
                color = AetherTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            activeTransfers.forEach { transfer ->
                TransferPreviewItem(transfer = transfer)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AetherCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = AetherTextSecondary)
        }
    }
}

@Composable
private fun DevicePreviewItem(device: Device) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AetherCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(StatusOnline)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                device.name,
                style = MaterialTheme.typography.bodyMedium,
                color = AetherTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                device.discoverySource.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AetherTextMuted
            )
        }
    }
}

@Composable
private fun TransferPreviewItem(transfer: Transfer) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AetherCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (transfer.direction == "outgoing") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = StatusTransferring,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transfer.fileName, style = MaterialTheme.typography.bodyMedium, color = AetherTextPrimary)
                Text(transfer.formattedSize(), style = MaterialTheme.typography.bodySmall, color = AetherTextMuted)
            }
            Text(
                "${transfer.progress}%",
                style = MaterialTheme.typography.labelMedium,
                color = AetherCyan
            )
        }
        LinearProgressIndicator(
            progress = { transfer.progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            color = AetherCyan,
            trackColor = AetherBorder
        )
    }
}
