package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetStateUseCase
import com.spinoza.messenger_tfs.domain.usecase.LoadMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlinx.coroutines.launch

class MessagesFragmentViewModel(
    private val getStateUseCase: GetStateUseCase,
    private val loadMessagesUseCase: LoadMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModel() {

    val messageActionIcon: LiveData<Int>
        get() = _messageActionIcon

    private val _messageActionIcon = MutableLiveData<Int>()

    fun getState(): LiveData<RepositoryState> {
        return getStateUseCase()
    }

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
        if (s != null && s.toString().trim().isNotEmpty()) {
            _messageActionIcon.value = R.drawable.ic_send
        } else {
            _messageActionIcon.value = R.drawable.ic_add_circle_outline
        }
    }

    fun updateReaction(messageView: MessageView, userId: Int, reactionView: ReactionView) {
        viewModelScope.launch {
            updateReactionUseCase(messageView.messageId, userId, reactionView.emoji)
        }
    }
}