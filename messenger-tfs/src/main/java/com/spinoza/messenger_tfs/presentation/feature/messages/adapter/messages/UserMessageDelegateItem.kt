package com.spinoza.messenger_tfs.presentation.feature.messages.adapter.messages

import com.spinoza.messenger_tfs.presentation.feature.messages.model.FlexBoxGravity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

class UserMessageDelegateItem(private val value: Message) : DelegateAdapterItem {

    override fun content(): Any {
        return value
    }

    override fun id(): Long {
        return value.id
    }

    override fun compareToOther(other: DelegateAdapterItem): Boolean {
        return (other as UserMessageDelegateItem).value == value
    }

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? {
        if (newItem !is UserMessageDelegateItem && newItem !is OwnMessageDelegateItem)
            return null
        val newReactions = (newItem.content() as Message).reactions
        return if (value.reactions == newReactions) null
        else newReactions
    }

    fun getGravity(): FlexBoxGravity {
        return FlexBoxGravity.START
    }
}