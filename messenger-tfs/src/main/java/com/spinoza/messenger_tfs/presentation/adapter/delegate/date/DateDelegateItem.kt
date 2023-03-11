package com.spinoza.messenger_tfs.presentation.adapter.delegate.date

import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.presentation.adapter.utils.DelegateItem

class DateDelegateItem(
    private val id: Int,
    private val value: MessageDate,
) : DelegateItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Int {
        return id
    }

    override fun compareToOther(other: DelegateItem): Boolean {
        return (other as DateDelegateItem).value == content()
    }
}