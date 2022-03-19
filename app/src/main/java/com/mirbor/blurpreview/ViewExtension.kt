package com.mirbor.blurpreview

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentManager
import com.mirbor.blurpreview.AndroidUtils.dp


@SuppressLint("ClickableViewAccessibility")
fun View.setBlurredPeekFragment(
    fragmentManager: FragmentManager,
    fragment: FullscreenDialogFragment,
    swipeIgnoreBottomPadding: Int = 14.dp
) {
    //var lastY = 0f
    //var startY = 0f
    var startRowY = 0
    var lastRowY = 0
    var isReachMaximizedState = false
    var calculatedSwipeIgnoreBottomPadding = 0

    setOnLongClickListener {
        //startRowY = lastRowY
        calculatedSwipeIgnoreBottomPadding = 0
        fragment.show(fragmentManager, fragment.javaClass.name)
        return@setOnLongClickListener true
    }

    setOnTouchListener { _, motionEvent ->
        //lastY = motionEvent.y
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
                    if (calculatedSwipeIgnoreBottomPadding == 0 && startRowY > fragment.getYBottomRaw()) {
                        calculatedSwipeIgnoreBottomPadding = fragment.getYBottomRaw() - swipeIgnoreBottomPadding
                        startRowY = fragment.getYBottomRaw() - swipeIgnoreBottomPadding
                    }
                    fragment.onChangePeekCoordrinates(motionEvent.rawX, motionEvent.rawY)
                    val diff = (startRowY  - lastRowY)

                    Log.d("Bluuur", "" +
                            "startRawY $startRowY" +
                            " calculatedSwipeIgnoreBottomPadding ${calculatedSwipeIgnoreBottomPadding} " +
                            "lastRawY $lastRowY")

                    if (diff > 400 && !isReachMaximizedState) {
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