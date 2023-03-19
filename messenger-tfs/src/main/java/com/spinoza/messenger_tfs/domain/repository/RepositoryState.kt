package com.spinoza.messenger_tfs.domain.repository

import com.spinoza.messenger_tfs.domain.model.*

sealed class RepositoryState {

    object Loading : RepositoryState()

    class Error(val type: ErrorType, val text: String) : RepositoryState()

    class Messages(
        val messages: List<Message>,
        val position: MessagePosition = MessagePosition(),
    ) : RepositoryState()

    class Channels(val value: List<Channel>) : RepositoryState()

    class Topics(val value: List<Topic>) : RepositoryState()

    class Users(val value: List<User>) : RepositoryState()

    enum class ErrorType {
        USER_WITH_ID_NOT_FOUND
    }
}