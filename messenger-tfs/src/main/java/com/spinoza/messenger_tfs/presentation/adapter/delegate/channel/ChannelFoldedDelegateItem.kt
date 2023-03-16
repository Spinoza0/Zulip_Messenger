package com.spinoza.messenger_tfs.presentation.adapter.delegate.channel

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateItem

class ChannelFoldedDelegateItem(
    private val value: Channel,
    private val onChannelClickListener: (Long) -> Unit,
) : DelegateItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.hashCode().toLong()
    }

    override fun compareToOther(other: DelegateItem): Boolean {
        return (other as ChannelFoldedDelegateItem).value == content()
    }

    fun getOnChannelClickListener(): (Long) -> Unit {
        return onChannelClickListener
    }
}