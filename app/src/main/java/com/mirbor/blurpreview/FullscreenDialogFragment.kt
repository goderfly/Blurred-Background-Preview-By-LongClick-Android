package com.mirbor.blurpreview

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.mirbor.blurpreview.AndroidUtils.dp
import com.mirbor.blurpreview.AndroidUtils.getStatusBarHeight

abstract class FullscreenDialogFragment(
    val layout: Int,
    val paddingRightDp: Int = -1,
    val paddingTopDp: Int = -1,
    val paddingBottomDp: Int = -1,
) : DialogFragment(layout) {

    private lateinit var bmp: Bitmap
    lateinit var actualBackground: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
        //val decorView = requireActivity().window.decorView
        //val rootView = (decorView.rootView as ViewGroup).getChildAt(0)
        //actualBackground = decorView.background
        //rootView.background = blurredBg.toDrawable(requireContext().resources)
        bmp = NativeBlur.getBlurredBackgroundBitmap(requireActivity())!!
    }

/*    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layout, container, false)
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                onBackPressed()
                return@OnKeyListener true
            }
            false
        })

        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        //root.background = blurredBmp.toDrawable(requireContext().resources)
        return dialog.apply {
            //requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(root)
            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                setBackgroundDrawable(bmp.toDrawable(requireContext().resources))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setDimAmount(0f)
            }

        }
    }

    open fun onBackPressed() {
        dismiss()
    }
}