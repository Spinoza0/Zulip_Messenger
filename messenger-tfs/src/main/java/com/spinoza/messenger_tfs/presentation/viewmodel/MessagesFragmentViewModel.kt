package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.MessagesState
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MessagesFragmentViewModel(
    getMessagesStateUseCase: GetMessagesStateUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModel() {

    private val _state: MutableSharedFlow<MessagesState> = getMessagesStateUseCase()

    val state: SharedFlow<MessagesState>
        get() = _state.asSharedFlow()


    fun loadMessages(userId: Int) {
        viewModelScope.launch {
            loadMessagesUseCase(userId)
        }
    }

    fun sendMessage(messageText: String, userId: Int): Boolean {
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                val message = Message(
                    // test data
                    MessageDate(10, "2 марта 2023"),
                    userId,
                    "Name $userId",
                    messageText,
                    R.drawable.test_face,
                    emptyMap(),
                    false
                )
                sendMessageUseCase(message)
            }
            return true
        }
        return false
    }

    fun onMessageTextChanged(s: CharSequence?) {
        viewModelScope.launch {
            if (s != null && s.toString().trim().isNotEmpty()) {
                _state.emit(MessagesState.ReadyToSend(true))
            } else {
                _state.emit(MessagesState.ReadyToSend(false))
            }
        }
    }

    fun updateReaction(messageView: MessageView, userId: Int, reactionView: ReactionView) {
        viewModelScope.launch {
            updateReactionUseCase(messageView.messageId, userId, reactionView.emoji)
        }
    }
}