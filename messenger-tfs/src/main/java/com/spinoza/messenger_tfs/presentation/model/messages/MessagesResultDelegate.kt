package com.spinoza.messenger_tfs.presentation.model.messages

import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

data class MessagesResultDelegate(
    val messages: List<DelegateAdapterItem>,
    val position: MessagePosition,
)