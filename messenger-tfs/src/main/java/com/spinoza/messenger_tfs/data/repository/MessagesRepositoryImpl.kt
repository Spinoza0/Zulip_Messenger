package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesState
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val state = MutableSharedFlow<MessagesState>(
        replay = COUNT_OF_LAST_EMITTED_VALUES,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val messages = mutableListOf<Message>()

    init {
        // for testing purpose
        messages.addAll(prepareTestData())
    }

    override fun getMessagesState(): MutableSharedFlow<MessagesState> {
        return state
    }

    override suspend fun loadMessages(userId: Int) {
        messages.replaceAll { oldMessage ->
            if (oldMessage.reactions.size > EMPTY_MAP) {
                val newReactions = oldMessage.reactions.toMutableMap()
                newReactions.forEach { entry ->
                    newReactions[entry.key] = ReactionParam(
                        entry.value.usersIds,
                        isSelected = entry.value.usersIds.contains(userId)
                    )
                }
                oldMessage.copy(
                    reactions = newReactions,
                    isIconAddVisible = newReactions.size > EMPTY_MAP
                )
            } else
                oldMessage.copy(isIconAddVisible = false)
        }
        state.emit(MessagesState.Messages(messages))
    }

    override suspend fun sendMessage(message: Message) {
        val newMessage = message.copy(id = messages.size + 1)
        messages.add(newMessage)
        state.emit(MessagesState.MessageSent(messages))
    }

    override suspend fun updateReaction(messageId: Int, userId: Int, reaction: String) {
        var changedMessageId = Message.UNDEFINED_ID
        messages.replaceAll { oldMessage ->
            if (oldMessage.id == messageId) {
                val newReactions = mutableMapOf<String, ReactionParam>()
                var needAddReaction = true

                oldMessage.reactions.forEach { entry ->
                    if (entry.key == reaction) {
                        needAddReaction = false
                        val newUsersIds = mutableListOf<Int>()
                        val isSelected =
                            entry.value.usersIds.removeIfExistsOrAddToList(userId, newUsersIds)
                        if (newUsersIds.size > EMPTY_LIST) {
                            newReactions[entry.key] =
                                entry.value.copy(usersIds = newUsersIds, isSelected = isSelected)
                        } else {
                            newReactions.remove(entry.key)
                        }
                    } else {
                        newReactions[entry.key] = entry.value
                    }
                }

                if (needAddReaction) {
                    newReactions[reaction] = ReactionParam(listOf(userId), isSelected = true)
                }

                changedMessageId = messageId
                oldMessage.copy(
                    reactions = newReactions,
                    isIconAddVisible = newReactions.size > EMPTY_MAP
                )
            } else
                oldMessage
        }
        state.emit(MessagesState.MessageChanged(messages, changedMessageId))
    }

    private fun List<Int>.removeIfExistsOrAddToList(
        value: Int,
        dest: MutableList<Int>,
    ): Boolean {
        var deletedFromList = false
        this.forEach { existingValue ->
            if (existingValue == value) {
                deletedFromList = true
            } else {
                dest.add(existingValue)
            }
        }
        if (!deletedFromList) {
            dest.add(value)
        }
        return !deletedFromList
    }

    companion object {

        private const val EMPTY_MAP = 0
        private const val EMPTY_LIST = 0
        private const val COUNT_OF_LAST_EMITTED_VALUES = 2

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