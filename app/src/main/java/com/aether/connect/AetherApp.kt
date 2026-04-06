package com.aether.connect

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.aether.connect.data.db.AetherDatabase
import com.aether.connect.util.NotificationUtil

class AetherApp : Application() {

    lateinit var database: AetherDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Room database
        database = AetherDatabase.getInstance(this)

        // Create notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return

        val channels = listOf(
            NotificationChannel(
                NotificationUtil.CHANNEL_SERVICE,
                "AetherConnect Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for device connectivity"
                setShowBadge(false)
            },
            NotificationChannel(
                NotificationUtil.CHANNEL_TRANSFER,
                "File Transfers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "File transfer progress and completion"
                enableVibration(true)
            },
            NotificationChannel(
                NotificationUtil.CHANNEL_CLIPBOARD,
                "Clipboard Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Clipboard synchronization notifications"
                setShowBadge(false)
            },
            NotificationChannel(
                NotificationUtil.CHANNEL_DEVICE,
                "Device Events",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Device connection and pairing events"
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    companion object {
        lateinit var instance: AetherApp
            private set
    }
}
