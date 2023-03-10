package com.spinoza.messenger_tfs.domain.model

sealed class RepositoryState {
    class Error(val text: String) : RepositoryState()
    class Messages(
        val messages: List<Message>,
        val needScrollToLastPosition: Boolean,
    ) : RepositoryState()
}