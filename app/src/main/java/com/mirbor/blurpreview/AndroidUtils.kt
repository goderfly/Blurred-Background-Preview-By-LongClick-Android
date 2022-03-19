package com.mirbor.blurpreview

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

object AndroidUtils {

    internal fun View.getFirstViewFromViewGroup() = (this as ViewGroup).getChildAt(0)

    internal fun View.getChildViewListFromViewGroup() = (this as? ViewGroup)?.children?.toList()

    internal fun getStatusBarHeight(): Int {
        val resourceId =
            App.appContext!!.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) App.appContext!!.resources.getDimensionPixelSize(resourceId) else 0
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}


