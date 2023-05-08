package com.spinoza.messenger_tfs.presentation.feature.messages.adapter.topic

import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

class MessagesTopicDelegateItem(private val value: String) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.hashCode().toLong()
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as MessagesTopicDelegateItem).value.equals(value, ignoreCase = true)
    }

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? {
        return null
    }
}