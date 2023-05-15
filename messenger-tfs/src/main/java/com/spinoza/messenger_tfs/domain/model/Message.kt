package com.spinoza.messenger_tfs.domain.model

data class Message(
    val datetime: MessageDateTime,
    val user: User,
    val content: String,
    val subject: String,
    val reactions: Map<Emoji, ReactionParam>,
    val id: Long = UNDEFINED_ID,
) {
    companion object {
        const val UNDEFINED_ID = -1L
    }
}