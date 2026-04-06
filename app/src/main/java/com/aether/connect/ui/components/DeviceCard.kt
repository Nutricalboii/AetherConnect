package com.aether.connect.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aether.connect.data.model.Device
import com.aether.connect.ui.theme.*

@Composable
fun DeviceCard(
    device: Device,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        if (device.isOnline) StatusOnline else StatusOffline,
        label = "status"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AetherCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Platform icon with gradient background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(AetherCyan.copy(alpha = 0.2f), AetherViolet.copy(alpha = 0.2f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = platformIcon(device.platform),
                    contentDescription = device.platform,
                    tint = AetherCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Device info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (device.isOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted
                    )
                    if (device.ipAddress.isNotEmpty()) {
                        Text(
                            text = " · ${device.ipAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextMuted
                        )
                    }
                }
            }

            // Discovery source badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AetherSurfaceAlt
            ) {
                Text(
                    text = device.discoverySource.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

fun platformIcon(platform: String): ImageVector {
    return when (platform.lowercase()) {
        "android" -> Icons.Default.PhoneAndroid
        "ios" -> Icons.Default.PhoneIphone
        "windows" -> Icons.Default.DesktopWindows
        "macos" -> Icons.Default.LaptopMac
        "linux" -> Icons.Default.Computer
        else -> Icons.Default.DeviceUnknown
    }
}
