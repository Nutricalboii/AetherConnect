package com.aether.connect.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aether.connect.ui.theme.AetherCyan
import com.aether.connect.ui.theme.AetherViolet

@Composable
fun RadarScanner(
    isScanning: Boolean,
    deviceCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing, delayMillis = 700),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension / 2

        // Concentric rings
        for (i in 1..3) {
            val ringRadius = maxRadius * (i / 3f)
            drawCircle(
                color = AetherCyan.copy(alpha = 0.08f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Cross lines
        drawLine(
            color = AetherCyan.copy(alpha = 0.05f),
            start = Offset(center.x, 0f),
            end = Offset(center.x, size.height),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = AetherCyan.copy(alpha = 0.05f),
            start = Offset(0f, center.y),
            end = Offset(size.width, center.y),
            strokeWidth = 1.dp.toPx()
        )

        if (isScanning) {
            // Sweep line
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val endX = center.x + maxRadius * Math.cos(sweepRad).toFloat()
            val endY = center.y + maxRadius * Math.sin(sweepRad).toFloat()
            drawLine(
                color = AetherCyan.copy(alpha = 0.6f),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )

            // Pulse rings
            drawCircle(
                color = AetherCyan.copy(alpha = (1f - pulse1) * 0.3f),
                radius = maxRadius * pulse1,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = AetherViolet.copy(alpha = (1f - pulse2) * 0.2f),
                radius = maxRadius * pulse2,
                center = center,
                style = Stroke(width = 1.5f.dp.toPx())
            )
        }

        // Center dot
        drawCircle(
            color = if (isScanning) AetherCyan else AetherCyan.copy(alpha = 0.3f),
            radius = 6.dp.toPx(),
            center = center
        )
    }
}
