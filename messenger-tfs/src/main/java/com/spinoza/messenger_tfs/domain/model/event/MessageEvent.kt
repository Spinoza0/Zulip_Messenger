package com.spinoza.messenger_tfs.domain.model.event

import com.spinoza.messenger_tfs.domain.model.MessagesResult

data class MessageEvent(
    val lastEventId: Long,
    val messagesResult: MessagesResult,
)