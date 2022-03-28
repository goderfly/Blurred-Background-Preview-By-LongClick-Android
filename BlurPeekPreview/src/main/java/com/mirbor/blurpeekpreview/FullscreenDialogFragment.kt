package com.mirbor.blurpeekpreview

import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.mirbor.blurpeekpreview.AndroidUtils.disallowClipForParents
import com.mirbor.blurpeekpreview.AndroidUtils.getChildViewListFromViewGroup
import com.mirbor.blurpeekpreview.AndroidUtils.getFirstViewFromViewGroup
import com.mirbor.blurpeekpreview.AndroidUtils.getYBottomRaw


abstract class FullscreenDialogFragment : DialogFragment(), IBlurredPeekFragmentInteraction {
    internal var currentIntersectedView: View? = null
    private var verDetectPadding: Int = 0
    private var horDetectPadding: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NativeBlur.getBlurredBackgroundBitmap(requireActivity()) {
            dialog!!.window?.apply {

                    decorView.apply {
                        minimumWidth = requireActivity().window.decorView.width
                        minimumHeight = requireActivity().window.decorView.height

                        getFirstViewFromViewGroup()
                            .apply { disallowClipForParents() }
                            .apply {
                                layoutParams = FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT
                                ).apply {
                                    gravity = Gravity.CENTER
                                }
                            }

                    }
                    setBackgroundDrawable(it.toDrawable(requireContext().resources))
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    setDimAmount(0f)

            }

        }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window?.apply {
            attributes = attributes?.apply {
                dimAmount = 0.10f
                flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
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

        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }


    internal fun setViewsPeekDetectPadding(horDetectPadding: Int, verDetectPadding: Int) {
        this.verDetectPadding = horDetectPadding
        this.horDetectPadding = verDetectPadding
    }

    internal fun getYBottomRaw(): Int {
        return view?.getYBottomRaw() ?: 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentIntersectedView = null
    }

    internal fun onChangePeekCoordrinates(x: Float, y: Float) {
        view?.getChildViewListFromViewGroup()?.forEach {

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

