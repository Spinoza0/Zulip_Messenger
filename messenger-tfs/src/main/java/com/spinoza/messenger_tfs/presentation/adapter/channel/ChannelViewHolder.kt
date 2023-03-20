package com.spinoza.messenger_tfs.presentation.adapter.channel

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

class ChannelViewHolder(private val binding: ChannelItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        isAllChannels: Boolean,
        channelItem: ChannelItem,
        onClickListener: (Boolean, ChannelItem, ChannelItemBinding) -> Unit,
    ) {
        binding.textViewChannel.text = String.format("#%s", channelItem.channel.name)
        binding.root.setOnClickListener {
            onClickListener.invoke(isAllChannels, channelItem, binding)
        }
        (binding.recyclerViewTopics.adapter as TopicAdapter).channel = channelItem.channel
    }
}