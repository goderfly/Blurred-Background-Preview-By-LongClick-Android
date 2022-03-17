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
    rawX: Float,
    rawY: Float,
    wPercent: Int = 100,
    hPercent: Int = 100
): Boolean {
    val location = IntArray(2)
    this.getLocationOnScreen(location)
    val x = location[0]
    val y = location[1]

    val width: Int = this.width
    val height: Int = this.height
    val calculatedWith = width * (wPercent / 100)
    val calculatedHeight = height * (hPercent / 100)
    //Check the intersection of point with rectangle achieved
    return !(rawX < x || rawY > x + calculatedWith || rawY < y || rawY > y + calculatedHeight)
}