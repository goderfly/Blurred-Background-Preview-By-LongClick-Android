package com.mirbor.blurpreview

import android.view.View

interface IBlurredPeekFragmentInteraction {
    fun onDismiss()
    fun onMaximize()
    fun onInteractWithView(view: View)
}