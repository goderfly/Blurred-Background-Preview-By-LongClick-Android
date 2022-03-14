/*
*
* Copyright (c) 2021 Abolfazl Abbasi
*
* */

package com.mirbor.blurpreview

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.ViewGroup
import kotlin.math.roundToInt


object NativeBlur {
    private const val SUCCESS = 1
    private const val INVALID_RADIUS = -1
    private const val CAN_NOT_GET_BITMAP_INFO = -2
    private const val INVALID_BITMAP_FORMAT = -3
    private const val BITMAP_CONCURRENCY_ERROR = -4

    private var initialized: Boolean = false
    private const val LIBRARY_NAME: String = "blurlib"
    var cachedBackground: Drawable? = null



    init {
        try {
            System.loadLibrary(LIBRARY_NAME)
            initialized = true
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    fun getBlurredBackgroundBitmap(activity: Activity, includeStatusbar: Boolean = false): Bitmap? {
        val decorView = activity.window.decorView
        val rootView = (decorView.rootView as ViewGroup).getChildAt(0)

        val internalBitmap = Bitmap.createBitmap(
            rootView.width,
            rootView.height,
            Bitmap.Config.ARGB_8888
        )

        val internalCanvas = Canvas(internalBitmap)

        decorView.background.draw(internalCanvas)

        rootView.draw(internalCanvas)

        val croppedBpm = Bitmap.createBitmap(
            internalBitmap,
            0, if (includeStatusbar) AndroidUtils.getStatusBarHeight() else 0,
            internalBitmap.width,
            internalBitmap.height - if (includeStatusbar) {
                AndroidUtils.getStatusBarHeight()
            } else {
                0
            }
        )

        return blurBitmap(croppedBpm)
    }

    /**
     * Returns a blurred bitmap with the specified radius and compress. Its
     * call native C++ method
     *
     * @param sourceBitmap    The source bitmap must blurred
     * @param radius   The radius of the blur grate than 1
     * @param compress   The compress config to return
     */
    fun blurBitmap(sourceBitmap: Bitmap, radius: Int = 10, compress: Boolean = true): Bitmap? {
        if (!initialized) {
            Log.e("NativeBlur", "First init blur lib.")
            return null
        }
        val startTime = System.currentTimeMillis()
        val result: Int
        val finalBitmap: Bitmap

        if (compress) {
            val compressedBitmap: Bitmap = if (sourceBitmap.height > sourceBitmap.width) {
                Bitmap.createBitmap(
                    (450f * sourceBitmap.width / sourceBitmap.height).roundToInt(),
                    450,
                    Bitmap.Config.ARGB_8888
                )
            } else {
                Bitmap.createBitmap(
                    450,
                    (450f * sourceBitmap.height / sourceBitmap.width).roundToInt(),
                    Bitmap.Config.ARGB_8888
                )
            }
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            val rect = Rect(0, 0, compressedBitmap.width, compressedBitmap.height)
            Canvas(compressedBitmap).drawBitmap(sourceBitmap, null, rect, paint)
            result = fastBlurAlpha(compressedBitmap, radius)
            finalBitmap = compressedBitmap
        } else {
            result = fastBlurAlpha(sourceBitmap, radius)
            finalBitmap = sourceBitmap
        }

        if (BuildConfig.DEBUG) {
            logResult(result, startTime, radius, compress)
        }

        return if (result == 1) finalBitmap else null
    }

    /**
     * Log native JNI native call result in DEBUG
     * build config
     */
    private fun logResult(result: Int, startTime: Long, radius: Int, compress: Boolean) {
        val endTime = System.currentTimeMillis()
        when (result) {
            SUCCESS -> {
                Log.i("NativeBlur", "Blur has done successfully with radius:$radius at:${endTime - startTime}ms in compress:$compress")
            }
            INVALID_RADIUS -> {
                Log.i("NativeBlur", "INVALID_RADIUS $radius")
            }
            CAN_NOT_GET_BITMAP_INFO -> {
                Log.i("NativeBlur", "CAN_NOT_GET_BITMAP_INFO")
            }
            INVALID_BITMAP_FORMAT -> {
                Log.i("NativeBlur", "INVALID_BITMAP_FORMAT ARG888")
            }
            BITMAP_CONCURRENCY_ERROR -> {
                Log.i("NativeBlur", "BITMAP_CONCURRENCY_ERROR")
            }
        }
    }

    /**
     * This native JNI method call for Stack Blur.
     * returns the blur Bitmap result in int.  Note that
     * while the unit of time of the return value is a error,
     *
     * @return Int result
     */
    private external fun fastBlurAlpha(bitmap: Bitmap, radius: Int): Int

}