package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Message

class MessagePosition(
    val type: Type = Type.UNDEFINED,
    val messageId: Long = Message.UNDEFINED_ID,
) {
    enum class Type {
        UNDEFINED,
        LAST_POSITION,
        EXACTLY
    }
}