package com.spinoza.messenger_tfs.domain.model

data class MessagesResult(
    val messages: List<Message>,
    val position: MessagePosition,
    val isNewMessageExists: Boolean = false,
)