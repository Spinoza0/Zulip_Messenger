package com.spinoza.messenger_tfs.presentation.adapter.channels

import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class TopicDelegateItem(private val value: MessagesFilter) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return (value.channel.hashCode() + value.topic.name.hashCode() * 31).toLong()
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as TopicDelegateItem).value == content()
    }

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? {
        if (newItem !is TopicDelegateItem)
            return null
        val newMessageCount = (newItem.content() as MessagesFilter).topic.messageCount
        return if (value.topic.messageCount == newMessageCount) null
        else newMessageCount
    }
}