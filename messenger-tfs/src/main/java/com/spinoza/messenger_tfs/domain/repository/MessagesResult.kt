package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message

data class MessagesResult(
    val messages: List<Message>,
    val position: MessagePosition,
)