package com.spinoza.messenger_tfs.presentation.feature.messages.adapter.date

import com.spinoza.messenger_tfs.domain.model.MessageDateTime
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

class DateDelegateItem(
    private val value: MessageDateTime,
) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.hashCode().toLong()
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as DateDelegateItem).value == value
    }

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? {
        return null
    }
}