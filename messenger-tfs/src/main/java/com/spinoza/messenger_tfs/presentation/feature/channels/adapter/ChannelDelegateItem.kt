package com.spinoza.messenger_tfs.presentation.feature.channels.adapter

import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.feature.channels.model.ChannelItem

class ChannelDelegateItem(private val value: ChannelItem) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.channel.channelId
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as ChannelDelegateItem).value == value
    }

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? {
        return null
    }
}