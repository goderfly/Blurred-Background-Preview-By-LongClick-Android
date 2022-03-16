package com.mirbor.blurpreview.ui.main

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
        binding.button.showBlurredPeekFragment(parentFragmentManager, fragment,
            object : IBlurredPeekFragmentInteraction {
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

@SuppressLint("ClickableViewAccessibility")
private fun View.showBlurredPeekFragment(
    fragmentManager: FragmentManager,
    fragment: FullscreenDialogFragment,
    callback: IBlurredPeekFragmentInteraction
) {
    var lastY = 0f
    var startY = 0f

    setOnLongClickListener {
        fragment
            .setInteractionCallback(callback)
            .show(fragmentManager, fragment.javaClass.name)
        startY = lastY
        return@setOnLongClickListener true
    }

    fun recreateTouchListener() {
        lastY = 0f
        startY = 0f

        setOnTouchListener { view, motionEvent ->
            lastY = motionEvent.y

            if (motionEvent.action == MotionEvent.ACTION_UP) {
                callback.onDismiss()
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                val dy = startY - lastY

                if (dy > 400) {
                    callback.onMaximize()
                    Log.d("Bluuur", "dy > 400")
                    setOnTouchListener(null)
                    recreateTouchListener()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }

    recreateTouchListener()
}

