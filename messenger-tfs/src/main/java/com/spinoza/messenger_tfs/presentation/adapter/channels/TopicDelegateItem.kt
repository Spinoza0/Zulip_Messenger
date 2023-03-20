package com.spinoza.messenger_tfs.presentation.adapter.channels

import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class TopicDelegateItem(private val value: ChannelFilter) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.hashCode().toLong()
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as TopicDelegateItem).value == content()
    }
}