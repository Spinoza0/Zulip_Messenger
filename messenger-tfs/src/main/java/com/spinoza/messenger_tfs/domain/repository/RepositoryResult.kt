package com.spinoza.messenger_tfs.domain.repository

sealed class RepositoryResult<T> {

    class Success<T>(val value: T) : RepositoryResult<T>()

    sealed class Failure<T> : RepositoryResult<T>() {

        class UserNotFound<T>(val userId: Long) : Failure<T>()

        class MessageNotFound<T>(val messageId: Long) : Failure<T>()
    }
}