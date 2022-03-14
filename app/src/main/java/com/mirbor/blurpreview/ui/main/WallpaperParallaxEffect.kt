/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Grishka, 2013-2019.
 */
package com.mirbor.blurpreview.ui.main

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.view.WindowManager
import android.hardware.SensorManager
import android.hardware.SensorEvent
import android.view.Surface
import com.mirbor.blurpreview.AndroidUtils.dp
import com.mirbor.blurpreview.AndroidUtils.dpf

class WallpaperParallaxEffect(context: Context) : SensorEventListener {
    private val rollBuffer = FloatArray(3)
    private val pitchBuffer = FloatArray(3)
    private var bufferOffset = 0
    private val wm: WindowManager
    private val sensorManager: SensorManager
    private val accelerometer: Sensor?
    private var enabled = false
    private var callback: Callback? = null
    fun setEnabled(enabled: Boolean) {
        if (this.enabled != enabled) {
            this.enabled = enabled
            if (accelerometer == null) return
            if (enabled) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            } else {
                sensorManager.unregisterListener(this)
            }
        }
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    fun getScale(boundsWidth: Int, boundsHeight: Int): Float {
        val offset = 16.dp
        return Math.max(
            (boundsWidth.toFloat() + offset * 2) / boundsWidth.toFloat(),
            (boundsHeight.toFloat() + offset * 2) / boundsHeight.toFloat()
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotation = wm.defaultDisplay.rotation
        val x = event.values[0] / SensorManager.GRAVITY_EARTH
        val y = event.values[1] / SensorManager.GRAVITY_EARTH
        val z = event.values[2] / SensorManager.GRAVITY_EARTH
        var pitch = (Math.atan2(
            x.toDouble(),
            Math.sqrt((y * y + z * z).toDouble())
        ) / Math.PI * 2.0).toFloat()
        var roll = (Math.atan2(
            y.toDouble(),
            Math.sqrt((x * x + z * z).toDouble())
        ) / Math.PI * 2.0).toFloat()
        when (rotation) {
            Surface.ROTATION_0 -> {}
            Surface.ROTATION_90 -> {
                val tmp = pitch
                pitch = roll
                roll = tmp
            }
            Surface.ROTATION_180 -> {
                roll = -roll
                pitch = -pitch
            }
            Surface.ROTATION_270 -> {
                val tmp = -pitch
                pitch = roll
                roll = tmp
            }
        }
        rollBuffer[bufferOffset] = roll
        pitchBuffer[bufferOffset] = pitch
        bufferOffset = (bufferOffset + 1) % rollBuffer.size
        pitch = 0f
        roll = pitch
        for (i in rollBuffer.indices) {
            roll += rollBuffer[i]
            pitch += pitchBuffer[i]
        }
        roll /= rollBuffer.size.toFloat()
        pitch /= rollBuffer.size.toFloat()
        if (roll > 1f) {
            roll = 2f - roll
        } else if (roll < -1f) {
            roll = -2f - roll
        }
        val offsetX = Math.round(pitch * 16.dpf)
        val offsetY = Math.round(roll * 16.dpf)
        var vx = Math.max(-1.0f, Math.min(1.0f, -pitch / 0.45f))
        var vy = Math.max(-1.0f, Math.min(1.0f, -roll / 0.45f))
        val len = Math.sqrt((vx * vx + vy * vy).toDouble()).toFloat()
        vx /= len
        vy /= len
        val y2 = -1f
        val x2 = 0f
        var angle = (Math.atan2(
            (vx * y2 - vy * x2).toDouble(),
            (vx * x2 + vy * y2).toDouble()
        ) / (Math.PI / 180.0f)).toFloat()
        if (angle < 0) {
            angle += 360f
        }
        if (callback != null) {
            callback!!.onOffsetsChanged(offsetX, offsetY, angle)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    interface Callback {
        fun onOffsetsChanged(offsetX: Int, offsetY: Int, angle: Float)
    }

    init {
        wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
}