package com.aether.connect.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aether.connect.data.model.Device
import com.aether.connect.data.model.Transfer
import com.aether.connect.data.repository.DeviceRepository
import com.aether.connect.data.repository.TransferRepository
import com.aether.connect.ui.components.TransferProgress
import com.aether.connect.ui.theme.*

@Composable
fun TransferScreen() {
    val deviceRepo = remember { DeviceRepository() }
    val transferRepo = remember { TransferRepository() }
    val pairedDevices by deviceRepo.getPairedDevices().collectAsState(initial = emptyList())
    val recentTransfers by transferRepo.getAllTransfers().collectAsState(initial = emptyList())
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedFiles = uris
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherDeep)
    ) {
        // Header
        Text(
            "File Transfer",
            style = MaterialTheme.typography.headlineLarge,
            color = AetherTextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Select Device
            item {
                Text("1. Select Device", style = MaterialTheme.typography.titleMedium, color = AetherTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                if (pairedDevices.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AetherCard)
                    ) {
                        Text(
                            "No paired devices. Go to Devices tab to discover and pair.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AetherTextMuted,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pairedDevices.take(4).forEach { device ->
                            val isSelected = selectedDevice?.id == device.id
                            Card(
                                onClick = { selectedDevice = device },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AetherCyan.copy(alpha = 0.12f) else AetherCard
                                ),
                                border = if (isSelected) {
                                    CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(listOf(AetherCyan, AetherViolet))
                                    )
                                } else null,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (device.isOnline) StatusOnline else StatusOffline)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        device.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AetherTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Select Files
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("2. Select Files", style = MaterialTheme.typography.titleMedium, color = AetherTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = { filePicker.launch(arrayOf("*/*")) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(AetherCyan.copy(alpha = 0.3f), AetherViolet.copy(alpha = 0.3f))),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = "Pick files",
                                tint = AetherCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (selectedFiles.isEmpty()) "Tap to select files"
                                else "${selectedFiles.size} file${if (selectedFiles.size > 1) "s" else ""} selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AetherTextSecondary
                            )
                        }
                    }
                }
            }

            // Send Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* TODO: initiate transfer */ },
                    enabled = selectedDevice != null && selectedFiles.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AetherCyan,
                        disabledContainerColor = AetherCard
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Send",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Transfer History
            if (recentTransfers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Recent Transfers", style = MaterialTheme.typography.titleMedium, color = AetherTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(recentTransfers.take(10), key = { it.id }) { transfer ->
                    TransferHistoryItem(transfer)
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun TransferHistoryItem(transfer: Transfer) {
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
            // Direction icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (transfer.status) {
                            "completed" -> AetherGreen.copy(alpha = 0.15f)
                            "failed" -> AetherRed.copy(alpha = 0.15f)
                            else -> AetherBlue.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (transfer.status) {
                        "completed" -> Icons.Default.Check
                        "failed" -> Icons.Default.Close
                        else -> if (transfer.direction == "outgoing") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                    },
                    contentDescription = null,
                    tint = when (transfer.status) {
                        "completed" -> AetherGreen
                        "failed" -> AetherRed
                        else -> AetherBlue
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transfer.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AetherTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${transfer.formattedSize()} · ${transfer.deviceName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextMuted
                )
            }

            if (transfer.isActive) {
                TransferProgress(progress = transfer.progress / 100f, size = 32)
            }
        }
    }
}
