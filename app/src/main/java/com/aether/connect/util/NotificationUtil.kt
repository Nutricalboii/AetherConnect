package com.aether.connect.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aether.connect.MainActivity
import com.aether.connect.R

object NotificationUtil {
    const val CHANNEL_SERVICE = "aether_service"
    const val CHANNEL_TRANSFER = "aether_transfer"
    const val CHANNEL_CLIPBOARD = "aether_clipboard"
    const val CHANNEL_DEVICE = "aether_device"

    fun createServiceNotification(context: Context, connectedCount: Int): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtitle = if (connectedCount > 0) {
            "$connectedCount device${if (connectedCount > 1) "s" else ""} nearby"
        } else {
            "Scanning for devices…"
        }

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setContentTitle("AetherConnect")
            .setContentText(subtitle)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun showTransferNotification(context: Context, id: Int, fileName: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_TRANSFER)
            .setContentTitle("Transferring: $fileName")
            .setProgress(100, progress, false)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(id, notification)
    }

    fun showTransferComplete(context: Context, id: Int, fileName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_TRANSFER)
            .setContentTitle("Transfer complete")
            .setContentText(fileName)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(id, notification)
    }

    fun showDeviceNotification(context: Context, id: Int, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DEVICE)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(id, notification)
    }

    fun updateNotification(context: Context, id: Int, notification: Notification) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(id, notification)
    }

    fun cancel(context: Context, id: Int) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.cancel(id)
    }
}
