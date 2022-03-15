package com.mirbor.blurpreview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable


class PlaygroundFragment(val blurredBg: Bitmap) : FullscreenDialogFragment(R.layout.blurred_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fm = requireActivity().supportFragmentManager
        if (fm.isDestroyed) {
            return
        }

        val decorView = requireActivity().window.decorView
        val rootView = (decorView.rootView as ViewGroup).getChildAt(0)
        actualBackground = decorView.background
        rootView.background = blurredBg.toDrawable(requireContext().resources)

    }

    override fun onDetach() {
        super.onDetach()
        val decorView = requireActivity().window.decorView
        val rootView = (decorView.rootView as ViewGroup).getChildAt(0)
        rootView.background = actualBackground
    }

    companion object {
        const val TAG = "PlaygroundFragment"

        fun newInstance(blurredBg: Bitmap): PlaygroundFragment {
            return PlaygroundFragment(blurredBg)
        }
    }
}