package com.mirbor.blurpreview

import android.view.View

object AndroidUtils {

    fun getStatusBarHeight(): Int {
        val resourceId =
            App.appContext!!.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) App.appContext!!.resources.getDimensionPixelSize(resourceId) else 0
    }

}


