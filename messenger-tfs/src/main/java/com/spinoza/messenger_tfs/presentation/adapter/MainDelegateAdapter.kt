package com.spinoza.messenger_tfs.presentation.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import javax.inject.Inject

class MainDelegateAdapter @Inject constructor() :
    ListAdapter<DelegateAdapterItem, RecyclerView.ViewHolder>(DelegateAdapterItemCallback()) {

    var borderPosition: Int = DEFAULT_BORDER_POSITION
    var onReachStartListener: (() -> Unit)? = null
    var onReachEndListener: (() -> Unit)? = null

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
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (position != RecyclerView.NO_POSITION) {
            var isListenerInvoked = false
            onReachEndListener?.let { listener ->
                if (position + borderPosition >= itemCount) {
                    isListenerInvoked = true
                    listener()
                }
            }
            if (!isListenerInvoked) onReachStartListener?.let { listener ->
                if (position - borderPosition <= FIRST_POSITION) {
                    listener()
                }
            }
            delegates[getItemViewType(position)]
                .onBindViewHolder(holder, getItem(position), position, payloads)
        }
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

    private companion object {

        const val FIRST_POSITION = 0
        const val DEFAULT_BORDER_POSITION = 10
    }
}