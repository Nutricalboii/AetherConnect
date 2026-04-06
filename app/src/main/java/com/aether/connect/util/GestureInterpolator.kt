package com.aether.connect.util

import android.graphics.Path
import android.graphics.PathMeasure
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import com.aether.connect.network.InputEvent

/**
 * GestureInterpolator — Calculates smooth paths and velocity curves for remote input injection.
 * Translates raw coordinates into timed StrokeDescription points.
 */
object GestureInterpolator {

    /**
     * Smoothes a sequence of points into a natural-feeling curve using cubic easing.
     * @return A list of (x, y, duration) segments to be dispatched.
     */
    fun interpolatePath(fromX: Float, fromY: Float, toX: Float, toY: Float, durationMs: Long): List<PointDelta> {
        val path = Path().apply {
            moveTo(fromX, fromY)
            lineTo(toX, toY)
        }
        
        val pm = PathMeasure(path, false)
        val length = pm.length
        val segments = 10 // Divide into 10 steps for smoothness
        val results = mutableListOf<PointDelta>()
        
        for (i in 1..segments) {
            val progress = i.toFloat() / segments
            val easedProgress = cubicEaseInOut(progress)
            
            val pos = FloatArray(2)
            pm.getPosTan(length * easedProgress, pos, null)
            
            results.add(PointDelta(pos[0], pos[1], durationMs / segments))
        }
        
        return results
    }

    /**
     * Cubic Easing function to simulate inertia (Acceleration/Deceleration)
     */
    private fun cubicEaseInOut(t: Float): Float {
        return if (t < 0.5f) {
            4f * t * t * t
        } else {
            (t - 1) * (2 * t - 2) * (2 * t - 2) + 1
        }
    }

    data class PointDelta(val x: Float, val y: Float, val duration: Long)
}
