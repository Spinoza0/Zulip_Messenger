package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.*
import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.repository.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult.Type
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

    override fun getCurrentUser(): Pair<RepositoryResult, User> {
        return Pair(RepositoryResult(Type.SUCCESS), currentUser.toDomain())
    }

    override suspend fun getUser(userId: Long): Pair<RepositoryResult, User?> {
        val user = usersDto.find { it.userId == userId }
        return if (user != null)
            Pair(RepositoryResult(Type.SUCCESS), user.toDomain())
        else
            Pair(RepositoryResult(Type.ERROR_USER_WITH_ID_NOT_FOUND, "$userId"), null)
    }

    override suspend fun getAllUsers(): Pair<RepositoryResult, List<User>> {
        return Pair(RepositoryResult(Type.SUCCESS), usersDto.listToDomain())
    }

    override suspend fun getMessages(
        channelFilter: ChannelFilter,
    ): Pair<RepositoryResult, MessagesResult?> {
        return Pair(
            RepositoryResult(Type.SUCCESS),
            MessagesResult(
                messagesLocalCache.toDomain(currentUser.userId, channelFilter),
                MessagePosition()
            )
        )
    }

    override suspend fun getAllChannels(): Pair<RepositoryResult, List<Channel>> {
        return Pair(RepositoryResult(Type.SUCCESS), channelsDto.toDomain())
    }

    // TODO: "Not yet implemented"
    override suspend fun getSubscribedChannels(): Pair<RepositoryResult, List<Channel>> {
        return getAllChannels()
    }

    override suspend fun getTopics(channelId: Long): Pair<RepositoryResult, List<Topic>> {
        val topics = channelsDto
            .find { it.id == channelId }
            ?.topics
            ?.toDomain(messagesLocalCache, channelId) ?: listOf()
        return Pair(RepositoryResult(Type.SUCCESS), topics)
    }

    override suspend fun sendMessage(
        message: Message,
        channelFilter: ChannelFilter,
    ): Pair<RepositoryResult, MessagesResult?> {
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
        return Pair(
            RepositoryResult(Type.SUCCESS),
            MessagesResult(
                messagesLocalCache.toDomain(message.user.userId, channelFilter),
                MessagePosition(type = MessagePosition.Type.LAST_POSITION)
            )
        )
    }

    override suspend fun updateReaction(
        messageId: Long,
        reaction: String,
        channelFilter: ChannelFilter,
    ): Pair<RepositoryResult, MessagesResult?> {
        val messageDto = messagesLocalCache
            .find { it.id == messageId }
            ?: return Pair(
                RepositoryResult(Type.ERROR_MESSAGE_WITH_ID_NOT_FOUND, "$messageId"),
                null
            )

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
        return Pair(
            RepositoryResult(Type.SUCCESS),
            MessagesResult(
                messagesLocalCache.toDomain(currentUser.userId, channelFilter),
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