package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
import com.spinoza.messenger_tfs.domain.model.event.MessagesEvent
import com.spinoza.messenger_tfs.domain.repository.RepositoryResult
import com.spinoza.messenger_tfs.domain.usecase.*
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
    private val messagesFilter: MessagesFilter,
    private val getOwnUserIdUseCase: GetOwnUserIdUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateReactionUseCase: UpdateReactionUseCase,
    private val registerEventQueueUseCase: RegisterEventQueueUseCase,
    private val deleteEventQueueUseCase: DeleteEventQueueUseCase,
    private val getMessageEventsUseCase: GetMessageEventsUseCase,
    private val setOwnStatusActiveUseCase: SetOwnStatusActiveUseCase,
) : ViewModel() {

    val state: SharedFlow<MessagesScreenState>
        get() = _state.asSharedFlow()

    private val _state =
        MutableSharedFlow<MessagesScreenState>(replay = 10)
    private val newMessageFieldState = MutableSharedFlow<String>()
    private var eventsQueue = EventsQueue()
    private var isMessageSent = false

    init {
        loadMessages()
        subscribeToNewMessageFieldChanges()
        setOwnStatusToActive()
    }

    fun sendMessage(messageText: String) {
        if (messageText.isNotEmpty()) viewModelScope.launch {
            _state.emit(MessagesScreenState.SendingMessage)
            val result = sendMessageUseCase(messageText, messagesFilter)
            if (result is RepositoryResult.Success) {
                isMessageSent = true
                _state.emit(MessagesScreenState.MessageSent(result.value))
            }
        }
    }

    fun updateReaction(messageId: Long, emoji: Emoji) {
        viewModelScope.launch {
            val result = updateReactionUseCase(messageId, emoji, messagesFilter)
            if (result is RepositoryResult.Success) {
                _state.emit(MessagesScreenState.ReactionSent)
                when (val userIdResult = getOwnUserIdUseCase()) {
                    is RepositoryResult.Success -> {
                        handleSuccessMessagesResult(result.value, userIdResult.value)
                    }
                    is RepositoryResult.Failure -> handleErrors(userIdResult)
                }
            }
        }
    }

    fun doOnTextChanged(text: CharSequence?) {
        viewModelScope.launch {
            newMessageFieldState.emit(text.toString())
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            val setLoadingState = setLoadingStateWithDelay()
            val result = getMessagesUseCase(messagesFilter)
            setLoadingState.cancel()
            handleRepositoryResult(result)
        }
    }

    private fun setOwnStatusToActive() {
        viewModelScope.launch {
            while (true) {
                setOwnStatusActiveUseCase()
                delay(DELAY_BEFORE_UPDATE_OWN_STATUS)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun subscribeToNewMessageFieldChanges() {
        newMessageFieldState
            .distinctUntilChanged()
            .debounce(DELAY_BEFORE_UPDATE_ACTION_ICON)
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

    private suspend fun handleRepositoryResult(result: RepositoryResult<MessagesResult>) {
        when (result) {
            is RepositoryResult.Success -> withContext(Dispatchers.Default) {
                when (val userIdResult = getOwnUserIdUseCase()) {
                    is RepositoryResult.Success -> {
                        handleSuccessMessagesResult(result.value, userIdResult.value)
                        registerEventQueue()
                    }
                    is RepositoryResult.Failure -> handleErrors(userIdResult)
                }
            }
            is RepositoryResult.Failure -> handleErrors(result)
        }
    }

    private suspend fun handleSuccessMessagesResult(
        messagesResult: MessagesResult,
        userId: Long,
    ) {
        _state.emit(
            MessagesScreenState.Messages(
                MessagesResultDelegate(
                    messagesResult.messages.groupByDate(userId),
                    messagesResult.position
                )
            )
        )
    }

    private fun registerEventQueue() {
        viewModelScope.launch {
            when (val queueResult = registerEventQueueUseCase(
                listOf(
                    EventType.MESSAGE,
                    EventType.DELETE_MESSAGE,
                    EventType.REACTION
                ), messagesFilter
            )) {
                is RepositoryResult.Success -> {
                    eventsQueue = queueResult.value
                    handleOnSuccessQueueRegistration()
                }
                is RepositoryResult.Failure -> handleErrors(queueResult)
            }
        }
    }

    private suspend fun handleOnSuccessQueueRegistration() {
        while (true) {
            val eventResult = getMessageEventsUseCase(eventsQueue, messagesFilter)
            if (eventResult is RepositoryResult.Success) {
                handleEvent(eventResult.value)
            }
        }
    }

    private suspend fun handleEvent(messagesEvent: MessagesEvent) {
        val userIdResult = getOwnUserIdUseCase()
        if (userIdResult is RepositoryResult.Success) {
            eventsQueue = eventsQueue.copy(lastEventId = messagesEvent.lastEventId)
            val messagesResult = if (isMessageSent) {
                isMessageSent = false
                messagesEvent.messagesResult.copy(
                    position = MessagePosition(MessagePosition.Type.LAST_POSITION)
                )
            } else {
                messagesEvent.messagesResult
            }
            handleSuccessMessagesResult(messagesResult, userIdResult.value)
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
            delay(DELAY_BEFORE_SHOW_SHIMMER)
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

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            deleteEventQueueUseCase(eventsQueue.queueId)
        }
    }

    private companion object {

        const val DELAY_BEFORE_SHOW_SHIMMER = 100L
        const val DELAY_BEFORE_UPDATE_ACTION_ICON = 200L
        const val DELAY_BEFORE_UPDATE_OWN_STATUS = 60_000L
    }
}