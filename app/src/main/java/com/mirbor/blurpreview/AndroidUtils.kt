package com.mirbor.blurpreview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.widget.FrameLayout

object AndroidUtils {
    external fun generateGradient(
        bitmap: Bitmap?,
        unpin: Boolean,
        phase: Int,
        progress: Float,
        width: Int,
        height: Int,
        stride: Int,
        colors: IntArray?
    )

    fun getStatusBarHeight(): Int {
        val resourceId = App.appContext!!.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) App.appContext!!.resources.getDimensionPixelSize(resourceId) else 0
    }

    val rectTmp2 = Rect()

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Int.dpf: Float
        get() = this * Resources.getSystem().displayMetrics.density + 0.5f

    val Float.dpf: Float
        get() = this * Resources.getSystem().displayMetrics.density + 0.5f

    fun getAverageColor(color1: Int, color2: Int): Int {
        val r1 = Color.red(color1)
        val r2 = Color.red(color2)
        val g1 = Color.green(color1)
        val g2 = Color.green(color2)
        val b1 = Color.blue(color1)
        val b2 = Color.blue(color2)
        return Color.argb(255, r1 / 2 + r2 / 2, g1 / 2 + g2 / 2, b1 / 2 + b2 / 2)
    }
    fun getPatternColor(color: Int): Int {
        return getPatternColor(color, false)
    }

    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    fun getPatternColor(color: Int, alwaysDark: Boolean): Int {
        val hsb: FloatArray = RGBtoHSB(
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        if (hsb[1] > 0.0f || hsb[2] < 1.0f && hsb[2] > 0.0f) {
            hsb[1] =
                Math.min(1.0f, hsb[1] + (if (alwaysDark) 0.15f else 0.05f) + 0.1f * (1.0f - hsb[1]))
        }
        if (alwaysDark || hsb[2] > 0.5f) {
            hsb[2] = Math.max(0.0f, hsb[2] * 0.65f)
        } else {
            hsb[2] = Math.max(0.0f, Math.min(1.0f, 1.0f - hsb[2] * 0.65f))
        }
        return HSBtoRGB(
            hsb[0],
            hsb[1],
            hsb[2]
        ) and if (alwaysDark) -0x66000001 else 0x66ffffff
    }


    fun HSBtoRGB(hue: Float, saturation: Float, brightness: Float): Int {
        var r = 0
        var g = 0
        var b = 0
        if (saturation == 0f) {
            b = (brightness * 255.0f + 0.5f).toInt()
            g = b
            r = g
        } else {
            val h = (hue - Math.floor(hue.toDouble()).toFloat()) * 6.0f
            val f = h - Math.floor(h.toDouble()).toFloat()
            val p = brightness * (1.0f - saturation)
            val q = brightness * (1.0f - saturation * f)
            val t = brightness * (1.0f - saturation * (1.0f - f))
            when (h.toInt()) {
                0 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (t * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }
                1 -> {
                    r = (q * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (p * 255.0f + 0.5f).toInt()
                }
                2 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (brightness * 255.0f + 0.5f).toInt()
                    b = (t * 255.0f + 0.5f).toInt()
                }
                3 -> {
                    r = (p * 255.0f + 0.5f).toInt()
                    g = (q * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }
                4 -> {
                    r = (t * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (brightness * 255.0f + 0.5f).toInt()
                }
                5 -> {
                    r = (brightness * 255.0f + 0.5f).toInt()
                    g = (p * 255.0f + 0.5f).toInt()
                    b = (q * 255.0f + 0.5f).toInt()
                }
            }
        }
        return -0x1000000 or (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff)
    }

    fun RGBtoHSB(r: Int, g: Int, b: Int): FloatArray {
        var hue: Float
        val saturation: Float
        val brightness: Float
        val hsbvals = FloatArray(3)
        var cmax = Math.max(r, g)
        if (b > cmax) {
            cmax = b
        }
        var cmin = Math.min(r, g)
        if (b < cmin) {
            cmin = b
        }
        brightness = cmax.toFloat() / 255.0f
        saturation = if (cmax != 0) {
            (cmax - cmin) as Float / cmax.toFloat()
        } else {
            0f
        }
        if (saturation == 0f) {
            hue = 0f
        } else {
            val redc = (cmax - r) as Float / (cmax - cmin) as Float
            val greenc = (cmax - g) as Float / (cmax - cmin) as Float
            val bluec = (cmax - b) as Float / (cmax - cmin) as Float
            hue = if (r == cmax) {
                bluec - greenc
            } else if (g == cmax) {
                2.0f + redc - bluec
            } else {
                4.0f + greenc - redc
            }
            hue = hue / 6.0f
            if (hue < 0) {
                hue = hue + 1.0f
            }
        }
        hsbvals[0] = hue
        hsbvals[1] = saturation
        hsbvals[2] = brightness
        return hsbvals
    }

    fun runOnUIThread(runnable: Runnable) {
        runOnUIThread(runnable, 0)
    }

    fun runOnUIThread(runnable: Runnable, delay: Long) {
        if (App.applicationHandler == null) {
            return
        }
        if (delay == 0L) {
            App.applicationHandler?.post(runnable)
        } else {
            App.applicationHandler?.postDelayed(runnable, delay)
        }
    }
    fun createFrame(width: Int, height: Float): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height))
    }

    private fun getSize(size: Float): Int {
        return (if (size < 0) size else size.dp).toInt()
    }

    fun Context?.getForceActivity(): Activity? {
        if (this == null) return null
        if (this is Activity) return this
        return if (this is ContextWrapper) this.baseContext.getForceActivity() else null
    }
}


