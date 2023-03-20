package com.spinoza.messenger_tfs.presentation.adapter.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapterConfig
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

class ChannelsAdapter(
    private val isAllChannels: Boolean,
    private val topicConfig: TopicAdapterConfig,
    private val onChannelClickListener: (Boolean, ChannelItem, ChannelItemBinding) -> Unit,
) : ListAdapter<ChannelItem, ChannelViewHolder>(ChannelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ChannelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.recyclerViewTopics.adapter = TopicAdapter(topicConfig)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(isAllChannels, getItem(position), onChannelClickListener)
    }
}