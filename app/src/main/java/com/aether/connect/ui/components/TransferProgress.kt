package com.aether.connect.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aether.connect.ui.theme.AetherCyan
import com.aether.connect.ui.theme.AetherViolet

@Composable
fun TransferProgress(
    progress: Float,  // 0f to 1f
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300, easing = LinearEasing),
        label = "progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier.size(size.dp)) {
        val strokeWidth = 6.dp.toPx()
        val radius = (this.size.minDimension - strokeWidth) / 2
        val center = Offset(this.size.width / 2, this.size.height / 2)

        // Background ring
        drawCircle(
            color = AetherCyan.copy(alpha = 0.1f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Progress arc with gradient
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(AetherCyan, AetherViolet, AetherCyan),
                center = center
            ),
            startAngle = -90f,
            sweepAngle = animatedProgress * 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Glow dot at progress tip
        if (animatedProgress > 0f && animatedProgress < 1f) {
            val angle = Math.toRadians((-90.0 + animatedProgress * 360.0))
            val tipX = center.x + radius * Math.cos(angle).toFloat()
            val tipY = center.y + radius * Math.sin(angle).toFloat()

            drawCircle(
                color = AetherCyan.copy(alpha = glowAlpha),
                radius = strokeWidth * 1.5f,
                center = Offset(tipX, tipY)
            )
        }
    }
}
