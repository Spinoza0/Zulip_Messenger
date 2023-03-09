package com.spinoza.messenger_tfs.presentation.adapter.message

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.utils.DelegateItem

class CompanionMessageDelegateItem(
    private val id: Int,
    private val value: Message,
) : DelegateItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Int {
        return id
    }

    override fun compareToOther(other: DelegateItem): Boolean {
        return (other as CompanionMessageDelegateItem).value == content()
    }
}