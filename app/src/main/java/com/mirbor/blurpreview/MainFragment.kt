package com.mirbor.blurpreview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mirbor.blurpeekpreview.AndroidUtils.dp
import com.mirbor.blurpeekpreview.setBlurredPeekFragment
import com.mirbor.blurpreview.databinding.MainFragmentBinding

class MainFragment : Fragment() {
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val fragment = SampleDialogFragment.newInstance()
        binding.button.setBlurredPeekFragment(
            fragmentManager = parentFragmentManager,
            fragment = fragment,
            horizontalPadding = 16.dp
        )

        binding.button4.setBlurredPeekFragment(
            fragmentManager = parentFragmentManager,
            fragment = fragment,
            horizontalPadding = 16.dp
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}

