package com.spinoza.messenger_tfs.presentation.adapter.message.messages

import com.spinoza.messenger_tfs.domain.model.FlexBoxGravity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class UserMessageDelegateItem(private val value: Message) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.id
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as UserMessageDelegateItem).value == content()
    }

    fun getGravity(): FlexBoxGravity {
        return FlexBoxGravity.END
    }
}