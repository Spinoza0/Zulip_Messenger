package com.spinoza.messenger_tfs.domain.repository

sealed class RepositoryResult<out T> {

    class Success<T>(val value: T) : RepositoryResult<T>()

    sealed class Failure : RepositoryResult<Nothing>() {

        class UserNotFound(val userId: Long) : Failure()

        class MessageNotFound(val messageId: Long) : Failure()
    }
}