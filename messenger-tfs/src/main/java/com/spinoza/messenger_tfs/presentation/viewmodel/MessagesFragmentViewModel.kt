package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.repository.RepositoryState
import com.spinoza.messenger_tfs.domain.usecase.GetRepositoryStateUseCase
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
    getRepositoryStateUseCase: GetRepositoryStateUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
) : ViewModel() {

    val repositoryState: StateFlow<RepositoryState> = getRepositoryStateUseCase(TEST_USER_ID)

    val messagesFragmentState: StateFlow<MessagesFragmentState>
        get() = _messagesFragmentState.asStateFlow()

    private val _messagesFragmentState =
        MutableStateFlow<MessagesFragmentState>(MessagesFragmentState(R.drawable.ic_add_circle_outline))

    // for testing purpose
    fun getUserId(): Long {
        return TEST_USER_ID
    }

    fun doOnTextChanged(text: CharSequence?) {
        val resId = if (text != null && text.toString().trim().isNotEmpty())
            R.drawable.ic_send
        else
            R.drawable.ic_add_circle_outline
        _messagesFragmentState.value = MessagesFragmentState(resId)
    }

    fun sendMessage(messageText: String): Boolean {
        if (messageText.isNotEmpty()) {
            viewModelScope.launch {
                val message = Message(
                    // test data
                    MessageDate("2 марта 2023"),
                    TEST_USER_ID,
                    "Name $TEST_USER_ID",
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

    fun updateReaction(messageId: Long, userId: Long, reaction: String) {
        viewModelScope.launch {
            updateReactionUseCase(messageId, userId, reaction)
        }
    }

    fun updateReaction(messageView: MessageView, reactionView: ReactionView) {
        updateReaction(messageView.messageId, TEST_USER_ID, reactionView.emoji)
    }

    companion object {
        // for testing purpose
        const val TEST_USER_ID = 100L
    }
}