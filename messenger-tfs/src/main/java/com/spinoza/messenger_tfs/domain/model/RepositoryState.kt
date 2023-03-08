package com.spinoza.messenger_tfs.domain.model

sealed class RepositoryState {
    class Error(val text: String) : RepositoryState()
    class Messages(val messages: List<MessageEntity>) : RepositoryState()
    class Reactions(val messageId: String, val reactions: List<ReactionEntity>) : RepositoryState()
}