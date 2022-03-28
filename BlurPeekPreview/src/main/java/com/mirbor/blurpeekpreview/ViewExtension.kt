package com.mirbor.blurpeekpreview

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.fragment.app.FragmentManager
import com.mirbor.blurpeekpreview.AndroidUtils.dp


@SuppressLint("ClickableViewAccessibility")
fun View.setBlurredPeekFragment(
    fragmentManager: FragmentManager,
    fragment: com.mirbor.blurpeekpreview.FullscreenDialogFragment,
    swipeIgnoreBottomPadding: Int = 48.dp,
    swipeMaximizeLength: Int = 48.dp
) {
    var startRowY = 0
    var lastRowY = 0
    var isReachMaximizedState = false
    var isBottomPaddingCalculated = false

    setOnLongClickListener {
        isBottomPaddingCalculated = false
        fragment.show(fragmentManager, fragment.javaClass.name)
        return@setOnLongClickListener true
    }

    setOnTouchListener { _, motionEvent ->
        lastRowY = motionEvent.rawY.toInt()

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                startRowY = motionEvent.rawY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                if (fragment.isResumed) {
                    isReachMaximizedState = false
                    fragment.onPeekDismiss()
                    val curView = fragment.currentIntersectedView
                    curView?.let {
                        fragment.onPeekChooseView(it)
                    }
                }
                return@setOnTouchListener true
            }

            MotionEvent.ACTION_MOVE -> {
                if (fragment.isVisible) {
                    fragment.onChangePeekCoordrinates(motionEvent.rawX, motionEvent.rawY)

                    if (!isBottomPaddingCalculated && startRowY > fragment.getYBottomRaw()) {
                        isBottomPaddingCalculated = true
                        startRowY = fragment.getYBottomRaw() - swipeIgnoreBottomPadding
                    }

                    val diff = (startRowY  - lastRowY)

                    if (diff > swipeMaximizeLength && !isReachMaximizedState) {
                        fragment.onPeekMaximized()
                        isReachMaximizedState = true
                        return@setOnTouchListener true
                    } else if (diff > 0) {
                        fragment.onPeekMaximizeSwipe(diff)
                    }
                }
            }

        }
        return@setOnTouchListener false
    }

}

fun View.toBitmap(onBitmapReady: (Bitmap) -> Unit, onBitmapError: (Exception) -> Unit) {

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val temporalBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)

            // Above Android O, use PixelCopy due
            // https://stackoverflow.com/questions/58314397/
            val window: Window = (this.context as Activity).window
            val location = IntArray(2)
            this.getLocationInWindow(location)
            val viewRectangle = Rect(location[0], location[1], location[0] + this.width, location[1] + this.height)
            val onPixelCopyListener: PixelCopy.OnPixelCopyFinishedListener = PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    onBitmapReady(temporalBitmap)
                } else {
                    error("Error while copying pixels, copy result: $copyResult")
                }
            }
            PixelCopy.request(window, viewRectangle, temporalBitmap, onPixelCopyListener, Handler(Looper.getMainLooper()))
        } else {
            val temporalBitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565)
            val canvas = android.graphics.Canvas(temporalBitmap)
            this.draw(canvas)
            canvas.setBitmap(null)
            onBitmapReady(temporalBitmap)
        }

    } catch (exception: Exception) {
        onBitmapError(exception)
    }
}

fun View.isIntersectWith(
    rawX: Int,
    rawY: Int,
    horDetectPadding: Int = 0,
    verDetectPadding: Int = 0
): Boolean {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    val x = location[0]
    val y = location[1]

    val width: Int = this.width
    val height: Int = this.height
    val calculatedWidth = width - (horDetectPadding * 2)
    val calculatedHeight = height - (verDetectPadding * 2)
    //Check the intersection of point with rectangle achieved
    val calculatedWidthRange = (x + horDetectPadding) .. (x + horDetectPadding + calculatedWidth)
    val calculatedHeightRange = (y + verDetectPadding) .. (y + verDetectPadding + calculatedHeight)

    return rawX in calculatedWidthRange && rawY in calculatedHeightRange
}
// ш 125
// жо 50
// 25 50 25
// rawY 1535 > x 288 + calculatedWith 865