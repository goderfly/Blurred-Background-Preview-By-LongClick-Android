package com.mirbor.blurpreview

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.children
import androidx.fragment.app.DialogFragment


abstract class FullscreenDialogFragment : DialogFragment(), IBlurredPeekFragmentInteraction {
    private lateinit var bmpBackground: Bitmap
    private var wPercent: Int = 100
    private var hPercent: Int = 100
    private var currentIntersectedViews: MutableSet<View> = mutableSetOf()

    internal fun setAreaOfViewsToPeekInteract(withPercent: Int, heightPercent: Int) {
        this.wPercent = withPercent
        this.hPercent = heightPercent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bmpBackground = NativeBlur.getBlurredBackgroundBitmap(requireActivity())
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
                setBackgroundDrawable(bmpBackground.toDrawable(requireContext().resources))
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setDimAmount(0f)
            }
        }
    }

    internal fun onChangePeekCoordrinates(x: Float, y: Float) {
        if (isResumed) {
            (view as? ViewGroup)?.children?.forEach {
                if (it.isIntersectWith(
                        rawX = x,
                        rawY = y,
                        wPercent = wPercent,
                        hPercent = hPercent
                    )
                ) {
                    //Для исключения множественной сработки коллбека, сравниваем на повторный хит
                    if (!currentIntersectedViews.contains(it)) {
                        currentIntersectedViews.add(it)
                        onPeekInteractWithView(it)
                    }
                } else {
                    currentIntersectedViews.remove(it)
                }
            }
        }
    }
}