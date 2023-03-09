package com.spinoza.messenger_tfs.domain.model

data class Message(
    val datetime: String,
    val user: User,
    val text: String,
    val reactions: Map<Reaction, List<User>>,
    val iconAddVisibility: Boolean,
    val id: Int = UNDEFINED_ID,
) {
    companion object {
        const val UNDEFINED_ID = -1
    }
}