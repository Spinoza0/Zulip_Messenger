package com.spinoza.messenger_tfs.presentation.adapter.message.messages

import com.spinoza.messenger_tfs.presentation.model.FlexBoxGravity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class OwnMessageDelegateItem(private val value: Message) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.id
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as OwnMessageDelegateItem).value == content()
    }

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? {
        if (newItem !is UserMessageDelegateItem && newItem !is OwnMessageDelegateItem)
            return null
        val newReactions = (newItem.content() as Message).reactions
        return if (value.reactions == newReactions) null
        else newReactions
    }

    fun getGravity(): FlexBoxGravity {
        return FlexBoxGravity.END
    }
}