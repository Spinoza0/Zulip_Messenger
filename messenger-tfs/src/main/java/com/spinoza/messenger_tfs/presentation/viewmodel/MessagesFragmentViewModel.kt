package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.model.User
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetOwnUserUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.state.MessagesScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

class MessagesFragmentViewModel(
    private val getOwnUserUseCase: GetOwnUserUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val messagesFilter: MessagesFilter,
) : ViewModel() {

    private var currentUser: User? = null

    val state: SharedFlow<MessagesScreenState>
        get() = _state.asSharedFlow()

    private val _state =
        MutableSharedFlow<MessagesScreenState>(replay = 1)

    fun onResume(isMessagesListEmpty: Boolean) {
        if (isMessagesListEmpty) viewModelScope.launch {
            loadCurrentUser()
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isNotEmpty()) viewModelScope.launch {
            if (currentUser == null) {
                _state.emit(MessagesScreenState.Failure.CurrentUserNotFound(""))
            } else currentUser?.let { user ->
                _state.emit(MessagesScreenState.MessageSent)
                val message = Message(
                    // TODO: test data
                    MessageDate("2 марта 2023"),
                    user,
                    messageText,
                    emptyMap(),
                    false
                )
                val result = sendMessageUseCase(message, messagesFilter)
                handleRepositoryResult(result)
            }
        }
    }

    fun updateReaction(messageId: Long, reaction: String) {
        viewModelScope.launch {
            _state.emit(MessagesScreenState.ReactionSent)
            val result = updateReactionUseCase(messageId, reaction, messagesFilter)
            handleRepositoryResult(result)
        }
    }

    fun doOnTextChanged(text: CharSequence?) {
        viewModelScope.launch {
            val resId = if (text?.toString()?.isNotBlank() == true)
                R.drawable.ic_send
            else
                R.drawable.ic_add_circle_outline
            _state.emit(MessagesScreenState.UpdateIconImage(resId))
        }
    }

    private suspend fun loadCurrentUser() {
        val setLoadingState = setLoadingStateWithDelay()
        val result = getOwnUserUseCase()
        setLoadingState.cancel()
        when (result) {
            is RepositoryResult.Success -> {
                currentUser = result.value
                loadMessages()
            }
            is RepositoryResult.Failure -> handleErrors(result)
        }
    }

    private suspend fun loadMessages() {
        if (currentUser == null) {
            _state.emit(MessagesScreenState.Failure.CurrentUserNotFound(""))
        } else {
            val setLoadingState = setLoadingStateWithDelay()
            val result = getMessagesUseCase(messagesFilter)
            handleRepositoryResult(result)
            setLoadingState.cancel()
        }
    }

    private suspend fun handleRepositoryResult(result: RepositoryResult<MessagesResult>) {
        if (currentUser == null) {
            _state.emit(MessagesScreenState.Failure.CurrentUserNotFound(""))
        } else currentUser?.let { user ->
            when (result) {
                is RepositoryResult.Success -> withContext(Dispatchers.Default) {
                    _state.emit(
                        MessagesScreenState.Messages(
                            MessagesResultDelegate(
                                result.value.messages.groupByDate(user),
                                result.value.position
                            )
                        )
                    )
                }
                is RepositoryResult.Failure -> handleErrors(result)
            }
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.CurrentUserNotFound ->
                _state.emit(MessagesScreenState.Failure.CurrentUserNotFound(error.value))
            is RepositoryResult.Failure.MessageNotFound ->
                _state.emit(MessagesScreenState.Failure.MessageNotFound(error.messageId))
            is RepositoryResult.Failure.UserNotFound -> {
                _state.emit(MessagesScreenState.Failure.UserNotFound(error.userId))
            }
            is RepositoryResult.Failure.SendingMessage ->
                _state.emit(MessagesScreenState.Failure.SendingMessage(error.value))
            is RepositoryResult.Failure.UpdatingReaction ->
                _state.emit(MessagesScreenState.Failure.UpdatingReaction(error.value))
            else -> {}
        }
    }

    private fun setLoadingStateWithDelay(): Job {
        return viewModelScope.launch {
            delay(DELAY_BEFORE_SET_STATE)
            _state.emit(MessagesScreenState.Loading)
        }
    }

    private fun List<Message>.groupByDate(user: User): List<DelegateAdapterItem> {

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
                if (message.user.userId == user.userId) {
                    messageAdapterItemList.add(OwnMessageDelegateItem(message))
                } else {
                    messageAdapterItemList.add(UserMessageDelegateItem(message))
                }
            }
        }

        return messageAdapterItemList
    }

    private companion object {

        const val DELAY_BEFORE_SET_STATE = 200L
    }
}