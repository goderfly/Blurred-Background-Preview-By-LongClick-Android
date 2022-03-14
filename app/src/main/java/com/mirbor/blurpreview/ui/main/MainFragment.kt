package com.mirbor.blurpreview.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mirbor.blurpreview.AndroidUtils.getStatusBarHeight
import com.mirbor.blurpreview.NativeBlur
import com.mirbor.blurpreview.NativeBlur.getBlurredBackgroundBitmap
import com.mirbor.blurpreview.R
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
        binding.button.setOnLongClickListener {
            val bmp = getBlurredBackgroundBitmap(requireActivity())

            parentFragmentManager.beginTransaction()
                .replace(R.id.container, BlurredFragment.newInstance(bmp))
                .addToBackStack("main")
                .commit()

            return@setOnLongClickListener true
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}

