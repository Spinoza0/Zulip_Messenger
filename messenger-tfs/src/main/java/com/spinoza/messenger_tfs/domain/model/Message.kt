package com.spinoza.messenger_tfs.domain.model

class Message(
    val id: Int,
    val datetime: String,
    val user: User,
    val text: String,
    val reactions: Map<Reaction, List<User>>,
    val iconAddVisibility: Boolean,
)