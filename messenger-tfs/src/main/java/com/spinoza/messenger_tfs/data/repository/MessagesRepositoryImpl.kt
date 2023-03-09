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
                index,
                "time: $index",
                User("id$index", "User$index Name", R.drawable.test_face),
                "Message $index text",
                emptyMap(),
                false
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

    override suspend fun addMessage(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMessage(message: Message) {
        TODO("Not yet implemented")
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