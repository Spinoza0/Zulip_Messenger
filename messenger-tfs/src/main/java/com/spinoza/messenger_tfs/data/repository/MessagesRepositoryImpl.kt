package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.model.TopicDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import kotlinx.coroutines.delay
import java.util.*


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    // TODO: for testing purpose
    private val currentUser = testUserDto

    private val messagesLocalCache = TreeSet<MessageDto>()

    init {
        // TODO: for testing purpose
        messagesLocalCache.addAll(prepareTestData())
    }

    override suspend fun getCurrentUser(): RepositoryResult<User> {
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(currentUser.toDomain())
        } else {
            RepositoryResult.Failure.CurrentUserNotFound(errorText)
        }
    }

    override suspend fun getUser(userId: Long): RepositoryResult<User> {
        val user = usersDto.find { it.userId == userId }
        return if (user != null)
            RepositoryResult.Success(user.toDomain())
        else
            RepositoryResult.Failure.UserNotFound(userId)
    }

    override suspend fun getUsersByFilter(usersFilter: String): RepositoryResult<List<User>> {
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(usersDto.listToDomain(usersFilter))
        } else {
            RepositoryResult.Failure.LoadingUsers(errorText)
        }
    }

    override suspend fun getMessages(
        messagesFilter: MessagesFilter,
    ): RepositoryResult<MessagesResult> {
        // TODO: for testing purpose
        delay(1000)
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(
                MessagesResult(
                    messagesLocalCache.toDomain(currentUser.userId, messagesFilter),
                    MessagePosition()
                )
            )
        } else {
            RepositoryResult.Failure.LoadingMessages(messagesFilter)
        }
    }

    override suspend fun getAllChannels(
        channelsFilter: ChannelsFilter,
    ): RepositoryResult<List<Channel>> {
        // TODO: for testing purpose
        delay(1000)
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(channelsDto.toDomain(channelsFilter))
        } else {
            RepositoryResult.Failure.LoadingChannels(channelsFilter)
        }
    }

    // TODO: "Not yet implemented"
    override suspend fun getSubscribedChannels(
        channelsFilter: ChannelsFilter,
    ): RepositoryResult<List<Channel>> {
        return getAllChannels(channelsFilter)
    }

    override suspend fun getTopics(channel: Channel): RepositoryResult<List<Topic>> {
        val topics = channelsDto
            .find { it.id == channel.channelId }
            ?.topics
            ?.toDomain(messagesLocalCache, channel.channelId) ?: listOf()
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(topics)
        } else {
            RepositoryResult.Failure.LoadingChannelTopics(channel)
        }
    }

    override suspend fun getTopic(
        messagesFilter: MessagesFilter,
    ): RepositoryResult<Topic> {
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(
                TopicDto(messagesFilter.topic.name, Message.UNDEFINED_ID)
                    .toDomain(messagesLocalCache, messagesFilter.channel.channelId)
            )
        } else {
            RepositoryResult.Failure.LoadingTopicData(messagesFilter)
        }
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
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(
                MessagesResult(
                    messagesLocalCache.toDomain(message.user.userId, messagesFilter),
                    MessagePosition(type = MessagePosition.Type.LAST_POSITION)
                )
            )
        } else {
            RepositoryResult.Failure.SendingMessage(errorText)
        }
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
        return if (!isErrorInRepository()) {
            RepositoryResult.Success(
                MessagesResult(
                    messagesLocalCache.toDomain(currentUser.userId, messagesFilter),
                    MessagePosition(type = MessagePosition.Type.EXACTLY, messageId = messageId)
                )
            )
        } else {
            RepositoryResult.Failure.UpdatingReaction(errorText)
        }
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