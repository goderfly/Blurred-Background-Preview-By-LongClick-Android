package com.mirbor.blurpreview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mirbor.blurpeekpreview.AndroidUtils.dp
import com.mirbor.blurpeekpreview.setOnLongClickBlurredPeekFragment
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
            add(Movie(url = "https://avatars.mds.yandex.net/get-kinopoisk-image/1599028/4057c4b8-8208-4a04-b169-26b0661453e3/68x102", name = "SampleFilm1"))
            add(Movie(url = "https://avatars.mds.yandex.net/get-kinopoisk-image/1599028/0b76b2a2-d1c7-4f04-a284-80ff7bb709a4/68x102", name = "SampleFilm2"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=4F32C4CF", name = "SampleFilm3"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=7F5AE56A", name = "SampleFilm4"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=BDC01094", name = "SampleFilm5"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=9D9539E7", name = "SampleFilm6"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=225E6693", name = "SampleFilm7"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=A89D0DE6", name = "SampleFilm8"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=500B67FB", name = "SampleFilm9"))
            add(Movie(url = "https://api.lorem.space/image/movie?w=150&h=220&hash=8B7BCDC2", name = "SampleFilm0"))
            add(Movie(url = "https://avatars.mds.yandex.net/get-kinopoisk-image/1773646/b327ada7-d790-49ae-8b24-374497a0980c/68x102", name = "SampleFilm11"))
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
            it.setOnLongClickBlurredPeekFragment(
                fragmentManager = parentFragmentManager,
                fragment = fragment,
                horizontalPadding = 16.dp
            )
            it.setOnClickListener {
                Log.d("Bluuu", "CLICK!!!!!!")
            }
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

