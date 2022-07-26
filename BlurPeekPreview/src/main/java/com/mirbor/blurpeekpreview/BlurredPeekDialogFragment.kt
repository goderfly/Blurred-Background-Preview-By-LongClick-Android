package com.mirbor.blurpeekpreview

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.mirbor.blurpeekpreview.AndroidUtils.disallowClipForParents
import com.mirbor.blurpeekpreview.AndroidUtils.dp
import com.mirbor.blurpeekpreview.AndroidUtils.getFirstViewFromViewGroup
import com.mirbor.blurpeekpreview.AndroidUtils.getYBottomRaw


abstract class BlurredPeekDialogFragment : DialogFragment(), IBlurredPeekFragmentInteraction {
    private var blurredBmp: Bitmap? = null
    private var horizontalPadding: Int = 0
    internal var currentIntersectedView: View? = null
    private var currentInitiatedView: View? = null
    private var verDetectPadding: Int = 0
    private var horDetectPadding: Int = 0

    companion object {
        var statusBarHeight = 9.dp
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NativeBlur.getBlurredBackgroundBitmap(
            requireActivity(),
            onBitmapReady = {
                blurredBmp = it
                dialog?.window?.apply {
                    setBackgroundDrawable(blurredBmp?.toDrawable(requireContext().resources))
                }
            },
            onBitmapError = {
                it.printStackTrace()
            },
            statusBarHeight = BlurredPeekDialogFragment.statusBarHeight
        )


    }

    override fun onStart() {
        super.onStart()
        dialog!!.window?.apply {
            setDimAmount(0.0f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.apply {
            setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    dialog?.onBackPressed()
                    return@OnKeyListener true
                }
                false
            })
            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                setBackgroundDrawable(blurredBmp?.toDrawable(requireContext().resources))

                decorView.apply {
                    attributes = WindowManager.LayoutParams().apply {
                        copyFrom(window!!.attributes)
                        width = WindowManager.LayoutParams.MATCH_PARENT
                        height = WindowManager.LayoutParams.MATCH_PARENT
                    }

                    requestLayout()
                    getFirstViewFromViewGroup()
                        .apply { disallowClipForParents() }
                        .apply {
                            layoutParams = FrameLayout.LayoutParams(
                                Resources.getSystem().displayMetrics.widthPixels - (horizontalPadding * 2),
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.CENTER
                            }
                        }

                }
            }

        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    internal fun setHorizontalPadding(horDetectPadding: Int, verDetectPadding: Int) {
        this.verDetectPadding = horDetectPadding
        this.horDetectPadding = verDetectPadding
    }

    internal fun setHorizontalPadding(horizontalPadding: Int) {
        this.horizontalPadding = horizontalPadding
    }

    internal fun setInitiatedView(initiatedView: View) {
        this.currentInitiatedView = initiatedView
    }

    internal fun getYBottomRaw(): Int {
        return view?.getYBottomRaw() ?: 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentIntersectedView = null
    }

    internal fun onChangePeekCoordrinates(x: Float, y: Float) {
       view?.getAllChildren()?.forEach {
            if (it.isIntersectWith(
                    rawX = x.toInt(),
                    rawY = y.toInt(),
                    horDetectPadding = horDetectPadding,
                    verDetectPadding = verDetectPadding
                )
            ) {

                //Для исключения множественной сработки коллбека, сравниваем на повторный хит
                if (currentIntersectedView != it) {
                    currentIntersectedView = it
                    onPeekInteractWithView(it)
                }
            }

        }
    }
}

