package com.spinoza.messenger_tfs.presentation.adapter.message.date

import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateAdapterItem

class DateDelegateItem(
    private val value: MessageDate,
) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.hashCode().toLong()
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as DateDelegateItem).value == content()
    }
}