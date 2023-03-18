package com.spinoza.messenger_tfs.presentation.adapter.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapterConfig

class ChannelsAdapter(
    private val allChannels: Boolean,
    private val topicConfig: TopicAdapterConfig,
    private val onChannelClickListener: (Boolean, Channel, ChannelItemBinding) -> Unit,
    private val onTopicClickListener: (Channel, String) -> Unit,
) : ListAdapter<Channel, ChannelViewHolder>(ChannelDiffCallback()) {

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
        holder.bind(allChannels, getItem(position), onChannelClickListener, onTopicClickListener)
    }
}