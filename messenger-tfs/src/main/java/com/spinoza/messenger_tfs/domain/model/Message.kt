package com.spinoza.messenger_tfs.domain.model

data class Message(
    val date: MessageDate,
    val user: User,
    val content: String,
    val reactions: Map<String, ReactionParam>,
    val isIconAddVisible: Boolean,
    val id: Long = UNDEFINED_ID,
) {
    companion object {
        const val UNDEFINED_ID = -1L
    }
}