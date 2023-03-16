package com.spinoza.messenger_tfs.domain.model

sealed class RepositoryState {

    object Idle : RepositoryState()

    class Error(val text: String) : RepositoryState()

    class Messages(
        val messages: List<Message>,
        val position: MessagePosition = MessagePosition(),
    ) : RepositoryState()
}