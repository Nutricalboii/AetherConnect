package com.aether.connect.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.aether.connect.network.InputEvent

/**
 * InputInjectionService — Accessibility Service to inject inputs received from remote device
 * Must be manually enabled by the user in System Settings.
 */
class InputInjectionService : AccessibilityService() {

    companion object {
        private const val TAG = "InputInjectionService"
        var instance: InputInjectionService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "InputInjectionService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: we only use this for injection
    }

    override fun onInterrupt() {
        Log.d(TAG, "InputInjectionService interrupted")
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Injects a touch or gesture based on the received InputEvent
     */
    fun injectInput(event: InputEvent) {
        when (event.type) {
            "TOUCH_DOWN" -> {
                lastX = event.x
                lastY = event.y
                injectGesture(event.x, event.y, false)
            }
            "TOUCH_MOVE" -> {
                if (lastX != 0f && lastY != 0f) {
                    val segments = com.aether.connect.util.GestureInterpolator.interpolatePath(
                        lastX, lastY, event.x, event.y, 20 // 20ms window for smoothness
                    )
                    segments.forEach { seg ->
                        injectGesture(seg.x, seg.y, false, 10)
                    }
                }
                lastX = event.x
                lastY = event.y
            }
            "TOUCH_UP" -> {
                lastX = 0f
                lastY = 0f
            }
            "CLICK" -> {
                injectGesture(event.x, event.y, true)
            }
            "SCROLL" -> {
                injectScroll(event.scrollX, event.scrollY)
            }
            "KEY" -> {
                when (event.keyCode) {
                    4 -> performGlobalAction(GLOBAL_ACTION_BACK)
                    3 -> performGlobalAction(GLOBAL_ACTION_HOME)
                    187 -> performGlobalAction(GLOBAL_ACTION_RECENTS)
                }
            }
        }
    }

    private var lastX: Float = 0f
    private var lastY: Float = 0f

    private fun injectGesture(x: Float, y: Float, isClick: Boolean, duration: Long = 50) {
        val path = Path()
        path.moveTo(x, y)
        
        val builder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(
            path, 0, duration, !isClick // willContinue = true for moves
        )
        builder.addStroke(stroke)
        
        dispatchGesture(builder.build(), null, null)
    }

    private fun injectScroll(dx: Float, dy: Float) {
        // Scrolling is simulated with a quick swipe
        val path = Path()
        val centerX = resources.displayMetrics.widthPixels / 2f
        val centerY = resources.displayMetrics.heightPixels / 2f
        path.moveTo(centerX, centerY)
        path.lineTo(centerX + dx, centerY + dy)
        
        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        dispatchGesture(builder.build(), null, null)
    }
}
