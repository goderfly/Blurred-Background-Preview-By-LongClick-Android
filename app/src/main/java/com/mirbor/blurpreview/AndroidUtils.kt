package com.mirbor.blurpreview

import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

object AndroidUtils {

    internal fun View.getFirstViewFromViewGroup() = (this as ViewGroup).getChildAt(0)

    internal fun View.getChildViewListFromViewGroup() = (this as? ViewGroup)?.children?.toList()

    internal tailrec fun View?.disallowClipForParents() {
        if (this is ViewGroup) {
            clipChildren = false
        }

        val parentOfView = this?.parent

        if (parentOfView != null && parentOfView is View) {
            parentOfView.disallowClipForParents()
        }
    }

    internal fun getStatusBarHeight(): Int {
        val resourceId =
            App.appContext!!.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) App.appContext!!.resources.getDimensionPixelSize(resourceId) else 0
    }

    internal fun View.getYBottomRaw(): Int {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return location[1] + height
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}


