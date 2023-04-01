package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.Emoji
import com.spinoza.messenger_tfs.domain.model.Message
import com.spinoza.messenger_tfs.domain.model.MessageDate
import com.spinoza.messenger_tfs.domain.model.MessagesFilter
import com.spinoza.messenger_tfs.domain.repository.MessagesResult
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.GetMessagesUseCase
import com.spinoza.messenger_tfs.domain.usecase.GetOwnUserIdUseCase
import com.spinoza.messenger_tfs.domain.usecase.SendMessageUseCase
import com.spinoza.messenger_tfs.domain.usecase.UpdateReactionUseCase
import com.spinoza.messenger_tfs.presentation.adapter.delegate.DelegateAdapterItem
import com.spinoza.messenger_tfs.presentation.adapter.message.date.DateDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.OwnMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.adapter.message.messages.UserMessageDelegateItem
import com.spinoza.messenger_tfs.presentation.model.MessagesResultDelegate
import com.spinoza.messenger_tfs.presentation.state.MessagesScreenState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

class MessagesFragmentViewModel(
    private val getOwnUserIdUseCase: GetOwnUserIdUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val messagesFilter: MessagesFilter,
) : ViewModel() {

    val state: SharedFlow<MessagesScreenState>
        get() = _state.asSharedFlow()

    private val _state =
        MutableSharedFlow<MessagesScreenState>(replay = 10)
    private val newMessageFieldState = MutableSharedFlow<String>()

    init {
        loadMessages()
        subscribeToNewMessageFieldChanges()
    }

    fun sendMessage(messageText: String) {
        if (messageText.isNotEmpty()) viewModelScope.launch {
            _state.emit(MessagesScreenState.MessageSent)
            val result = sendMessageUseCase(messageText, messagesFilter)
            handleRepositoryResult(result)
        }
    }

    fun updateReaction(messageId: Long, emoji: Emoji) {
        viewModelScope.launch {
            _state.emit(MessagesScreenState.ReactionSent)
            val result = updateReactionUseCase(messageId, emoji, messagesFilter)
            handleRepositoryResult(result)
        }
    }

    fun doOnTextChanged(text: CharSequence?) {
        viewModelScope.launch {
            newMessageFieldState.emit(text.toString())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToNewMessageFieldChanges() {
        newMessageFieldState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_SET_STATE)
            .mapLatest { text ->
                val resId = if (text.isNotBlank())
                    R.drawable.ic_send
                else
                    R.drawable.ic_add_circle_outline
                resId
            }
            .distinctUntilChanged()
            .onEach { resId ->
                _state.emit(MessagesScreenState.UpdateIconImage(resId))
            }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    private fun loadMessages() {
        viewModelScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result = getMessagesUseCase(messagesFilter)
            handleRepositoryResult(result)
            setLoadingState.cancel()
        }
    }

    private suspend fun handleRepositoryResult(result: RepositoryResult<MessagesResult>) {
        when (result) {
            is RepositoryResult.Success -> withContext(Dispatchers.Default) {
                when (val userIdResult = getOwnUserIdUseCase()) {
                    is RepositoryResult.Success -> _state.emit(
                        MessagesScreenState.Messages(
                            MessagesResultDelegate(
                                result.value.messages.groupByDate(userIdResult.value),
                                result.value.position
                            )
                        )
                    )
                    is RepositoryResult.Failure -> handleErrors(userIdResult)
                }
            }
            is RepositoryResult.Failure -> handleErrors(result)
        }
    }

    private suspend fun handleErrors(error: RepositoryResult.Failure) {
        when (error) {
            is RepositoryResult.Failure.OwnUserNotFound ->
                _state.emit(MessagesScreenState.Failure.OwnUserNotFound(error.value))
            is RepositoryResult.Failure.MessageNotFound ->
                _state.emit(MessagesScreenState.Failure.MessageNotFound(error.messageId))
            is RepositoryResult.Failure.UserNotFound ->
                _state.emit(MessagesScreenState.Failure.UserNotFound(error.userId, error.value))
            is RepositoryResult.Failure.SendingMessage ->
                _state.emit(MessagesScreenState.Failure.SendingMessage(error.value))
            is RepositoryResult.Failure.UpdatingReaction ->
                _state.emit(MessagesScreenState.Failure.UpdatingReaction(error.value))
            is RepositoryResult.Failure.Network ->
                _state.emit(MessagesScreenState.Failure.Network(error.value))
            is RepositoryResult.Failure.LoadingMessages -> _state.emit(
                MessagesScreenState.Failure.LoadingMessages(
                    error.messagesFilter,
                    error.value
                )
            )
            else -> {}
        }
    }

    private fun setLoadingStateWithDelay(): Job {
        return viewModelScope.launch {
            delay(DELAY_BEFORE_SET_STATE)
            _state.emit(MessagesScreenState.Loading)
        }
    }

    private fun List<Message>.groupByDate(userId: Long): List<DelegateAdapterItem> {

        val messageAdapterItemList = mutableListOf<DelegateAdapterItem>()
        val dates = TreeSet<MessageDate>()
        forEach {
            dates.add(it.date)
        }

        dates.forEach { messageDate ->
            messageAdapterItemList.add(DateDelegateItem(messageDate))
            val allDayMessages = this.filter { message ->
                message.date.value == messageDate.value
            }

            allDayMessages.forEach { message ->
                if (message.user.userId == userId) {
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