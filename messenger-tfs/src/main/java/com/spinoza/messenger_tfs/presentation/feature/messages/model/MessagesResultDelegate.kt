package com.spinoza.messenger_tfs.presentation.feature.messages.model

import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.presentation.feature.app.adapter.DelegateAdapterItem

data class MessagesResultDelegate(
    val messages: List<DelegateAdapterItem>,
    val position: MessagePosition,
    val isNewMessageExists: Boolean
)