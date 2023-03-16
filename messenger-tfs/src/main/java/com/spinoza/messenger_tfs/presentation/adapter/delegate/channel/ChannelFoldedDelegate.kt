package com.spinoza.messenger_tfs.presentation.adapter.delegate.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.ChannelFoldedItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.delegate.AdapterDelegate
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem

class ChannelFoldedDelegate : AdapterDelegate {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(
            ChannelFoldedItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        item: DelegateItem,
        position: Int,
    ) {
        (holder as ViewHolder).bind(item as ChannelFoldedDelegateItem)
    }

    override fun isOfViewType(item: DelegateItem): Boolean {
        return item is ChannelFoldedDelegateItem
    }

    class ViewHolder(private val binding: ChannelFoldedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChannelFoldedDelegateItem) {
            val channel = item.content() as Channel
            binding.root.setOnClickListener {
                item.getOnChannelClickListener().invoke(channel.channelId)
            }
            binding.textViewChannel.text = channel.name
        }
    }
}