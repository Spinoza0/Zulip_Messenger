package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateMessageUseCase
import kotlinx.coroutines.launch

class MessageFragmentViewModel(
    private val getStateUseCase: GetStateUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
) : ViewModel() {

    val messageActionIcon: LiveData<Int>
        get() = _messageActionIcon

    private val _messageActionIcon = MutableLiveData<Int>()

    init {
        loadMessages()
    }

    fun getState(): LiveData<RepositoryState> {
        return getStateUseCase()
    }

    fun loadMessages() {
        viewModelScope.launch {
            loadMessagesUseCase()
        }
    }

    fun sendMessage(messageText: String, currentUser: User): Boolean {
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                val message = Message(
                    // test data
                    MessageDate(10, "2 марта 2023"),
                    currentUser,
                    messageText,
                    emptyMap(),
                    false
                )
                sendMessageUseCase(message)
            }
            return true
        }
        return false
    }

    fun updateMessage(message: Message) {
        viewModelScope.launch {
            updateMessageUseCase(message)
        }
    }

    fun onMessageTextChanged(s: CharSequence?) {
        if (s != null && s.toString().trim().isNotEmpty()) {
            _messageActionIcon.value = R.drawable.ic_send
        } else {
            _messageActionIcon.value = R.drawable.ic_add_circle_outline
        }
    }
}