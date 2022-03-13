package com.example.myapplication.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.FrameLayout
import com.example.myapplication.AndroidUtils

class BlurredFrameLayout(
    context: Context,
    private val sizeNotifierFrameLayout: SizeNotifierFrameLayout?
) : FrameLayout(context) {
    protected var backgroundPaint: Paint? = null
    var backgrColor = Color.TRANSPARENT
    var backgroundPaddingBottom = 0
    var backgroundPaddingTop = 0
    var isTopView = true
    var drawBlur = true
    override fun dispatchDraw(canvas: Canvas) {
        if (sizeNotifierFrameLayout != null && drawBlur && backgrColor != Color.TRANSPARENT) {
            if (backgroundPaint == null) {
                backgroundPaint = Paint()
            }
            backgroundPaint!!.color = backgrColor
            AndroidUtils.rectTmp2[0, backgroundPaddingTop, measuredWidth] =
                measuredHeight - backgroundPaddingBottom
            var y = 0f
            var view: View = this
            while (view !== sizeNotifierFrameLayout) {
                y += view.y
                view = view.parent as View
            }
            sizeNotifierFrameLayout.drawBlur(
                canvas,
                y,
                AndroidUtils.rectTmp2,
                backgroundPaint!!,
                isTopView
            )
        }
        super.dispatchDraw(canvas)
    }

    override fun setBackgroundColor(color: Int) {
        if (sizeNotifierFrameLayout != null) {
            backgrColor = color
        } else {
            super.setBackgroundColor(color)
        }
    }

    override fun onAttachedToWindow() {
        sizeNotifierFrameLayout?.blurBehindViews?.add(this)
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        sizeNotifierFrameLayout?.blurBehindViews?.remove(this)
        super.onDetachedFromWindow()
    }
}