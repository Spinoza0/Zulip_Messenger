package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import java.util.*


// TODO: 1) отрефакторить - не все сообщения сразу отправлять, а только новые или измененные
// TODO: 2) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    // TODO: for testing purpose
    private val user = testUserDto

    private val messagesLocalCache = TreeSet<MessageDto>()

    init {
        // for testing purpose
        messagesLocalCache.addAll(prepareTestData())
    }

    override fun getUser(): User {
        return user.toDomain()
    }

    override suspend fun getMessages(channelFilter: ChannelFilter): RepositoryState {
        return RepositoryState.Messages(messagesLocalCache.toDomain(user.userId, channelFilter))
    }

    override suspend fun getAllChannels(): RepositoryState {
        return RepositoryState.Channels(channelsDto.toDomain())
    }

    // TODO: "Not yet implemented"
    override suspend fun getSubscribedChannels(): RepositoryState {
        return getAllChannels()
    }

    override suspend fun getTopics(channelId: Long): RepositoryState {
        val topics = channelsDto
            .find { it.id == channelId }
            ?.topics
            ?.toDomain(messagesLocalCache, channelId) ?: listOf()
        return RepositoryState.Topics(topics)
    }

    override suspend fun sendMessage(
        message: Message,
        channelFilter: ChannelFilter,
    ): RepositoryState {
        val newMessageId = if (message.id == Message.UNDEFINED_ID) {
            messagesLocalCache.size.toLong()
        } else {
            message.id
        }
        messagesLocalCache.add(
            message.toDto(
                userId = message.user.userId,
                messageId = newMessageId,
                channelId = channelFilter.channelId,
                topicName = channelFilter.topicName
            )
        )
        return RepositoryState.Messages(
            messagesLocalCache.toDomain(message.user.userId, channelFilter),
            MessagePosition(type = MessagePosition.Type.LAST_POSITION)
        )
    }

    override suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        channelFilter: ChannelFilter,
    ): RepositoryState {
        val messageDto = messagesLocalCache.find { it.id == messageId }
            ?: return RepositoryState.Error(String.format(ERROR_USER_NOT_FOUND, user.userId))

        val reactionDto = messageDto.reactions[reaction]
        val newReactionsDto = messageDto.reactions.toMutableMap()

        if (reactionDto != null) {
            val newUsersIds = reactionDto.usersIds.removeIfExistsOrAddToList(user.userId)
            if (newUsersIds.isNotEmpty()) {
                newReactionsDto[reaction] = ReactionParamDto(newUsersIds)
            } else {
                newReactionsDto.remove(reaction)
            }
        } else {
            newReactionsDto[reaction] = ReactionParamDto(listOf(user.userId))
        }

        messagesLocalCache.removeIf { it.id == messageId }
        messagesLocalCache.add(messageDto.copy(reactions = newReactionsDto))
        return RepositoryState.Messages(
            messagesLocalCache.toDomain(user.userId, channelFilter),
            MessagePosition(type = MessagePosition.Type.EXACTLY, messageId = messageId)
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

        // TODO: extract to string resources
        private const val ERROR_USER_NOT_FOUND = "User %s not found"

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