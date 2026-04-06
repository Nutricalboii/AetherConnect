package com.aether.connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aether.connect.data.model.Device
import com.aether.connect.data.repository.DeviceRepository
import com.aether.connect.ui.components.DeviceCard
import com.aether.connect.ui.components.RadarScanner
import com.aether.connect.ui.theme.*

@Composable
fun DevicesScreen(
    onDeviceClick: (Device) -> Unit = {}
) {
    val deviceRepo = remember { DeviceRepository() }
    val allDevices by deviceRepo.getAllDevices().collectAsState(initial = emptyList())
    val onlineDevices = allDevices.filter { it.isOnline }
    val offlineDevices = allDevices.filter { !it.isOnline }
    var isScanning by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherDeep)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Devices",
                style = MaterialTheme.typography.headlineLarge,
                color = AetherTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isScanning = !isScanning }) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = if (isScanning) AetherCyan else AetherTextMuted
                )
            }
        }

        // Radar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            RadarScanner(
                isScanning = isScanning,
                deviceCount = onlineDevices.size
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Device count
        Text(
            "${onlineDevices.size} online · ${offlineDevices.size} remembered",
            style = MaterialTheme.typography.bodySmall,
            color = AetherTextMuted,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Device list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onlineDevices.isNotEmpty()) {
                item {
                    Text(
                        "ONLINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = StatusOnline,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(onlineDevices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device) }
                    )
                }
            }

            if (offlineDevices.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "REMEMBERED",
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherTextMuted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(offlineDevices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}
