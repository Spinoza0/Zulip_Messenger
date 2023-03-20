package com.spinoza.messenger_tfs.presentation.model

import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.presentation.adapter.message.DelegateAdapterItem

class MessagesResultDelegate(
    val messages: List<DelegateAdapterItem>,
    val position: MessagePosition,
)