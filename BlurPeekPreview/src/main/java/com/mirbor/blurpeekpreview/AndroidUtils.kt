package com.mirbor.blurpeekpreview

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

object AndroidUtils {

    internal fun View.getFirstViewFromViewGroup() = (this as ViewGroup).getChildAt(0)

    internal fun View.getDecorViewAsViewGroup() = this.context.getContextActivity()?.window?.decorView as ViewGroup

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

    internal fun Context.getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    internal fun View.getYBottomRaw(): Int {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return location[1] + height
    }

    internal fun Context?.getContextActivity(): Activity? {
        if (this == null) return null
        if (this is Activity) return this
        return if (this is ContextWrapper) this.baseContext.getContextActivity() else null
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    val Float.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

}


