package com.spinoza.messenger_tfs.domain.model

class MessagePosition(
    val type: Type = Type.UNDEFINED,
    val id: Int = UNDEFINED,
) {
    enum class Type {
        UNDEFINED,
        LAST_POSITION,
        EXACTLY
    }

    companion object {
        const val UNDEFINED = -1
    }
}