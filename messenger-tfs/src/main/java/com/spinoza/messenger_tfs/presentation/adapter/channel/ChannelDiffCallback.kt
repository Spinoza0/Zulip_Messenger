package com.spinoza.messenger_tfs.presentation.adapter.channel

import androidx.recyclerview.widget.DiffUtil
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

class ChannelDiffCallback : DiffUtil.ItemCallback<ChannelItem>() {

    override fun areItemsTheSame(oldItem: ChannelItem, newItem: ChannelItem): Boolean {
        return oldItem.channel.channelId == newItem.channel.channelId
    }

    override fun areContentsTheSame(oldItem: ChannelItem, newItem: ChannelItem): Boolean {
        return oldItem == newItem
    }
}