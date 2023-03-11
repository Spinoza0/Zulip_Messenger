package com.spinoza.messenger_tfs.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.ReactionParam
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository
import com.spinoza.messenger_tfs.domain.utils.removeIfExistsOrAddToList

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val state = MutableLiveData<RepositoryState>()
    private val messages = mutableListOf<Message>()

    init {
        messages.addAll(prepareTestData())
    }

    override fun getState(): LiveData<RepositoryState> {
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

    private fun loadMessages(userId: Int, needScrollToLastPosition: Boolean) {
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
        state.value = RepositoryState.Messages(messages, needScrollToLastPosition)
    }

    companion object {

        private const val EMPTY_MAP = 0
        private const val EMPTY_LIST = 0

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