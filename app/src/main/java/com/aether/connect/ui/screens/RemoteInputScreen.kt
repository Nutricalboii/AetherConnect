package com.aether.connect.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aether.connect.network.InputEvent
import com.aether.connect.service.AetherService
import com.aether.connect.ui.theme.*

@Composable
fun RemoteInputScreen(service: AetherService, peerId: String) {
    val context = LocalContext.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AetherDeep)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Remote Control",
                style = MaterialTheme.typography.headlineLarge,
                color = AetherTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { isKeyboardVisible = !isKeyboardVisible }) {
                Icon(
                    if (isKeyboardVisible) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = if (isKeyboardVisible) AetherCyan else AetherTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Virtual Trackpad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(AetherSurfaceAlt)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            service.sendInputEvent(peerId, InputEvent("TOUCH_DOWN", offset.x, offset.y))
                        },
                        onDrag = { change, dragAmount ->
                            service.sendInputEvent(peerId, InputEvent("TOUCH_MOVE", change.position.x, change.position.y))
                            change.consume()
                        },
                        onDragEnd = {
                            service.sendInputEvent(peerId, InputEvent("TOUCH_UP"))
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = AetherTextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Trackpad Active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AetherTextMuted.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ControlActionButton(Icons.Default.ArrowBack, "Back", AetherViolet) {
                service.sendInputEvent(peerId, InputEvent("KEY", keyCode = 4))
            }
            ControlActionButton(Icons.Default.Home, "Home", AetherCyan) {
                service.sendInputEvent(peerId, InputEvent("KEY", keyCode = 3))
            }
            ControlActionButton(Icons.Default.Layers, "Recents", AetherAmber) {
                service.sendInputEvent(peerId, InputEvent("KEY", keyCode = 187))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ControlActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            onClick = onClick,
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = AetherCard)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = AetherTextMuted)
    }
}
