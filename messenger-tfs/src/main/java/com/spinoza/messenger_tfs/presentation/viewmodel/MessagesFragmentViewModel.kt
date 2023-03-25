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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

class MessagesFragmentViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
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
            val setLoadingState = setLoadingStateWithDelay()
            when (val result = getCurrentUserUseCase()) {
                is RepositoryResult.Success -> currentUser = result.value
                is RepositoryResult.Failure -> handleErrors(result)
            }
            setLoadingState.cancel()
        }
    }

    fun loadMessages() {
        if (currentUser != null) useCasesScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result = getMessagesUseCase(messagesFilter)
            handleRepositoryResult(result)
            setLoadingState.cancel()
        }
    }

    fun sendMessage(messageText: String): Boolean {
        currentUser?.let { user ->
            if (messageText.isNotEmpty()) {
                useCasesScope.launch {
                    val setLoadingState = setLoadingStateWithDelay()
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
                    setLoadingState.cancel()
                }
                return true
            }
        }
        return false
    }

    fun updateReaction(messageId: Long, reaction: String) {
        useCasesScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result = updateReactionUseCase(messageId, reaction, messagesFilter)
            handleRepositoryResult(result)
            setLoadingState.cancel()
        }
    }

    fun doOnTextChanged(text: CharSequence?) {
        useCasesScope.launch {
            val resId = if (text?.toString()?.isNotBlank() == true)
                R.drawable.ic_send
            else
                R.drawable.ic_add_circle_outline
            _state.emit(MessagesScreenState.UpdateIconImage(resId))
        }
    }

    private suspend fun handleRepositoryResult(result: RepositoryResult<MessagesResult>) {
        currentUser?.let { user ->
            when (result) {
                is RepositoryResult.Success -> _state.emit(
                    MessagesScreenState.Messages(
                        MessagesResultDelegate(
                            result.value.messages.groupByDate(user),
                            result.value.position
                        )
                    )
                )
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
        return useCasesScope.launch {
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
                    messageAdapterItemList.add(UserMessageDelegateItem(message))
                } else {
                    messageAdapterItemList.add(CompanionMessageDelegateItem(message))
                }
            }
        }

        return messageAdapterItemList
    }

    private companion object {

        const val DELAY_BEFORE_SET_STATE = 200L
    }
}