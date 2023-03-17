package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.Topic

sealed class RepositoryState {

    class Error(val text: String) : RepositoryState()

    class Messages(
        val messages: List<Message>,
        val position: MessagePosition = MessagePosition(),
    ) : RepositoryState()

    class Channels(val channels: List<Channel>) : RepositoryState()

    class Topics(val topics: List<Topic>) : RepositoryState()
}