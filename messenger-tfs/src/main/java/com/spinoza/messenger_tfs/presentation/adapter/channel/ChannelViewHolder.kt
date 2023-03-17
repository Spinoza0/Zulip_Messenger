package com.spinoza.messenger_tfs.presentation.adapter.channel

import androidx.recyclerview.widget.RecyclerView
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter

class ChannelViewHolder(private val binding: ChannelItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        allChannels: Boolean,
        channel: Channel,
        onClickListener: (Boolean, Channel, ChannelItemBinding) -> Unit,
        onTopicClickListener: (Channel, String) -> Unit,
    ) {
        binding.textViewChannel.text = String.format("#%s", channel.name)
        binding.root.setOnClickListener {
            onClickListener.invoke(allChannels, channel, binding)
        }
        with(binding.recyclerViewTopics.adapter as TopicAdapter) {
            this.channel = channel
            this.onClickListener = onTopicClickListener
        }
    }
}
