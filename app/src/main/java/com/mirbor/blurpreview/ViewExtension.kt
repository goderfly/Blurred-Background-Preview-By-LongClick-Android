package com.mirbor.blurpreview

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentManager


@SuppressLint("ClickableViewAccessibility")
fun View.showBlurredPeekFragment(
    fragmentManager: FragmentManager,
    fragment: FullscreenDialogFragment,
    callback: IBlurredPeekFragmentInteraction
) {
    var lastY = 0f
    var startY = 0f
    var isReachMaximizedState = false

    setOnLongClickListener {
        fragment
            .setInteractionCallback(callback)
            .show(fragmentManager, fragment.javaClass.name)
        startY = lastY
        return@setOnLongClickListener true
    }

    setOnTouchListener { view, motionEvent ->
        lastY = motionEvent.y

        if (motionEvent.action == MotionEvent.ACTION_UP) {
            isReachMaximizedState = false
            callback.onDismiss()
        } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            val dy = startY - lastY

            if (dy > 400 && !isReachMaximizedState) {
                callback.onMaximize()
                Log.d("Bluuur", "dy > 400")
                isReachMaximizedState = true
            }
        } else {
            Log.d("Bluuur", "ELESEEEEEEEEEE")
        }
        return@setOnTouchListener false
    }

}

fun View.recreateTouchListener() {

}