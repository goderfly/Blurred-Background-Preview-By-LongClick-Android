package com.mirbor.blurpreview

import android.widget.FrameLayout
import com.mirbor.blurpreview.InterceptTouchFrameLayout
import android.view.MotionEvent
import com.mirbor.blurpreview.InterceptTouchFrameLayout.OnInterceptTouchEventListener
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import com.mirbor.blurpreview.InterceptTouchFrameLayout.DummyInterceptTouchEventListener

/**
 * A FrameLayout that allow setting a delegate for intercept touch event
 */
class InterceptTouchFrameLayout : FrameLayout {
    private var mDisallowIntercept = false

    interface OnInterceptTouchEventListener {
        /**
         * If disallowIntercept is true the touch event can't be stealed and the return value is ignored.
         * @see android.view.ViewGroup.onInterceptTouchEvent
         */
        fun onInterceptTouchEvent(
            view: InterceptTouchFrameLayout?,
            ev: MotionEvent?,
            disallowIntercept: Boolean
        ): Boolean

        /**
         * @see android.view.View.onTouchEvent
         */
        fun onTouchEvent(view: InterceptTouchFrameLayout?, event: MotionEvent?): Boolean
    }

    override fun onDraw(canvas: Canvas?) {
        Log.d("Bluuur", "onDraw")
        super.onDraw(canvas)
    }

    private class DummyInterceptTouchEventListener : OnInterceptTouchEventListener {
        override fun onInterceptTouchEvent(
            view: InterceptTouchFrameLayout?,
            ev: MotionEvent?,
            disallowIntercept: Boolean
        ): Boolean {
            return false
        }

        override fun onTouchEvent(view: InterceptTouchFrameLayout?, event: MotionEvent?): Boolean {
            return false
        }
    }

    private var mInterceptTouchEventListener = DUMMY_LISTENER

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyle: Int) : super(
        context!!, attrs, defStyleAttr, defStyle
    ) {
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("Bluuur", "event $ev")
        return super.dispatchTouchEvent(ev)
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        parent.requestDisallowInterceptTouchEvent(disallowIntercept)
        mDisallowIntercept = disallowIntercept
    }

    fun setOnInterceptTouchEventListener(interceptTouchEventListener: OnInterceptTouchEventListener?) {
        mInterceptTouchEventListener = interceptTouchEventListener ?: DUMMY_LISTENER
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val stealTouchEvent =
            mInterceptTouchEventListener.onInterceptTouchEvent(this, ev, mDisallowIntercept)
        return stealTouchEvent && !mDisallowIntercept || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = mInterceptTouchEventListener.onTouchEvent(this, event)
        return handled || super.onTouchEvent(event)
    }

    companion object {
        private val DUMMY_LISTENER: OnInterceptTouchEventListener =
            DummyInterceptTouchEventListener()
    }
}