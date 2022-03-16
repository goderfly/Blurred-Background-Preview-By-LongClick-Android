package com.mirbor.blurpreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class PlaygroundFragment() : FullscreenDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.blurred_fragment, container, false)
    }


    companion object {
        const val TAG = "PlaygroundFragment"

        fun newInstance(): PlaygroundFragment {
            return PlaygroundFragment()
        }

    }
}