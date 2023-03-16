package com.spinoza.messenger_tfs.domain.model

class MessagePosition(
    val type: Type = Type.UNDEFINED,
    val messageId: Int = Message.UNDEFINED_ID,
) {
    enum class Type {
        UNDEFINED,
        LAST_POSITION,
        EXACTLY
    }
}