package com.mirbor.blurpeekpreview

import android.app.Activity
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.mirbor.blurpeekpreview.AndroidUtils.dp
import com.mirbor.blurpeekpreview.AndroidUtils.getStatusBarHeight
import com.mirbor.blurpeekpreview.BlurredPeekDialogFragment.Companion.statusBarHeight
import kotlin.math.roundToInt

object NativeBlur {
    private const val SUCCESS = 1
    private const val INVALID_RADIUS = -1
    private const val CAN_NOT_GET_BITMAP_INFO = -2
    private const val INVALID_BITMAP_FORMAT = -3
    private const val BITMAP_CONCURRENCY_ERROR = -4

    private var initialized: Boolean = false
    private const val LIBRARY_NAME: String = "blurlib"


    init {
        try {
            System.loadLibrary(LIBRARY_NAME)
            initialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(RuntimeException::class)
    fun getBlurredBackgroundBitmap(
        activity: Activity,
        statusBarHeight: Int,
        onBitmapReady: (Bitmap) -> Unit,
        onBitmapError: (Exception) -> Unit
    ) {
        val rootView = (activity.window.decorView.rootView as ViewGroup).getChildAt(0)
        val statusbarHeight = activity.getStatusBarHeight() + statusBarHeight
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val temporalBitmap = Bitmap.createBitmap(rootView.width, rootView.height - statusbarHeight, Bitmap.Config.ARGB_8888)

                val location = IntArray(2)
                rootView.getLocationInWindow(location)
                val left = location[0]
                val top = location[1] + statusBarHeight
                Log.d("asdf", "statusBarHeight + $statusBarHeight")
                val right = location[0] + rootView.width
                val bottom = location[1] + (rootView.height)

                val viewRectangle = Rect(left, top, right, bottom)

                val onPixelCopyListener: PixelCopy.OnPixelCopyFinishedListener =
                    PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            val blurredBmp = blurBitmap(temporalBitmap)
                            onBitmapReady(blurredBmp)
                        } else {
                            error("Error while copying pixels, copy result: $copyResult")
                        }
                    }
                PixelCopy.request(
                    activity.window,
                    viewRectangle,
                    temporalBitmap,
                    onPixelCopyListener,
                    Handler(Looper.getMainLooper())
                )
            } else {
                val temporalBitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.RGB_565)
                val canvas = Canvas(temporalBitmap.shrinkStatusBar(activity))
                rootView.draw(canvas)
                canvas.setBitmap(null)
                onBitmapReady(temporalBitmap)
            }

        } catch (exception: Exception) {
            onBitmapError(exception)
        }

    }
    fun Bitmap.shrinkStatusBar(activity: Activity): Bitmap {

        val x = 0
        val y = activity.getStatusBarHeight()
        val width = this.width
        val height = this.height - activity.getStatusBarHeight()
        return Bitmap.createBitmap(this, x, y, width, height)
    }
    /**
     * Returns a blurred bitmap with the specified radius and compress. Its
     * call native C++ method
     *
     * @param sourceBitmap    The source bitmap must blurred
     * @param radius   The radius of the blur grate than 1
     * @param compress   The compress config to return
     * @throws RuntimeException When error at init of lib or native process of blurring
     */

    @Throws(RuntimeException::class)
    fun blurBitmap(sourceBitmap: Bitmap, radius: Int = 6, compress: Boolean = true): Bitmap {
        if (!initialized) {
            throw RuntimeException("First init NativeBlur lib.")
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

        return if (result == 1) {
            finalBitmap
        } else {
            throw RuntimeException("Process of blurring failed")
        }
    }

    /**
     * Log native JNI native call result in DEBUG
     * build config
     */
    private fun logResult(result: Int, startTime: Long, radius: Int, compress: Boolean) {
        val endTime = System.currentTimeMillis()
        when (result) {
            SUCCESS -> {
                Log.i(
                    "NativeBlur",
                    "Blur has done successfully with radius:$radius at:${endTime - startTime}ms in compress:$compress"
                )
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