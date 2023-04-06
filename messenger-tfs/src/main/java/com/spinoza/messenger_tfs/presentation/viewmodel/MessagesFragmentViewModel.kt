package com.spinoza.messenger_tfs.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinoza.messenger_tfs.R
import com.spinoza.messenger_tfs.domain.model.*
import com.spinoza.messenger_tfs.domain.model.event.EventType
import com.spinoza.messenger_tfs.domain.model.event.EventsQueue
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
    private val getMessageEventUseCase: GetMessageEventUseCase,
    private val getDeleteMessageEventUseCase: GetDeleteMessageEventUseCase,
    private val getReactionEventUseCase: GetReactionEventUseCase,
    private val setOwnStatusActiveUseCase: SetOwnStatusActiveUseCase,
    private val setMessagesFlagToReadUserCase: SetMessagesFlagToReadUserCase,
) : ViewModel() {

    val state: SharedFlow<MessagesScreenState>
        get() = _state.asSharedFlow()

    private val _state =
        MutableSharedFlow<MessagesScreenState>(replay = 10)
    private val newMessageFieldState = MutableSharedFlow<String>()
    private var messagesQueue = EventsQueue()
    private var deleteMessagesQueue = EventsQueue()
    private var reactionsQueue = EventsQueue()
    private var isMessageSent = false
    private val readMessageIds = TreeSet<Long>()
    private var isReadMessageIdsChanged = false

    init {
        loadMessages()
        subscribeToNewMessageFieldChanges()
        setOwnStatusToActive()
        setReadFlags()
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

    fun addToReadMessageIds(messageId: Long) {
        val oldSize = readMessageIds.size
        readMessageIds.add(messageId)
        if(!isReadMessageIdsChanged) {
            isReadMessageIdsChanged = readMessageIds.size != oldSize
        }
    }

    fun updateReaction(messageId: Long, emoji: Emoji) {
        viewModelScope.launch {
            val result = updateReactionUseCase(messageId, emoji, messagesFilter)
            if (result is RepositoryResult.Success) {
                _state.emit(MessagesScreenState.ReactionSent)
                when (val userIdResult = getOwnUserIdUseCase()) {
                    is RepositoryResult.Success ->
                        handleMessagesResult(result.value, userIdResult.value)
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
            _state.emit(MessagesScreenState.Loading)
            val result = getMessagesUseCase(messagesFilter)
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

    private fun setReadFlags() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(DELAY_BEFORE_UPDATE_READ_FLAG)
                if (isReadMessageIdsChanged) {
                    isReadMessageIdsChanged = false
                    setMessagesFlagToReadUserCase(readMessageIds.toList())
                }
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
                        handleMessagesResult(result.value, userIdResult.value)
                        registerMessagesQueue()
                        registerDeleteMessagesQueue()
                        registerReactionsQueue()
                    }
                    is RepositoryResult.Failure -> handleErrors(userIdResult)
                }
            }
            is RepositoryResult.Failure -> handleErrors(result)
        }
    }

    private suspend fun handleMessagesResult(result: MessagesResult, userId: Long) {
        _state.emit(
            MessagesScreenState.Messages(
                MessagesResultDelegate(result.messages.groupByDate(userId), result.position)
            )
        )
    }

    private suspend fun registerMessagesQueue() {
        when (val queueResult =
            registerEventQueueUseCase(listOf(EventType.MESSAGE), messagesFilter)) {
            is RepositoryResult.Success -> {
                messagesQueue = queueResult.value
                checkMessagesEvents()
            }
            is RepositoryResult.Failure -> handleErrors(queueResult)
        }
    }

    private suspend fun registerDeleteMessagesQueue() {
        when (val queueResult =
            registerEventQueueUseCase(listOf(EventType.DELETE_MESSAGE), messagesFilter)) {
            is RepositoryResult.Success -> {
                deleteMessagesQueue = queueResult.value
                checkDeleteMessagesEvents()
            }
            is RepositoryResult.Failure -> handleErrors(queueResult)
        }
    }

    private suspend fun registerReactionsQueue() {
        when (val queueResult =
            registerEventQueueUseCase(listOf(EventType.REACTION), messagesFilter)) {
            is RepositoryResult.Success -> {
                reactionsQueue = queueResult.value
                checkReactionsEvents()
            }
            is RepositoryResult.Failure -> handleErrors(queueResult)
        }
    }

    private fun checkMessagesEvents() {
        viewModelScope.launch {
            while (true) {
                val eventResult =
                    getMessageEventUseCase(messagesQueue, messagesFilter)
                if (eventResult is RepositoryResult.Success) {
                    val userIdResult = getOwnUserIdUseCase()
                    if (userIdResult is RepositoryResult.Success) {
                        messagesQueue =
                            messagesQueue.copy(lastEventId = eventResult.value.lastEventId)
                        val messagesResult = if (isMessageSent) {
                            isMessageSent = false
                            eventResult.value.messagesResult.copy(
                                position = MessagePosition(MessagePosition.Type.LAST_POSITION)
                            )
                        } else {
                            eventResult.value.messagesResult
                        }
                        handleMessagesResult(messagesResult, userIdResult.value)
                    }
                }
            }
        }
    }

    private fun checkDeleteMessagesEvents() {
        viewModelScope.launch {
            while (true) {
                val eventResult =
                    getDeleteMessageEventUseCase(deleteMessagesQueue, messagesFilter)
                if (eventResult is RepositoryResult.Success) {
                    val userIdResult = getOwnUserIdUseCase()
                    if (userIdResult is RepositoryResult.Success) {
                        deleteMessagesQueue =
                            deleteMessagesQueue.copy(lastEventId = eventResult.value.lastEventId)
                        handleMessagesResult(eventResult.value.messagesResult, userIdResult.value)
                    }
                }
                delay(DELAY_DELETE_MESSAGES_EVENTS)
            }
        }
    }

    private fun checkReactionsEvents() {
        viewModelScope.launch {
            while (true) {
                val eventResult =
                    getReactionEventUseCase(reactionsQueue, messagesFilter)
                if (eventResult is RepositoryResult.Success) {
                    val userIdResult = getOwnUserIdUseCase()
                    if (userIdResult is RepositoryResult.Success) {
                        reactionsQueue =
                            reactionsQueue.copy(lastEventId = eventResult.value.lastEventId)
                        handleMessagesResult(eventResult.value.messagesResult, userIdResult.value)
                    }
                }
                delay(DELAY_REACTIONS_EVENTS)
            }
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
            deleteEventQueueUseCase(messagesQueue.queueId)
            deleteEventQueueUseCase(deleteMessagesQueue.queueId)
            deleteEventQueueUseCase(reactionsQueue.queueId)
        }
    }

    private companion object {

        const val DELAY_BEFORE_UPDATE_ACTION_ICON = 200L
        const val DELAY_BEFORE_UPDATE_OWN_STATUS = 60_000L
        const val DELAY_BEFORE_UPDATE_READ_FLAG = 10_000L
        const val DELAY_REACTIONS_EVENTS = 200L
        const val DELAY_DELETE_MESSAGES_EVENTS = 500L
    }
}