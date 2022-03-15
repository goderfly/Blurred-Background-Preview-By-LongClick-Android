package com.mirbor.blurpreview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable


class PlaygroundFragment() : FullscreenDialogFragment(R.layout.blurred_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        const val TAG = "PlaygroundFragment"

        fun newInstance(): PlaygroundFragment {
            return PlaygroundFragment()
        }
    }
}