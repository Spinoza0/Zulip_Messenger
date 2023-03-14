package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.data.model.MessageDto
import com.spinoza.messenger_tfs.data.model.ReactionParamDto
import com.spinoza.messenger_tfs.data.toDto
import com.spinoza.messenger_tfs.data.toEntity
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagePosition
import com.spinoza.messenger_tfs.domain.model.MessagesState
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val state = MutableSharedFlow<MessagesState>(
        replay = COUNT_OF_LAST_EMITTED_VALUES,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val messagesDto = TreeSet<MessageDto>()

    init {
        // for testing purpose
        messagesDto.addAll(prepareTestData())
    }

    override fun getMessagesState(): SharedFlow<MessagesState> {
        return state.asSharedFlow()
    }

    override suspend fun loadMessages(userId: Int) {
        state.emit(MessagesState.Messages(messagesDto.toEntity(userId), MessagePosition()))
    }

    override suspend fun sendMessage(message: Message) {
        val newMessage = message.copy(id = messagesDto.size + 1)
        val newMessageId = if (message.id == Message.UNDEFINED_ID) {
            messagesDto.size
        } else {
            message.id
        }
        messagesDto.add(newMessage.toDto(message.userId, newMessageId))
        state.emit(
            MessagesState.Messages(
                messagesDto.toEntity(message.userId),
                MessagePosition(type = MessagePosition.Type.LAST_POSITION)
            )
        )
    }

    override suspend fun updateReaction(messageId: Int, userId: Int, reaction: String) {
        val messageDto = messagesDto.find { it.id == messageId } ?: return
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

        messagesDto.removeIf { it.id == messageId }
        messagesDto.add(messageDto.copy(reactions = newReactionsDto))
        state.emit(
            MessagesState.Messages(
                messagesDto.toEntity(userId),
                MessagePosition(type = MessagePosition.Type.EXACTLY, id = messageId)
            )
        )
    }

    private fun List<Int>.removeIfExistsOrAddToList(value: Int): List<Int> {
        val result = mutableListOf<Int>()
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

        private const val COUNT_OF_LAST_EMITTED_VALUES = 1

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