package com.spinoza.messenger_tfs.stub

import com.spinoza.messenger_tfs.data.network.model.user.UserDto
import com.spinoza.messenger_tfs.data.utils.toDomain
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.MessagesPageType
import com.spinoza.messenger_tfs.domain.model.MessagesResult
import com.spinoza.messenger_tfs.domain.model.RepositoryError
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.WebRepository
import com.spinoza.messenger_tfs.util.ERROR_MSG

class WebRepositoryStub : WebRepository {

    private val ownUser = createUserDto(0)
    private val user = createUserDto(1)

    override suspend fun logIn(
        email: String,
        password: String,
    ): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun getOwnUser(): Result<User> {
        return Result.success(ownUser.toDomain(provideUserPresence()))
    }

    override suspend fun getUser(userId: Long): Result<User> {
        return Result.success(user.toDomain(provideUserPresence()))
    }

    override suspend fun getAllUsers(): Result<List<User>> {
        val presence = provideUserPresence()
        val users = listOf(ownUser.toDomain(presence), user.toDomain(presence))
        return Result.success(users)
    }

    override suspend fun getMessages(
        messagesPageType: MessagesPageType,
        filter: MessagesFilter,
    ): Result<MessagesResult> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun editMessage(
        messageId: Long,
        topic: String,
        content: String,
    ): Result<Long> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getMessageRawContent(messageId: Long, default: String): String = ""

    override suspend fun deleteMessage(messageId: Long): Result<Boolean> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun getUpdatedMessageFilter(filter: MessagesFilter): MessagesFilter {
        return MessagesFilter()
    }

    override suspend fun sendMessage(
        subject: String,
        content: String,
        filter: MessagesFilter,
    ): Result<Long> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun updateReaction(
        messageId: Long,
        emoji: Emoji,
        filter: MessagesFilter,
    ): Result<MessagesResult> {
        return Result.failure(RepositoryError(ERROR_MSG))
    }

    override suspend fun setOwnStatusActive() {}

    override suspend fun setMessagesFlagToRead(messageIds: List<Long>) {}

    private fun createUserDto(id: Long): UserDto {
        return UserDto(userId = id)
    }

    private fun provideUserPresence(): User.Presence {
        return User.Presence.ACTIVE
    }
}