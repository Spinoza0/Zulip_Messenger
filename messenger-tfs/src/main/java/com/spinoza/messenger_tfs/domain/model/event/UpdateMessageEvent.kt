package com.spinoza.messenger_tfs.domain.model.event

import com.spinoza.messenger_tfs.domain.model.MessagesResult

data class UpdateMessageEvent(
    val lastEventId: Long,
    val messagesResult: MessagesResult,
)