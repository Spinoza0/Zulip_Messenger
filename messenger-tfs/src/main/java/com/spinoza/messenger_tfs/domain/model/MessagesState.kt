package com.spinoza.messenger_tfs.domain.model

sealed class MessagesState {
    class Error(val text: String) : MessagesState()
    class Messages(val messages: List<Message>, val position: MessagePosition) : MessagesState()
}