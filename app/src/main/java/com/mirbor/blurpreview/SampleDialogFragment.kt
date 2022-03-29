package com.mirbor.blurpreview

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mirbor.blurpeekpreview.BlurredPeekDialogFragment
import com.mirbor.blurpreview.databinding.BlurredFragmentBinding


class SampleDialogFragment : BlurredPeekDialogFragment() {
    private var _binding: BlurredFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = BlurredFragmentBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onPeekMaximizeSwipe(yAxisOffset: Int) {
        Log.d("Bluuur", "onPeekMaximizeSwipe $yAxisOffset")
        binding.root.translationY = -yAxisOffset.toFloat()
    }

    override fun onPeekMaximized() {
        Log.d("Bluuur", "onPeekMaximized")
        Toast.makeText(requireContext(), "onPeekMaximized", Toast.LENGTH_SHORT).show()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): SampleDialogFragment {
            return SampleDialogFragment()
        }

    }
}