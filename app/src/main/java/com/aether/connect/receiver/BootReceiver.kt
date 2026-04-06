package com.aether.connect.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aether.connect.service.AetherService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Boot completed — starting AetherService")
            val serviceIntent = Intent(context, AetherService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
