package com.mirbor.blurpreview.ui.main


import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import com.mirbor.blurpreview.databinding.BlurredFragmentBinding


class BlurredFragment(val blurBitmap: Bitmap?) : Fragment() {
    private var _binding: BlurredFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BlurredFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //activity?.window?.decorView!!.background = blurBitmap?.toDrawable(requireContext().resources)
        //val decorView: View = activity?.window?.decorView!!
        //val rootView: View = decorView.findViewById(android.R.id.content)
        //rootView.background = blurBitmap?.toDrawable(requireContext().resources)



        val decorView = requireActivity().window.decorView
        val rootView = (decorView.rootView as ViewGroup).getChildAt(0)

        rootView.background = blurBitmap?.toDrawable(requireContext().resources)

        Toast.makeText(this@BlurredFragment.context, "Opened", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(blurBitmap: Bitmap?) = BlurredFragment(blurBitmap)
    }
}