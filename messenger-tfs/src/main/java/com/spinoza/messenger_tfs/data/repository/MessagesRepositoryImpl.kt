package com.spinoza.messenger_tfs.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesRepository

class MessagesRepositoryImpl private constructor() : MessagesRepository {

    private val state = MutableLiveData<RepositoryState>()

    private val messages = mutableListOf<Message>()

    init {
        repeat(3) { index ->
            val message = Message(
                "time: $index",
                User(index, "User$index Name", R.drawable.test_face),
                "Message $index text",
                emptyMap(),
                false,
                index
            )
            messages.add(message)
        }
    }

    override fun getState(): LiveData<RepositoryState> {
        return state
    }

    override suspend fun loadMessages() {
        state.value = RepositoryState.Messages(messages)
    }

    override suspend fun sendMessage(message: Message) {
        val newMessage = message.copy(id = messages.size + 1)
        messages.add(newMessage)
        loadMessages()
    }

    override suspend fun updateMessage(message: Message) {
        messages.replaceAll { oldMessage ->
            if (oldMessage.id == message.id) message else oldMessage
        }
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