package com.spinoza.messenger_tfs.domain.model

data class MessagesResult(
    val topic: Topic,
    val messages: List<Message>,
    val position: MessagePosition,
    val isNewMessageExisting: Boolean = false,
)