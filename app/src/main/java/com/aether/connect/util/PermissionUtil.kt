package com.aether.connect.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {

    private const val REQUEST_CODE = 1001

    private val BASE_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }

    private val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
        )
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

    private val NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    private val NEARBY_PERMISSIONS = if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
    } else {
        emptyArray()
    }

    fun requestAllPermissions(activity: Activity) {
        val needed = mutableListOf<String>()

        val allPerms = BASE_PERMISSIONS + BLUETOOTH_PERMISSIONS + STORAGE_PERMISSIONS +
                NOTIFICATION_PERMISSIONS + NEARBY_PERMISSIONS

        allPerms.forEach { perm ->
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                needed.add(perm)
            }
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, needed.toTypedArray(), REQUEST_CODE)
        }
    }

    fun hasPermission(activity: Activity, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasBluetoothPermissions(activity: Activity): Boolean {
        return BLUETOOTH_PERMISSIONS.all { hasPermission(activity, it) }
    }

    fun hasLocationPermission(activity: Activity): Boolean {
        return hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun hasStoragePermissions(activity: Activity): Boolean {
        return STORAGE_PERMISSIONS.all { hasPermission(activity, it) }
    }
}
