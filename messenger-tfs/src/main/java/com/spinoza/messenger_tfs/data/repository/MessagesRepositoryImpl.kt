package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.prepareTestData
import com.spinoza.messenger_tfs.data.streamsDto
import com.spinoza.messenger_tfs.data.toDomain
import com.spinoza.messenger_tfs.data.toDto
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*


// TODO: 1) возможно, есть смысл отказаться от Flow, использовать просто suspend functions
// TODO: 2) отрефакторить - не все сообщения сразу эмиттить, а только новые или измененные
// TODO: 3) отрефакторить - кэш (messagesLocalCache) вынести в отдельный класс

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val userId = TEST_USER_ID

    private val state = MutableStateFlow<RepositoryState>(
        RepositoryState.Idle
    )

    private val messagesLocalCache = TreeSet<MessageDto>()

    init {
        // for testing purpose
        messagesLocalCache.addAll(prepareTestData())
    }

    override fun getUserId(): Long {
        return userId
    }

    // TODO: подумать о вынесении в init или конструктор, не забыть про userId - скорее всего
    //  userId будет передаваться через метод фильтрации (stream/topic) или к этому моменту его
    //  передача перестанет быть актуальной (поменяется структура данных)
    override fun getState(): StateFlow<RepositoryState> {
        state.value = RepositoryState.Messages(messagesLocalCache.toDomain(userId))
        return state.asStateFlow()
    }

    override fun getAllChannels() {
        state.value = RepositoryState.Channels(streamsDto.toDomain())
    }

    override fun getSubscribedChannels() {
        getAllChannels()
        // TODO: "Not yet implemented"
    }

    override suspend fun sendMessage(message: Message) {
        val newMessageId = if (message.id == Message.UNDEFINED_ID) {
            messagesLocalCache.size.toLong()
        } else {
            message.id
        }
        messagesLocalCache.add(message.toDto(message.userId, newMessageId))
        state.emit(
            RepositoryState.Messages(
                messagesLocalCache.toDomain(message.userId),
                MessagePosition(type = MessagePosition.Type.LAST_POSITION)
            )
        )
    }

    override suspend fun updateReaction(messageId: Long, reaction: String) {
        val messageDto = messagesLocalCache.find { it.id == messageId } ?: return
        val reactionDto = messageDto.reactions[reaction]
        val newReactionsDto = messageDto.reactions.toMutableMap()

        if (reactionDto != null) {
            val newUsersIds = reactionDto.usersIds.removeIfExistsOrAddToList(userId)
            if (newUsersIds.isNotEmpty()) {
                newReactionsDto[reaction] = ReactionParamDto(newUsersIds)
            } else {
                newReactionsDto.remove(reaction)
            }
        } else {
            newReactionsDto[reaction] = ReactionParamDto(listOf(userId))
        }

        messagesLocalCache.removeIf { it.id == messageId }
        messagesLocalCache.add(messageDto.copy(reactions = newReactionsDto))
        state.emit(
            RepositoryState.Messages(
                messagesLocalCache.toDomain(userId),
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

        // TODO: for testing purpose
        const val TEST_USER_ID = 100L

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