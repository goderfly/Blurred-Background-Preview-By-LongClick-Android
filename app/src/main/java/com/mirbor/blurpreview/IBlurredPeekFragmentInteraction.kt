package com.mirbor.blurpreview

import android.view.View

interface IBlurredPeekFragmentInteraction {
    fun onPeekDismiss()
    fun onPeekMaximize()
    fun onPeekInteractWithView(view: View)
    fun onPeekChooseView(view: View)
}