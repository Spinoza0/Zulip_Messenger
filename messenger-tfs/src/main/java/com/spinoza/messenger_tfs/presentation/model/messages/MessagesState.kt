package com.spinoza.messenger_tfs.presentation.model.messages

data class MessagesState(
    val isLoading: Boolean = false,
    val isSendingMessage: Boolean = false,
    val messages: MessagesResultDelegate? = null,
)