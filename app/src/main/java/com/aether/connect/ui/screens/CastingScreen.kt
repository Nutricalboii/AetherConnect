package com.aether.connect.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aether.connect.service.AetherService
import com.aether.connect.service.CastingService
import com.aether.connect.ui.theme.*
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer

@Composable
fun CastingScreen(service: AetherService, peerId: String) {
    val context = LocalContext.current
    var isCasting by remember { mutableStateOf(false) }
    val eglBaseContext = remember { EglBase.create().eglBaseContext }

    val projectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = Intent(context, CastingService::class.java).apply {
                putExtra(CastingService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(CastingService.EXTRA_DATA, result.data)
            }
            context.startService(intent)
            
            // Start WebRTC capture
            CastingService.screenCapturer?.let { capturer ->
                service.webrtc.startScreenCapture(peerId, capturer)
                isCasting = true
            }
        }
    }

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
                "Screen Cast",
                style = MaterialTheme.typography.headlineLarge,
                color = AetherTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Help */ }) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = AetherTextMuted)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Preview Area / Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AetherSurfaceAlt)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCasting) {
                // Show remote view or local preview
                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            init(eglBaseContext, null)
                            setEnableHardwareScaler(true)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AetherCyan.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ScreenShare,
                            contentDescription = null,
                            tint = AetherCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Ready to start casting",
                        style = MaterialTheme.typography.titleMedium,
                        color = AetherTextSecondary
                    )
                    Text(
                        "Broadcast your screen to another device",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Controls
        Button(
            onClick = {
                if (isCasting) {
                    context.stopService(Intent(context, CastingService::class.java))
                    service.webrtc.stopScreenCapture()
                    isCasting = false
                } else {
                    val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    projectionLauncher.launch(manager.createScreenCaptureIntent())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCasting) AetherRed else AetherCyan
            )
        ) {
            Icon(
                if (isCasting) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                if (isCasting) "Stop Casting" else "Start Casting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
