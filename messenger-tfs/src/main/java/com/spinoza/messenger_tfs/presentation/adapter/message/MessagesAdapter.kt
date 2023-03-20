package com.spinoza.messenger_tfs.presentation.adapter.message

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MessagesAdapter :
    ListAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>(DelegateAdapterItemCallback()) {

    private val delegates: MutableList<AdapterDelegate> = mutableListOf()

    fun addDelegate(delegate: AdapterDelegate) {
        delegates.add(delegate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegates[viewType].onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return delegates[getItemViewType(position)].onBindViewHolder(
            holder,
            getItem(position),
            position
        )
    }

    override fun getItemViewType(position: Int): Int {
        return delegates.indexOfFirst {
            it.isOfViewType(currentList[position])
        }
    }
}