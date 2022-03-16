package com.mirbor.blurpreview

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment


abstract class FullscreenDialogFragment() : DialogFragment() {
    private lateinit var interactionCallback: IBlurredPeekFragmentInteraction
    private lateinit var bmp: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bmp = NativeBlur.getBlurredBackgroundBitmap(requireActivity())!!
    }

    fun setInteractionCallback(callback: IBlurredPeekFragmentInteraction): FullscreenDialogFragment  {
        interactionCallback = callback
        return this
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*view.parent.requestDisallowInterceptTouchEvent(true)
        view.parent.parent.requestDisallowInterceptTouchEvent(true)
        view.parent.parent.parent.requestDisallowInterceptTouchEvent(true)
        *//*root.setOnTouchListener { view, motionEvent ->
            Log.d("Bluuur", "view $view event $motionEvent")
            return@setOnTouchListener true
        }*//*
        view.parent.apply {
           val firstView = (this as ViewGroup).getChildAt(0)
            removeViewAt(0)
            addView(InterceptTouchFrameLayout(activity).apply {
                isClickable = true
                isFocusable = true
                setOnInterceptTouchEventListener(object :
                    InterceptTouchFrameLayout.OnInterceptTouchEventListener {
                    override fun onInterceptTouchEvent(
                        view: InterceptTouchFrameLayout?,
                        ev: MotionEvent?,
                        disallowIntercept: Boolean
                    ): Boolean {
                        Log.d("Bluuur", "view $view event $ev")
                        return false
                    }

                    override fun onTouchEvent(
                        view: InterceptTouchFrameLayout?,
                        event: MotionEvent?
                    ): Boolean {
                        Log.d("Bluuur", "view $view event $event")
                        return false
                    }
                })
                addView(firstView)
            })
        }*/

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(root)
            setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    onBackPressed()
                    return@OnKeyListener true
                }
                false
            })

            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                setBackgroundDrawable(bmp.toDrawable(requireContext().resources))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setDimAmount(0f)
            }

        }
    }
}