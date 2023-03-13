package com.spinoza.messenger_tfs.domain.model

sealed class MessagesState {
    class ReadyToSend(val status: Boolean) : MessagesState()
    class Error(val text: String) : MessagesState()
    class Messages(val messages: List<Message>) : MessagesState()
    class MessageSent(val messages: List<Message>) : MessagesState()
    class MessageChanged(val messages: List<Message>, val changedMessageId: Int) : MessagesState()
}