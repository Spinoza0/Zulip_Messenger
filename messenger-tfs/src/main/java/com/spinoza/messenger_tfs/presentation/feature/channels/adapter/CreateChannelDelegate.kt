package com.spinoza.messenger_tfs.presentation.feature.channels.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.CreateChannelItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

class CreateChannelDelegate(private val onClickListener: () -> Unit) : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CreateChannelItemBinding.inflate(
            inflater,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(onClickListener)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateAdapterItem,
        position: Int,
        payloads: List<Any>,
    ) {
        onBindViewHolder(holder, item, position)
    }

    override fun isOfViewType(item: DelegateAdapterItem): Boolean {
        return item is CreateChannelDelegateItem
    }

    class ViewHolder(private val binding: CreateChannelItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(onClickListener: () -> Unit) {
            binding.buttonCreateChannel.setOnClickListener { onClickListener() }
        }
    }
}