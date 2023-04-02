package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem

class MessagesResultDelegate(
    val messages: List<DelegateAdapterItem>,
    val position: MessagePosition,
)