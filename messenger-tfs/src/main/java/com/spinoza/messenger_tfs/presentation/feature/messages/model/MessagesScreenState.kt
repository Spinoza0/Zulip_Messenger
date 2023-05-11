package com.spinoza.messenger_tfs.presentation.feature.messages.model

data class MessagesScreenState(
    val isLoading: Boolean = false,
    val isLongOperation: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isNextMessageExisting: Boolean = false,
    val isNewMessageExisting: Boolean = false,
    val messages: MessagesResultDelegate? = null,
)