package com.aether.connect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aether.connect.data.model.ClipboardEntry
import com.aether.connect.data.repository.ClipboardRepository
import com.aether.connect.ui.theme.*

@Composable
fun ClipboardScreen() {
    val repository = remember { ClipboardRepository() }
    val entries by repository.getRecentEntries().collectAsState(initial = emptyList())
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Clipboard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Synced across all paired devices",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextMuted
                )
            }

            // Sync indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AetherGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AetherGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Syncing",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherGreen
                )
            }
        }

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = AetherTextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No clipboard entries yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AetherTextMuted
                    )
                    Text(
                        "Copy something to start syncing",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    ClipboardEntryCard(
                        entry = entry,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(entry.content))
                            showCopiedSnackbar = true
                        },
                        onDelete = {
                            // TODO: delete entry
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        if (showCopiedSnackbar) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showCopiedSnackbar = false
            }
        }
    }
}

@Composable
private fun ClipboardEntryCard(
    entry: ClipboardEntry,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AetherCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Type icon
                Icon(
                    when (entry.contentType) {
                        "url" -> Icons.Default.Link
                        "image" -> Icons.Default.Image
                        else -> Icons.Default.TextSnippet
                    },
                    contentDescription = null,
                    tint = when (entry.contentType) {
                        "url" -> AetherBlue
                        "image" -> AetherAmber
                        else -> AetherViolet
                    },
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Source
                Text(
                    if (entry.isLocal) "This Device" else entry.sourceDeviceName,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted
                )

                Spacer(modifier = Modifier.weight(1f))

                // Time
                Text(
                    formatRelativeTime(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content preview
            Text(
                entry.preview(200),
                style = MaterialTheme.typography.bodyMedium,
                color = AetherTextPrimary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Actions
            Row {
                AssistChip(
                    onClick = onCopy,
                    label = { Text("Copy", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = AetherCyan.copy(alpha = 0.1f),
                        labelColor = AetherCyan,
                        leadingIconContentColor = AetherCyan
                    ),
                    border = null
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}
