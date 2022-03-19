package com.mirbor.blurpreview

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast


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
        view.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING  // Ignore device's setting. Otherwise, you can use FLAG_IGNORE_VIEW_SETTING to ignore view's setting.
        );
    }

    override fun onPeekChooseView(view: View) {
        Toast.makeText(requireContext(), "Вы выбрали ${view.toString().substringAfterLast(":id/")}", Toast.LENGTH_SHORT).show()
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