package com.mirbor.blurpeekpreview

import android.view.View

interface IBlurredPeekFragmentInteraction {
    fun onPeekDismiss()
    fun onPeekMaximizeSwipe(yAxisOffset: Int, maxSwipeDistance: Int)
    fun onPeekMaximized()
    fun onPeekInteractWithView(view: View)
    fun onPeekChooseView(view: View)
}