package com.aether.connect.service

import android.app.*
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.aether.connect.util.NotificationUtil
import org.webrtc.ScreenCapturerAndroid

/**
 * CastingService — Foreground service to maintain MediaProjection for screen capture
 */
class CastingService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var projectionManager: MediaProjectionManager? = null

    companion object {
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA = "extra_data"
        
        var screenCapturer: ScreenCapturerAndroid? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = intent?.getParcelableExtra<Intent>(EXTRA_DATA)

        if (resultCode == Activity.RESULT_OK && data != null) {
            startForeground()
            
            mediaProjection = projectionManager?.getMediaProjection(resultCode, data)
            
            screenCapturer = ScreenCapturerAndroid(data, object : MediaProjection.Callback() {
                override fun onStop() {
                    stopSelf()
                }
            })
        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, NotificationUtil.CHANNEL_SERVICE_ID)
            .setContentTitle("Screen Casting")
            .setContentText("AetherConnect is capturing your screen")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationUtil.NOTIFICATION_ID_SERVICE + 1,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NotificationUtil.NOTIFICATION_ID_SERVICE + 1, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaProjection?.stop()
        mediaProjection = null
        screenCapturer = null
        super.onDestroy()
    }
}
