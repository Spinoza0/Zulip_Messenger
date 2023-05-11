package com.spinoza.messenger_tfs.presentation.feature.channels.adapter

import com.spinoza.messenger_tfs.domain.util.EMPTY_STRING
import com.spinoza.messenger_tfs.presentation.adapter.DelegateAdapterItem

class CreateChannelDelegateItem : DelegateAdapterItem {

    private val value = EMPTY_STRING

    override fun content(): Any = value

    override fun id(): Long = value.hashCode().toLong()

    override fun compareToOther(other: DelegateAdapterItem): Boolean = true

    override fun getChangePayload(newItem: DelegateAdapterItem): Any? = null
}