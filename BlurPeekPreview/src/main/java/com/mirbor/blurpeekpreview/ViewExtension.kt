package com.mirbor.blurpeekpreview

import android.annotation.SuppressLint
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.mirbor.blurpeekpreview.AndroidUtils.dp
import com.mirbor.blurpeekpreview.AndroidUtils.getDecorViewAsViewGroup


@SuppressLint("ClickableViewAccessibility")
fun View.setOnLongClickBlurredPeekFragment(
    fragmentManager: FragmentManager,
    fragment: BlurredPeekDialogFragment,
    swipeIgnoreBottomPadding: Int = 48.dp,
    swipeMaximizeLength: Int = 48.dp,
    horizontalPadding: Int = 16.dp
) {
    var startRowY = 0
    var lastRowY = 0
    var isReachMaximizedState = false
    var isBottomPaddingCalculated = false
    val handler = Handler()

    val longPressed = Runnable {
        isBottomPaddingCalculated = false
        fragment.show(fragmentManager, fragment.javaClass.name)
        fragment.setHorizontalPadding(horizontalPadding)
        fragment.setInitiatedView(this)
        getDecorViewAsViewGroup().setViewAndChildrenEnabled(this, false)
    }


    setOnTouchListener { _, motionEvent ->
        lastRowY = motionEvent.rawY.toInt()

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                startRowY = motionEvent.rawY.toInt()
                handler.postDelayed(longPressed, 500L);
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressed);
                if (fragment.isResumed) {
                    isReachMaximizedState = false
                    fragment.onPeekDismiss()
                    val curView = fragment.currentIntersectedView
                    curView?.let {
                        fragment.onPeekChooseView(it)
                    }
                }
                getDecorViewAsViewGroup().setViewAndChildrenEnabled(this, true)
            }

            MotionEvent.ACTION_MOVE -> {
                if (fragment.isVisible) {
                    fragment.onChangePeekCoordrinates(motionEvent.rawX, motionEvent.rawY)

                    if (!isBottomPaddingCalculated && startRowY > fragment.getYBottomRaw()) {
                        isBottomPaddingCalculated = true
                        startRowY = fragment.getYBottomRaw() - swipeIgnoreBottomPadding
                    }

                    val diff = (startRowY - lastRowY)

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

fun ViewGroup.suppressChildsRecyclerView(supress: Boolean) {
    children.forEach {
        if (it is ViewGroup) {
            it.suppressChildsRecyclerView(supress)
        }
        if (it is RecyclerView) {
            it.suppressLayout(supress)
        }
    }
}

private fun View.setViewAndChildrenEnabled(exceptView: View, enabled: Boolean) {
    if (this != exceptView) {
        isEnabled = enabled
        if (this is ViewGroup) {
            for (i in 0 until this.childCount) {
                this.getChildAt(i).setViewAndChildrenEnabled(this, enabled)
            }
        }
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
    val calculatedWidthRange = (x + horDetectPadding)..(x + horDetectPadding + calculatedWidth)
    val calculatedHeightRange = (y + verDetectPadding)..(y + verDetectPadding + calculatedHeight)

    return rawX in calculatedWidthRange && rawY in calculatedHeightRange
}
