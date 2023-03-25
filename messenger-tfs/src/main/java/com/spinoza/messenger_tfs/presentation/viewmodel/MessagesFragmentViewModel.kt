package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetCurrentUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.CompanionMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.state.MessagesScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MessagesFragmentViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val messagesFilter: MessagesFilter,
) : ViewModel() {

    private lateinit var currentUser: User

    val state: StateFlow<MessagesScreenState>
        get() = _state.asStateFlow()

    private val _state =
        MutableStateFlow<MessagesScreenState>(MessagesScreenState.Loading)

    private val useCasesScope = CoroutineScope(Dispatchers.IO)

    init {
        loadCurrentUser()
    }

    override fun onCleared() {
        super.onCleared()
        useCasesScope.cancel()
    }

    private fun loadCurrentUser() {
        useCasesScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is RepositoryResult.Success -> currentUser = result.value
                is RepositoryResult.Failure.UserNotFound -> {
                    _state.value = MessagesScreenState.Failure.UserNotFound(result.userId)
                }
                // TODO: process other errors
                else -> {}
            }
        }
    }

    fun loadMessages() {
        useCasesScope.launch {
            _state.value = MessagesScreenState.Loading
            val result = getMessagesUseCase(messagesFilter)
            updateMessages(result)
        }
    }

    fun sendMessage(messageText: String): Boolean {
        if (messageText.isNotEmpty()) {
            useCasesScope.launch {
                _state.value = MessagesScreenState.Loading
                val message = Message(
                    // test data
                    MessageDate("2 марта 2023"),
                    currentUser,
                    messageText,
                    emptyMap(),
                    false
                )
                val result = sendMessageUseCase(message, messagesFilter)
                updateMessages(result)
            }
            return true
        }
        return false
    }

    fun updateReaction(messageId: Long, reaction: String) {
        useCasesScope.launch {
            _state.value = MessagesScreenState.Loading
            val result = updateReactionUseCase(messageId, reaction, messagesFilter)
            updateMessages(result)
        }
    }

    fun doOnTextChanged(text: CharSequence?) {
        val resId = if (text?.toString()?.isNotBlank() == true)
            R.drawable.ic_send
        else
            R.drawable.ic_add_circle_outline
        _state.value = MessagesScreenState.UpdateIconImage(resId)
    }

    private fun updateMessages(result: RepositoryResult<MessagesResult>) {
        when (result) {
            is RepositoryResult.Success -> _state.value = MessagesScreenState.Messages(
                MessagesResultDelegate(result.value.messages.groupByDate(), result.value.position)
            )
            is RepositoryResult.Failure.MessageNotFound -> MessagesScreenState.Failure.MessageNotFound(
                result.messageId
            )
            // TODO: process other errors
            else -> {}
        }
    }

    private fun List<Message>.groupByDate(): List<DelegateAdapterItem> {

        val messageAdapterItemList = mutableListOf<DelegateAdapterItem>()
        val dates = TreeSet<MessageDate>()
        this.forEach {
            dates.add(it.date)
        }

        dates.forEach { messageDate ->
            messageAdapterItemList.add(DateDelegateItem(messageDate))
            val allDayMessages = this.filter { message ->
                message.date.date == messageDate.date
            }

            allDayMessages.forEach { message ->
                if (message.user.userId == currentUser.userId) {
                    messageAdapterItemList.add(UserMessageDelegateItem(message))
                } else {
                    messageAdapterItemList.add(CompanionMessageDelegateItem(message))
                }
            }
        }

        return messageAdapterItemList
    }
}