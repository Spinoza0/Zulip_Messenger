package com.spinoza.messenger_tfs.presentation.adapter.message.date

import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateItem

class DateDelegateItem(
    private val value: MessageDate,
) : DelegateItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.hashCode().toLong()
    }

    override fun compareToOther(other: DelegateItem): Boolean {
        return (other as DateDelegateItem).value == content()
    }
}