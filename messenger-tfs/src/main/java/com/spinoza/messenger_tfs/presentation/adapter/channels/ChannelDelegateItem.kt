package com.spinoza.messenger_tfs.presentation.adapter.channels

import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.model.ChannelItem

class ChannelDelegateItem(
    private val value: ChannelItem,
    private val onClickListener: (ChannelItem) -> Unit,
) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.channel.channelId
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as ChannelDelegateItem).value == content()
    }

    fun getOnClickListener(): (ChannelItem) -> Unit {
        return onClickListener
    }
}