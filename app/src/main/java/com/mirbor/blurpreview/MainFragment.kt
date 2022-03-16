package com.mirbor.blurpreview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.mirbor.blurpreview.FullscreenDialogFragment
import com.mirbor.blurpreview.IBlurredPeekFragmentInteraction
import com.mirbor.blurpreview.PlaygroundFragment
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


        val fragment = PlaygroundFragment.newInstance()
        binding.button.showBlurredPeekFragment(
            fragmentManager = parentFragmentManager,
            fragment = fragment,
            callback = object : IBlurredPeekFragmentInteraction {
                override fun onDismiss() {
                    Log.d("Bluuur", "onDismiss")
                    fragment.dismiss()
                }

                override fun onMaximize() {
                    Toast.makeText(view.context, "Maximize", Toast.LENGTH_SHORT).show()
                    Log.d("Bluuur", "onMaximize")
                }

                override fun onInteractWithView(view: View) {
                    Log.d("Bluuur", "onInteractWithView")
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}

