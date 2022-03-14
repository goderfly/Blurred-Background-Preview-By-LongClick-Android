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
import com.mirbor.blurpreview.NativeBlur
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
            Toast.makeText(this@MainFragment.context, "Test", Toast.LENGTH_SHORT).show()
            val internalBitmap = Bitmap.createBitmap(
                binding.root.width,
                binding.root.height,
                Bitmap.Config.ARGB_8888
            );
            val internalCanvas = Canvas(internalBitmap);

            requireActivity().window.decorView.rootView.draw(internalCanvas);


            val decorView: View = requireActivity().window.decorView;
            val rootView: View = decorView.findViewById(android.R.id.content);
            val windowBackground: Drawable = decorView.background;

            windowBackground.draw(internalCanvas);
            rootView.draw(internalCanvas);
            Toast.makeText(this@MainFragment.context, "Test", Toast.LENGTH_SHORT).show()
            //binding.imageView.setImageBitmap()

            parentFragmentManager.beginTransaction()
                .replace(R.id.container, BlurredFragment.newInstance(NativeBlur.blurBitmap(internalBitmap)))
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

