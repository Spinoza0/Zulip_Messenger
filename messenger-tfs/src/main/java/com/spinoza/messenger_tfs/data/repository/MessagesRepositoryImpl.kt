package com.spinoza.messenger_tfs.data.repository

import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessagesState
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.utils.removeIfExistsOrAddToList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val state = MutableSharedFlow<MessagesState>(
        replay = COUNT_OF_LAST_EMITTED_VALUES,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val messages = mutableListOf<Message>()

    init {
        messages.addAll(prepareTestData())
    }

    override fun getMessagesState(): MutableSharedFlow<MessagesState> {
        return state
    }

    override suspend fun loadMessages(userId: Int) {
        loadMessages(userId, false)
    }

    override suspend fun sendMessage(message: Message) {
        val newMessage = message.copy(id = messages.size + 1)
        messages.add(newMessage)
        loadMessages(message.userId, true)
    }

    override suspend fun updateReaction(messageId: Int, userId: Int, reaction: String) {
        messages.replaceAll { oldMessage ->
            if (oldMessage.id == messageId) {
                val newReactions = mutableMapOf<String, ReactionParam>()
                var needAddReaction = true

                oldMessage.reactions.forEach { entry ->
                    if (entry.key == reaction) {
                        needAddReaction = false
                        val newUsersIds =
                            entry.value.usersIds.removeIfExistsOrAddToList(userId)
                        if (newUsersIds.size > EMPTY_LIST) {
                            newReactions[entry.key] = entry.value.copy(usersIds = newUsersIds)
                        } else {
                            newReactions.remove(entry.key)
                        }
                    } else {
                        newReactions[entry.key] = entry.value
                    }
                }
                if (needAddReaction) {
                    newReactions[reaction] = ReactionParam(listOf(userId))
                }
                oldMessage.copy(reactions = newReactions)
            } else
                oldMessage
        }
        loadMessages(userId)
    }

    private suspend fun loadMessages(userId: Int, messageWasAdded: Boolean) {
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
        state.emit(MessagesState.Messages(messages, messageWasAdded))
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