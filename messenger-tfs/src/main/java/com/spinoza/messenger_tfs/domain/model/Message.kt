package com.spinoza.messenger_tfs.domain.model

data class Message(
    val date: MessageDate,
    val userId: Int,
    val name: String,
    val content: String,
    val avatarResId: Int,
    val reactions: Map<String, ReactionParam>,
    val isIconAddVisible: Boolean,
    val id: Int = UNDEFINED_ID,
) {
    companion object {
        const val UNDEFINED_ID = -1
    }
}