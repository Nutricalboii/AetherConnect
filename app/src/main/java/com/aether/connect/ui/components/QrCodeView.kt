package com.aether.connect.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.aether.connect.ui.theme.AetherTextPrimary
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Int = 200
) {
    var bitmap by remember(data) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(data) {
        bitmap = generateQrCode(data, size)
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AetherTextPrimary),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize()
            )
        }
    }
}

private fun generateQrCode(data: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size * 3, size * 3)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
