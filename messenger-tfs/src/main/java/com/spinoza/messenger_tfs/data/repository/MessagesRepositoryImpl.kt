package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import java.util.*


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    // TODO: for testing purpose
    private val currentUser = testUserDto

    private val messagesLocalCache = TreeSet<MessageDto>()

    init {
        // for testing purpose
        messagesLocalCache.addAll(prepareTestData())
    }

    override fun getCurrentUser(): RepositoryResult<User> {
        return RepositoryResult.Success(currentUser.toDomain())
    }

    override suspend fun getUser(userId: Long): RepositoryResult<User> {
        val user = usersDto.find { it.userId == userId }
        return if (user != null)
            RepositoryResult.Success(user.toDomain())
        else
            RepositoryResult.Failure.UserNotFound(userId)
    }

    override suspend fun getAllUsers(): RepositoryResult<List<User>> {
        return RepositoryResult.Success(usersDto.listToDomain())
    }

    override suspend fun getMessages(
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> {
        return RepositoryResult.Success(
            MessagesResult(
                messagesLocalCache.toDomain(currentUser.userId, messagesFilter),
                MessagePosition()
            )
        )
    }

    override suspend fun getAllChannels(): RepositoryResult<List<Channel>> {
        return RepositoryResult.Success(channelsDto.toDomain())
    }

    // TODO: "Not yet implemented"
    override suspend fun getSubscribedChannels(): RepositoryResult<List<Channel>> {
        return getAllChannels()
    }

    override suspend fun getTopics(channelId: Long): RepositoryResult<List<Topic>> {
        val topics = channelsDto
            .find { it.id == channelId }
            ?.topics
            ?.toDomain(messagesLocalCache, channelId) ?: listOf()
        return RepositoryResult.Success(topics)
    }

    override suspend fun sendMessage(
        message: Message,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> {
        val newMessageId = if (message.id == Message.UNDEFINED_ID) {
            messagesLocalCache.size.toLong()
        } else {
            message.id
        }
        messagesLocalCache.add(
            message.toDto(
                userId = message.user.userId,
                messageId = newMessageId,
                messagesFilter = messagesFilter
            )
        )
        return RepositoryResult.Success(
            MessagesResult(
                messagesLocalCache.toDomain(message.user.userId, messagesFilter),
                MessagePosition(type = MessagePosition.Type.LAST_POSITION)
            )
        )
    }

    override suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> {
        val messageDto = messagesLocalCache
            .find { it.id == messageId }
            ?: return RepositoryResult.Failure.MessageNotFound(messageId)

        val reactionDto = messageDto.reactions[reaction]
        val newReactionsDto = messageDto.reactions.toMutableMap()

        if (reactionDto != null) {
            val newUsersIds = reactionDto.usersIds.removeIfExistsOrAddToList(currentUser.userId)
            if (newUsersIds.isNotEmpty()) {
                newReactionsDto[reaction] = ReactionParamDto(newUsersIds)
            } else {
                newReactionsDto.remove(reaction)
            }
        } else {
            newReactionsDto[reaction] = ReactionParamDto(listOf(currentUser.userId))
        }

        messagesLocalCache.removeIf { it.id == messageId }
        messagesLocalCache.add(messageDto.copy(reactions = newReactionsDto))
        return RepositoryResult.Success(
            MessagesResult(
                messagesLocalCache.toDomain(currentUser.userId, messagesFilter),
                MessagePosition(type = MessagePosition.Type.EXACTLY, messageId = messageId)
            )
        )
    }

    private fun List<Long>.removeIfExistsOrAddToList(value: Long): List<Long> {
        val result = mutableListOf<Long>()
        var deletedFromList = false
        this.forEach { existingValue ->
            if (existingValue == value) {
                deletedFromList = true
            } else {
                result.add(existingValue)
            }
        }
        if (!deletedFromList) {
            result.add(value)
        }
        return result
    }

    companion object {

        private var instance: MessagesRepositoryImpl? = null
        private val LOCK = Unit

        fun getInstance(): MessagesRepositoryImpl {
            instance?.let {
                return it
            }
            synchronized(LOCK) {
                instance?.let {
                    return it
                }
                val newInstance = MessagesRepositoryImpl()
                instance = newInstance
                return newInstance
            }
        }
    }
}