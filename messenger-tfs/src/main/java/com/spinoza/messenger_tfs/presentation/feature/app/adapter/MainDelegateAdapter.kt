package com.spinoza.messenger_tfs.presentation.feature.app.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MainDelegateAdapter :
    ListAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>(DelegateAdapterItemCallback()) {

    private val delegates: MutableList<AdapterDelegate> = mutableListOf()

    fun addDelegate(delegate: AdapterDelegate) {
        delegates.add(delegate)
    }

    fun clear() {
        delegates.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegates[viewType].onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position != RecyclerView.NO_POSITION)
            delegates[getItemViewType(position)].onBindViewHolder(
                holder, getItem(position), position
            )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (position != RecyclerView.NO_POSITION)
            delegates[getItemViewType(position)].onBindViewHolder(
                holder, getItem(position), position, payloads
            )
    }

    override fun getItemViewType(position: Int): Int {
        return if (position != RecyclerView.NO_POSITION)
            delegates.indexOfFirst { it.isOfViewType(currentList[position]) }
        else
            RecyclerView.NO_POSITION
    }

    public override fun getItem(position: Int): DelegateAdapterItem {
        return super.getItem(position)
    }
}