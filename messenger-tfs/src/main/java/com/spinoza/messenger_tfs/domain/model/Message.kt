package com.spinoza.messenger_tfs.domain.model

class Message(
    val user: User,
    val text: String,
    val date: String,
    val reactions: Map<Reaction, List<User>>,
    val iconAddVisibility: Boolean,
)