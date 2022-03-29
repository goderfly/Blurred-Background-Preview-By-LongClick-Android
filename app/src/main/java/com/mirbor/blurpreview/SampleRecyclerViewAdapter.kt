package com.mirbor.blurpreview

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirbor.blurpreview.SampleRecyclerViewAdapter.ItemClickListener

class SampleRecyclerViewAdapter
internal constructor(
    val context: Context?,
    val data: List<Movie>,
    val onLongClicked: (View) -> Unit
) :
    RecyclerView.Adapter<SampleRecyclerViewAdapter.ViewHolder>() {
    private val mData: List<Movie>
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.cell, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.myTextView.text = mData[position].name

        Glide.with(context!!).load(mData[position].url).into(holder.imageView);


        holder.imageView.setImageURI(Uri.parse(mData[position].url))
        onLongClicked.invoke(holder.imageView)

    }

    override fun getItemCount(): Int {
        return mData.size
    }

    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var myTextView: TextView
        var imageView: ImageView
        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }

        init {
            myTextView = itemView.findViewById(R.id.info_text)
            imageView = itemView.findViewById(R.id.imageView)
            itemView.setOnClickListener(this)
        }
    }

    fun getItem(id: Int): Movie {
        return mData[id]
    }

    fun setClickListener(itemClickListener: ItemClickListener?) {
        mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    init {
        mInflater = LayoutInflater.from(context)
        mData = data
    }
}