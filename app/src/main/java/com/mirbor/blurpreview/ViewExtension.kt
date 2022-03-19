package com.mirbor.blurpreview

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentManager


@SuppressLint("ClickableViewAccessibility")
fun View.setBlurredPeekFragment(
    fragmentManager: FragmentManager,
    fragment: FullscreenDialogFragment
) {
    var lastY = 0f
    var startY = 0f
    var isReachMaximizedState = false

    setOnLongClickListener {
        startY = lastY
        fragment.show(fragmentManager, fragment.javaClass.name)
        return@setOnLongClickListener true
    }

    setOnTouchListener { _, motionEvent ->
        lastY = motionEvent.y

        when (motionEvent.action) {

            MotionEvent.ACTION_UP -> {
                isReachMaximizedState = false
                fragment.onPeekDismiss()
                val curView = fragment.currentIntersectedView
                Log.d("Bluuur", "fragment.currentIntersectedView: $curView")
                curView?.let {
                    fragment.onPeekChooseView(it)
                }
                return@setOnTouchListener true
            }

            MotionEvent.ACTION_MOVE -> {
                fragment.onChangePeekCoordrinates(motionEvent.rawX, motionEvent.rawY)
                val dy = startY - lastY
                if (dy > 400 && !isReachMaximizedState) {
                    fragment.onPeekMaximize()
                    isReachMaximizedState = true
                    return@setOnTouchListener true
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