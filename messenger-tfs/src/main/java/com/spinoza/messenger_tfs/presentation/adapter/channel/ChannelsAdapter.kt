package com.spinoza.messenger_tfs.presentation.adapter.channel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.spinoza.messenger_tfs.databinding.ChannelItemBinding
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.topic.TopicAdapter

class ChannelsAdapter(
    private val evenColor: Int,
    private val oddColor: Int,
    private val onChannelClickListener: (Channel, ChannelItemBinding) -> Unit,
    private val onTopicClickListener: (Channel, String) -> Unit,
) : ListAdapter<Channel, ChannelViewHolder>(ChannelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ChannelItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.recyclerViewTopics.adapter = TopicAdapter(evenColor, oddColor)
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(getItem(position), onChannelClickListener, onTopicClickListener)
    }
}