package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Channel
import com.spinoza.messenger_tfs.domain.model.ChannelFilter
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.model.MessagesFragmentState
import com.spinoza.messenger_tfs.presentation.ui.MessageView
import com.spinoza.messenger_tfs.presentation.ui.ReactionView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessagesFragmentViewModel(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    channel: Channel,
    topicName: String,
) : ViewModel() {

    val user = getCurrentUserUseCase()

    val state: StateFlow<MessagesFragmentState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<MessagesFragmentState>(
            MessagesFragmentState.SendIconImage(R.drawable.ic_add_circle_outline)
        )

    private val channelFilter = ChannelFilter(channel.channelId, topicName)

    fun getMessages() {
        viewModelScope.launch {
            _state.value = MessagesFragmentState.Loading
            _state.value = MessagesFragmentState.Repository(getMessagesUseCase(channelFilter))
        }
    }

    fun doOnTextChanged(text: CharSequence?) {
        val resId = if (text != null && text.toString().trim().isNotEmpty())
            R.drawable.ic_send
        else
            R.drawable.ic_add_circle_outline
        _state.value = MessagesFragmentState.SendIconImage(resId)
    }

    fun sendMessage(messageText: String): Boolean {
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                val message = Message(
                    // test data
                    MessageDate("2 марта 2023"),
                    user,
                    messageText,
                    emptyMap(),
                    false
                )
                _state.value = MessagesFragmentState.Repository(
                    sendMessageUseCase(message, channelFilter)
                )
            }
            return true
        }
        return false
    }

    fun updateReaction(messageId: Long, reaction: String) {
        viewModelScope.launch {
            _state.value = MessagesFragmentState.Repository(
                updateReactionUseCase(messageId, reaction, channelFilter)
            )
        }
    }

    fun updateReaction(messageView: MessageView, reactionView: ReactionView) {
        updateReaction(messageView.messageId, reactionView.emoji)
    }
}