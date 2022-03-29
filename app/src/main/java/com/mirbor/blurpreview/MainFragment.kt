package com.mirbor.blurpreview

import android.R.attr.data
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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


        val list = mutableListOf<Movie>().apply {
            add(Movie(url = "https://cdn.lorem.space/images/movie/.cache/150x220/kill-bill-2003.jpg", name = "SampleFilm1"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=B0E33EF4", name = "SampleFilm2"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=4F32C4CF", name = "SampleFilm3"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=7F5AE56A", name = "SampleFilm4"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=BDC01094", name = "SampleFilm5"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=9D9539E7", name = "SampleFilm6"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=225E6693", name = "SampleFilm7"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=A89D0DE6", name = "SampleFilm8"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=500B67FB", name = "SampleFilm9"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=8B7BCDC2", name = "SampleFilm0"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=2D297A22", name = "SampleFilm11"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=B0E33EF4", name = "SampleFilm12"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=4F32C4CF", name = "SampleFilm13"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=7F5AE56A", name = "SampleFilm14"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=BDC01094", name = "SampleFilm15"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=9D9539E7", name = "SampleFilm16"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=225E6693", name = "SampleFilm17"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=A89D0DE6", name = "SampleFilm18"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=500B67FB", name = "SampleFilm19"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=8B7BCDC2", name = "SampleFilm20"))
        }

        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 3)
        val adapter = SampleRecyclerViewAdapter(requireContext(), list) {
            val fragment = SampleDialogFragment.newInstance()
            it.setBlurredPeekFragment(
                fragmentManager = parentFragmentManager,
                fragment = fragment,
                horizontalPadding = 16.dp
            )
        }

        adapter.setClickListener(object: View.OnClickListener, SampleRecyclerViewAdapter.ItemClickListener {
            override fun onClick(p0: View?) {

            }

            override fun onItemClick(view: View?, position: Int) {

            }

        })

        binding.recycler.adapter = adapter


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}

