package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.state.MessagesScreenState
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesFragmentViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val channelFilter: ChannelFilter,
) : ViewModel() {

    val user = getCurrentUserUseCase()

    val state: StateFlow<MessagesScreenState>
        get() = _state.asStateFlow()

    private val _state = MutableStateFlow<MessagesScreenState>(
        MessagesScreenState.UpdateIconImage(R.drawable.ic_add_circle_outline)
    )

    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase.invoke()
            if (result.first.type == RepositoryResult.Type.SUCCESS) {
                result.second?.let { _state.value = MessagesScreenState.CurrentUser(it) }
            } else {
                _state.value = MessagesScreenState.Error(result.first)
            }
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            _state.value = MessagesScreenState.Loading
            val result = getMessagesUseCase(channelFilter)
            updateMessages(result)
        }
    }

    fun sendMessage(user: User, messageText: String): Boolean {
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                _state.value = MessagesScreenState.Loading
                val message = Message(
                    // test data
                    MessageDate("2 марта 2023"),
                    user,
                    messageText,
                    emptyMap(),
                    false
                )
                val result = sendMessageUseCase(message, channelFilter)
                updateMessages(result)
            }
            return true
        }
        return false
    }

    fun updateReaction(messageId: Long, reaction: String) {
        viewModelScope.launch {
            _state.value = MessagesScreenState.Loading
            val result = updateReactionUseCase(messageId, reaction, channelFilter)
            updateMessages(result)
        }
    }

    fun updateReaction(messageView: MessageView, reactionView: ReactionView) {
        updateReaction(messageView.messageId, reactionView.emoji)
    }

    fun doOnTextChanged(text: CharSequence?) {
        val resId = if (text != null && text.toString().trim().isNotEmpty())
            R.drawable.ic_send
        else
            R.drawable.ic_add_circle_outline
        _state.value = MessagesScreenState.UpdateIconImage(resId)
    }

    private fun updateMessages(result: Pair<RepositoryResult, MessagesResult?>) {
        if (result.first.type == RepositoryResult.Type.SUCCESS) {
            result.second?.let { _state.value = MessagesScreenState.Messages(it) }
        } else {
            _state.value = MessagesScreenState.Error(result.first)
        }
    }
}