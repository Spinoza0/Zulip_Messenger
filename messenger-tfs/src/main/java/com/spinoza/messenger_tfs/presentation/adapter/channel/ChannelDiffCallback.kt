package com.spinoza.messenger_tfs.presentation.adapter.channel

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.domain.model.Channel

class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {

    override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
        return oldItem.channelId == newItem.channelId
    }

    override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
        return oldItem == newItem
    }
}