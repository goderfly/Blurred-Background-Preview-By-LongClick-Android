package com.mirbor.blurpreview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class SampleFragment : FullscreenDialogFragment() {

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

    override fun onPeekInteractWithView(view: View) {
        Log.d("Bluuur", "interact with view $view")
    }

    override fun onPeekDismiss() {
        dismiss()
    }

    override fun onPeekMaximize() {
        Log.d("Bluuur", "maximize")
    }

    companion object {

        fun newInstance(): SampleFragment {
            return SampleFragment()
        }

    }
}